package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.ui.screens.InsightsDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Month
import java.time.YearMonth

class InsightsViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val scope: String = checkNotNull(savedStateHandle[InsightsDestination.scope])
    private val year: Int = checkNotNull(savedStateHandle[InsightsDestination.year])
    private val month: Int = checkNotNull(savedStateHandle[InsightsDestination.month])

    val uiState: StateFlow<InsightsUiState> = if (scope == InsightScope.Year.routeValue) {
        val totalsFlow = combine(
            itemRepository.getTotalSpendingOverallForYear(year),
            itemRepository.getTotalSpendingOverallForYear(year - 1),
            itemRepository.getTotalSpendingOnCategoryForYear("food", year),
            itemRepository.getTotalSpendingOnCategoryForYear("transportation", year),
            itemRepository.getTotalSpendingOnCategoryForYear("others", year),
        ) { totalSpending, previousYearTotal, food, transportation, others ->
            YearInsightTotals(
                totalSpending = totalSpending,
                previousYearTotal = previousYearTotal,
                food = food,
                transportation = transportation,
                others = others,
            )
        }
        combine(
            totalsFlow,
            itemRepository.getTransactionsForYear(year),
            itemRepository.getMonthlySpendingTotals(year),
        ) { totals, transactions, monthlyTotals ->
            val monthMap = monthlyTotals.associate { it.month to it.total }
            InsightsUiState(
                periodLabel = year.toString(),
                totalSpendingAmount = totals.totalSpending,
                totalSpending = formatCompactCurrencyIraqiDinar(totals.totalSpending),
                scope = InsightScope.Year,
                trendTitle = "Monthly spending trend",
                trendSubtitle = "See how spending moved across the year",
                trendPoints = buildYearTrendPoints(monthMap),
                comparison = buildYearlyComparison(totals.totalSpending, totals.previousYearTotal, year),
                overviewInsights = yearInsights(transactions, totals.totalSpending, year).take(3),
                habitInsights = yearInsights(transactions, totals.totalSpending, year).drop(2),
                categoryTotals = listOf(
                    CategoryTotalUi("food", totals.food),
                    CategoryTotalUi("transportation", totals.transportation),
                    CategoryTotalUi("others", totals.others),
                ),
            )
        }
    } else {
        val selectedMonth = month.coerceIn(1, 12)
        val previousPeriod = YearMonth.of(year, selectedMonth).minusMonths(1)
        val totalsFlow = combine(
            itemRepository.getTotalSpendingOverall(year, selectedMonth),
            itemRepository.getTotalSpendingOverall(previousPeriod.year, previousPeriod.monthValue),
            itemRepository.getTotalSpendingOnCategory("food", year, selectedMonth),
            itemRepository.getTotalSpendingOnCategory("transportation", year, selectedMonth),
            itemRepository.getTotalSpendingOnCategory("others", year, selectedMonth),
        ) { totalSpending, previousMonthTotal, food, transportation, others ->
            MonthInsightTotals(
                totalSpending = totalSpending,
                previousMonthTotal = previousMonthTotal,
                food = food,
                transportation = transportation,
                others = others,
            )
        }
        combine(
            totalsFlow,
            itemRepository.getTransactions(selectedMonth, year),
            itemRepository.getDailySpendingTotals(selectedMonth, year),
        ) { totals, transactions, dailyTotals ->
            val dayMap = dailyTotals.associate { it.day to it.total }
            val yearMonth = YearMonth.of(year, selectedMonth)
            InsightsUiState(
                periodLabel = "${Month.of(selectedMonth).name.capitalized()} $year",
                totalSpendingAmount = totals.totalSpending,
                totalSpending = formatCompactCurrencyIraqiDinar(totals.totalSpending),
                scope = InsightScope.Month,
                trendTitle = "Daily spending trend",
                trendSubtitle = "See which days pushed spending up",
                trendPoints = buildMonthTrendPoints(yearMonth.lengthOfMonth(), dayMap),
                comparison = buildMonthlyComparison(totals.totalSpending, totals.previousMonthTotal, yearMonth),
                overviewInsights = monthInsights(transactions, totals.totalSpending, year, selectedMonth).take(3),
                habitInsights = monthInsights(transactions, totals.totalSpending, year, selectedMonth).drop(2),
                categoryTotals = listOf(
                    CategoryTotalUi("food", totals.food),
                    CategoryTotalUi("transportation", totals.transportation),
                    CategoryTotalUi("others", totals.others),
                ),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = InsightsUiState(),
    )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

enum class InsightScope(val routeValue: String) {
    Month("month"),
    Year("year"),
}

enum class InsightSection {
    Overview,
    Trend,
    Habits,
    Categories,
}

data class CategoryTotalUi(
    val category: String,
    val total: Double,
)

private data class YearInsightTotals(
    val totalSpending: Double,
    val previousYearTotal: Double,
    val food: Double,
    val transportation: Double,
    val others: Double,
)

private data class MonthInsightTotals(
    val totalSpending: Double,
    val previousMonthTotal: Double,
    val food: Double,
    val transportation: Double,
    val others: Double,
)

data class InsightsUiState(
    val periodLabel: String = "",
    val totalSpendingAmount: Double = 0.0,
    val totalSpending: String = "",
    val scope: InsightScope = InsightScope.Month,
    val trendTitle: String = "",
    val trendSubtitle: String = "",
    val trendPoints: List<TrendPointUi> = emptyList(),
    val comparison: ComparisonInsightUi = ComparisonInsightUi(),
    val overviewInsights: List<StatInsightUi> = emptyList(),
    val habitInsights: List<StatInsightUi> = emptyList(),
    val categoryTotals: List<CategoryTotalUi> = emptyList(),
)
