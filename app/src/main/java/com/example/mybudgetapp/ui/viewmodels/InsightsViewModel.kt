package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.ui.screens.InsightsDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
            itemRepository.getCategoryTotalsByTypeForYear(TRANSACTION_TYPE_EXPENSE, year, includeArchived = true),
            itemRepository.getAllCategories(includeArchived = true),
        ) { totalSpending, previousYearTotal, categoryTotals, categories ->
            YearInsightTotals(
                totalSpending = totalSpending,
                previousYearTotal = previousYearTotal,
                categoryTotals = categoryTotals.toCategorySummaryUi(),
                categoryNames = categories.associate { it.categoryKey to it.name },
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
                overviewInsights = yearInsights(transactions, totals.totalSpending, year, totals.categoryNames).take(3),
                habitInsights = yearInsights(transactions, totals.totalSpending, year, totals.categoryNames).drop(2),
                categoryTotals = totals.categoryTotals.map { summary ->
                    CategoryTotalUi(
                        category = summary.categoryKey,
                        label = summary.categoryLabel,
                        total = summary.total,
                        iconKey = summary.iconKey,
                        colorHex = summary.colorHex,
                    )
                },
            )
        }
    } else {
        val selectedMonth = month.coerceIn(1, 12)
        val previousPeriod = YearMonth.of(year, selectedMonth).minusMonths(1)
        val totalsFlow = combine(
            itemRepository.getTotalSpendingOverall(year, selectedMonth),
            itemRepository.getTotalSpendingOverall(previousPeriod.year, previousPeriod.monthValue),
            itemRepository.getCategoryTotalsByType(TRANSACTION_TYPE_EXPENSE, year, selectedMonth, includeArchived = true),
            itemRepository.getAllCategories(includeArchived = true),
        ) { totalSpending, previousMonthTotal, categoryTotals, categories ->
            MonthInsightTotals(
                totalSpending = totalSpending,
                previousMonthTotal = previousMonthTotal,
                categoryTotals = categoryTotals.toCategorySummaryUi(),
                categoryNames = categories.associate { it.categoryKey to it.name },
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
                trendPoints = buildMonthTrendPoints(yearMonth, dayMap),
                comparison = buildMonthlyComparison(totals.totalSpending, totals.previousMonthTotal, yearMonth),
                overviewInsights = monthInsights(transactions, totals.totalSpending, year, selectedMonth, totals.categoryNames).take(3),
                habitInsights = monthInsights(transactions, totals.totalSpending, year, selectedMonth, totals.categoryNames).drop(2),
                categoryTotals = totals.categoryTotals.map { summary ->
                    CategoryTotalUi(
                        category = summary.categoryKey,
                        label = summary.categoryLabel,
                        total = summary.total,
                        iconKey = summary.iconKey,
                        colorHex = summary.colorHex,
                    )
                },
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
    val label: String = "",
    val total: Double,
    val iconKey: String = "",
    val colorHex: String = "",
)

private data class YearInsightTotals(
    val totalSpending: Double,
    val previousYearTotal: Double,
    val categoryTotals: List<CategorySummaryUi>,
    val categoryNames: Map<String, String>,
)

private data class MonthInsightTotals(
    val totalSpending: Double,
    val previousMonthTotal: Double,
    val categoryTotals: List<CategorySummaryUi>,
    val categoryNames: Map<String, String>,
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
