package com.example.mybudgetapp.ui.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.CATEGORY_KEY_FOOD
import com.example.mybudgetapp.database.CATEGORY_KEY_OTHERS
import com.example.mybudgetapp.database.CATEGORY_KEY_TRANSPORTATION
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.database.displayTitle
import com.example.mybudgetapp.ui.widgets.DropDownItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.Month

class ThisYearScreenViewModel(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val date: LocalDate = LocalDate.now()
    private val selectedYear = MutableStateFlow(date.year)
    private val availableYears = itemRepository.getAllYears()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val activeYear = combine(availableYears, selectedYear) { years, selected ->
        val normalizedYears = years.withCurrentYear(date.year)
        if (normalizedYears.isEmpty()) selected else normalizedYears.firstOrNull { it == selected } ?: normalizedYears.last()
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ThisYearScreenUiState> = combine(availableYears, activeYear) { years, active ->
        val normalizedYears = years.withCurrentYear(date.year).ifEmpty { listOf(active) }
        normalizedYears to active
    }.flatMapLatest { (years, currentYear) ->
        val totalsFlow = combine(
            itemRepository.getTotalSpendingOverallForYear(year = currentYear),
            itemRepository.getTotalIncomeOverallForYear(year = currentYear),
            itemRepository.getCategoryTotalsByTypeForYear(
                type = TRANSACTION_TYPE_EXPENSE,
                year = currentYear,
                includeArchived = true,
            ),
            itemRepository.getAllCategories(includeArchived = true),
        ) { totalSpending, totalIncome, categoryTotals, categories ->
            YearTotals(
                totalSpending = totalSpending,
                totalIncome = totalIncome,
                categoryTotals = categoryTotals.toCategorySummaryUi(),
                categoryNames = categories.associate { it.categoryKey to it.name },
            )
        }
        combine(
            totalsFlow,
            itemRepository.getTransactionsForYear(year = currentYear),
            itemRepository.getMonthlySpendingTotals(year = currentYear),
            itemRepository.getTotalSpendingOverallForYear(year = currentYear - 1),
        ) { totals, transactions, monthlyTotals, previousYearTotal ->
            val selectedIndex = years.indexOf(currentYear)
            val monthMap = monthlyTotals.associate { it.month to it.total }
            ThisYearScreenUiState(
                totalSpendingAmountForYear = totals.totalSpending,
                totalIncomeAmountForYear = totals.totalIncome,
                totalFoodAmountForYear = totals.categoryTotals.amountFor(CATEGORY_KEY_FOOD),
                totalTransportationAmountForYear = totals.categoryTotals.amountFor(CATEGORY_KEY_TRANSPORTATION),
                totalOthersAmountForYear = totals.categoryTotals.amountFor(CATEGORY_KEY_OTHERS),
                totalSpendingForYear = formatCurrencyIraqiDinar(totals.totalSpending),
                totalIncomeForYear = formatCurrencyIraqiDinar(totals.totalIncome),
                totalSpendingOnFoodForYear = formatCompactCurrencyIraqiDinar(totals.categoryTotals.amountFor(CATEGORY_KEY_FOOD)),
                totalSpendingOnOthersForYear = formatCompactCurrencyIraqiDinar(totals.categoryTotals.amountFor(CATEGORY_KEY_OTHERS)),
                totalSpendingOnTransportationForYear = formatCompactCurrencyIraqiDinar(totals.categoryTotals.amountFor(CATEGORY_KEY_TRANSPORTATION)),
                currentMonth = Month.of(date.monthValue).toString().capitalized(),
                selectedYear = currentYear,
                currentYear = currentYear.toString(),
                years = years.map {
                    DropDownItem(
                        title = it.toString(),
                        number = it
                    )
                },
                canNavigatePrevious = selectedIndex > 0,
                canNavigateNext = selectedIndex in 0 until years.lastIndex,
                isCurrentYear = currentYear == date.year,
                spendingTrend = buildYearTrendPoints(monthMap),
                comparison = buildYearlyComparison(totals.totalSpending, previousYearTotal, currentYear),
                insights = yearInsights(
                    transactions,
                    totals.totalSpending,
                    currentYear,
                    totals.categoryNames,
                ),
                categoryTotals = totals.categoryTotals,
                recentTransactions = transactions.take(4).map { transaction ->
                    val categoryDetails = totals.categoryTotals.firstOrNull { it.categoryKey == transaction.category }
                    HomeTransactionPreview(
                        title = transaction.displayTitle(),
                        categoryKey = transaction.category,
                        category = categoryLabel(transaction.category, totals.categoryNames),
                        categoryColorHex = categoryDetails?.colorHex.orEmpty(),
                        amount = formatCompactCurrencyIraqiDinar(transaction.amount),
                        date = transaction.transactionDate,
                        type = transaction.type,
                        year = currentYear,
                        month = 0,
                    )
                },
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = ThisYearScreenUiState(
            currentMonth = Month.of(date.monthValue).toString().capitalized(),
            selectedYear = date.year,
            currentYear = date.year.toString(),
            isCurrentYear = true,
        )
    )

    fun selectYear(year: Int) {
        selectedYear.value = year
    }

    fun selectPreviousYear() {
        val years = uiState.value.years
        val currentIndex = years.indexOfFirst { it.number == uiState.value.selectedYear }
        if (currentIndex > 0) {
            selectYear(years[currentIndex - 1].number)
        }
    }

    fun selectNextYear() {
        val years = uiState.value.years
        val currentIndex = years.indexOfFirst { it.number == uiState.value.selectedYear }
        if (currentIndex in 0 until years.lastIndex) {
            selectYear(years[currentIndex + 1].number)
        }
    }

    fun jumpToCurrentYear() {
        selectedYear.value = date.year
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

private fun List<Int>.withCurrentYear(currentYear: Int): List<Int> {
    return (this + currentYear).distinct().sorted()
}

private data class YearTotals(
    val totalSpending: Double,
    val totalIncome: Double,
    val categoryTotals: List<CategorySummaryUi>,
    val categoryNames: Map<String, String>,
)

data class ThisYearScreenUiState(
    val currentMonth: String = "",
    val selectedYear: Int = 0,
    val currentYear: String = "",
    val totalSpendingAmountForYear: Double = 0.0,
    val totalIncomeAmountForYear: Double = 0.0,
    val totalFoodAmountForYear: Double = 0.0,
    val totalTransportationAmountForYear: Double = 0.0,
    val totalOthersAmountForYear: Double = 0.0,
    val totalSpendingForYear: String = "",
    val totalIncomeForYear: String = "",
    val totalSpendingOnFoodForYear: String = "",
    val totalSpendingOnTransportationForYear: String = "",
    val totalSpendingOnOthersForYear: String = "",
    val years: List<DropDownItem> = listOf(),
    val canNavigatePrevious: Boolean = false,
    val canNavigateNext: Boolean = false,
    val isCurrentYear: Boolean = false,
    val spendingTrend: List<TrendPointUi> = emptyList(),
    val comparison: ComparisonInsightUi = ComparisonInsightUi(),
    val insights: List<StatInsightUi> = emptyList(),
    val categoryTotals: List<CategorySummaryUi> = emptyList(),
    val recentTransactions: List<HomeTransactionPreview> = emptyList(),
    val screenItemsUiState: MutableState<ScreenItemsUiState> = mutableStateOf(ScreenItemsUiState())
)
