package com.example.mybudgetapp.ui.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.CATEGORY_KEY_FOOD
import com.example.mybudgetapp.database.CATEGORY_KEY_OTHERS
import com.example.mybudgetapp.database.CATEGORY_KEY_TRANSPORTATION
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.MonthPeriod
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.database.displayTitle
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
import java.time.YearMonth

class ThisMonthScreenViewModel(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val selectedPeriod = MutableStateFlow(MonthPeriod(today.year, today.monthValue))
    private val availablePeriods = itemRepository.getAvailableMonthPeriods()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val activePeriod = combine(availablePeriods, selectedPeriod) { periods, selected ->
        val normalizedPeriods = periods.withCurrentPeriod(MonthPeriod(today.year, today.monthValue))
        if (normalizedPeriods.isEmpty()) {
            selected
        } else {
            normalizedPeriods.firstOrNull { it.year == selected.year && it.month == selected.month }
                ?: normalizedPeriods.last()
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ThisMonthScreenUiState> = combine(availablePeriods, activePeriod) { periods, active ->
        val normalizedPeriods = periods.withCurrentPeriod(MonthPeriod(today.year, today.monthValue)).ifEmpty { listOf(active) }
        normalizedPeriods to active
    }.flatMapLatest { (periods, period) ->
        val previousPeriod = YearMonth.of(period.year, period.month).minusMonths(1)
        val totalsFlow = combine(
            itemRepository.getTotalSpendingOverall(month = period.month, year = period.year),
            itemRepository.getTotalIncomeOverall(year = period.year, month = period.month),
            itemRepository.getCategoryTotalsByType(
                type = TRANSACTION_TYPE_EXPENSE,
                year = period.year,
                month = period.month,
                includeArchived = true,
            ),
            itemRepository.getAllCategories(includeArchived = true),
        ) { totalSpending, totalIncome, categoryTotals, categories ->
            val categorySummary = categoryTotals.toCategorySummaryUi()
            MonthTotals(
                totalSpending = totalSpending,
                totalIncome = totalIncome,
                categoryTotals = categorySummary,
                categoryNames = categories.associate { it.categoryKey to it.name },
            )
        }
        combine(
            totalsFlow,
            itemRepository.getTransactions(month = period.month, year = period.year),
            itemRepository.getDailySpendingTotals(month = period.month, year = period.year),
            itemRepository.getTotalSpendingOverall(year = previousPeriod.year, month = previousPeriod.monthValue),
        ) { totals, transactions, dailyTotals, previousTotal ->
            val selectedIndex = periods.indexOfFirst { it.year == period.year && it.month == period.month }
            val dayMap = dailyTotals.associate { it.day to it.total }
            val yearMonth = YearMonth.of(period.year, period.month)
            ThisMonthScreenUiState(
                totalSpendingAmount = totals.totalSpending,
                totalIncomeAmount = totals.totalIncome,
                totalFoodAmount = totals.categoryTotals.amountFor(CATEGORY_KEY_FOOD),
                totalTransportationAmount = totals.categoryTotals.amountFor(CATEGORY_KEY_TRANSPORTATION),
                totalOthersAmount = totals.categoryTotals.amountFor(CATEGORY_KEY_OTHERS),
                totalSpending = formatCurrencyIraqiDinar(totals.totalSpending),
                totalIncome = formatCurrencyIraqiDinar(totals.totalIncome),
                totalSpendingOnFood = formatCompactCurrencyIraqiDinar(totals.categoryTotals.amountFor(CATEGORY_KEY_FOOD)),
                totalSpendingOnOthers = formatCompactCurrencyIraqiDinar(totals.categoryTotals.amountFor(CATEGORY_KEY_OTHERS)),
                totalSpendingOnTransportation = formatCompactCurrencyIraqiDinar(totals.categoryTotals.amountFor(CATEGORY_KEY_TRANSPORTATION)),
                currentMonth = Month.of(period.month).toString().capitalized(),
                selectedMonth = period.month,
                selectedYear = period.year,
                periodLabel = "${Month.of(period.month).toString().capitalized()} ${period.year}",
                availablePeriods = periods.map { monthPeriod ->
                    MonthPeriodOption(
                        year = monthPeriod.year,
                        month = monthPeriod.month,
                        label = "${Month.of(monthPeriod.month).toString().capitalized()} ${monthPeriod.year}",
                    )
                },
                canNavigatePrevious = selectedIndex > 0,
                canNavigateNext = selectedIndex in 0 until periods.lastIndex,
                isCurrentPeriod = period.year == today.year && period.month == today.monthValue,
                spendingTrend = buildMonthTrendPoints(yearMonth, dayMap),
                comparison = buildMonthlyComparison(totals.totalSpending, previousTotal, yearMonth),
                insights = monthInsights(
                    transactions,
                    totals.totalSpending,
                    period.year,
                    period.month,
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
                        year = period.year,
                        month = period.month,
                    )
                },
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = ThisMonthScreenUiState(
            currentMonth = Month.of(today.monthValue).toString().capitalized(),
            selectedMonth = today.monthValue,
            selectedYear = today.year,
            periodLabel = "${Month.of(today.monthValue).toString().capitalized()} ${today.year}",
            isCurrentPeriod = true,
        )
    )

    fun selectPeriod(period: MonthPeriodOption) {
        selectedPeriod.value = MonthPeriod(period.year, period.month)
    }

    fun selectPreviousPeriod() {
        val periods = uiState.value.availablePeriods
        val currentIndex = periods.indexOfFirst {
            it.year == uiState.value.selectedYear && it.month == uiState.value.selectedMonth
        }
        if (currentIndex > 0) {
            selectPeriod(periods[currentIndex - 1])
        }
    }

    fun selectNextPeriod() {
        val periods = uiState.value.availablePeriods
        val currentIndex = periods.indexOfFirst {
            it.year == uiState.value.selectedYear && it.month == uiState.value.selectedMonth
        }
        if (currentIndex in 0 until periods.lastIndex) {
            selectPeriod(periods[currentIndex + 1])
        }
    }

    fun jumpToCurrentPeriod() {
        selectedPeriod.value = MonthPeriod(today.year, today.monthValue)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

private fun List<MonthPeriod>.withCurrentPeriod(current: MonthPeriod): List<MonthPeriod> {
    val combined = (this + current).distinctBy { it.year to it.month }
    return combined.sortedWith(compareBy<MonthPeriod> { it.year }.thenBy { it.month })
}

data class ScreenItemsUiState(
    val isDropDownMenuVisible: Boolean = false,
    val itemHeight: Dp = 0.dp,
    val offSet: DpOffset = DpOffset.Zero,
)

data class MonthPeriodOption(
    val year: Int,
    val month: Int,
    val label: String,
)

private data class MonthTotals(
    val totalSpending: Double,
    val totalIncome: Double,
    val categoryTotals: List<CategorySummaryUi>,
    val categoryNames: Map<String, String>,
)

data class ThisMonthScreenUiState(
    val currentMonth: String = "",
    val periodLabel: String = "",
    val selectedMonth: Int = 0,
    val selectedYear: Int = 0,
    val totalSpendingAmount: Double = 0.0,
    val totalIncomeAmount: Double = 0.0,
    val totalFoodAmount: Double = 0.0,
    val totalTransportationAmount: Double = 0.0,
    val totalOthersAmount: Double = 0.0,
    val totalSpending: String = "",
    val totalIncome: String = "",
    val totalSpendingOnFood: String = "",
    val totalSpendingOnTransportation: String = "",
    val totalSpendingOnOthers: String = "",
    val availablePeriods: List<MonthPeriodOption> = emptyList(),
    val canNavigatePrevious: Boolean = false,
    val canNavigateNext: Boolean = false,
    val isCurrentPeriod: Boolean = false,
    val spendingTrend: List<TrendPointUi> = emptyList(),
    val comparison: ComparisonInsightUi = ComparisonInsightUi(),
    val insights: List<StatInsightUi> = emptyList(),
    val categoryTotals: List<CategorySummaryUi> = emptyList(),
    val recentTransactions: List<HomeTransactionPreview> = emptyList(),
    val screenItemsUiState: MutableState<ScreenItemsUiState> = mutableStateOf(ScreenItemsUiState())
)

data class HomeTransactionPreview(
    val title: String,
    val categoryKey: String,
    val category: String,
    val categoryColorHex: String = "",
    val amount: String,
    val date: String,
    val type: String,
    val year: Int,
    val month: Int,
)
