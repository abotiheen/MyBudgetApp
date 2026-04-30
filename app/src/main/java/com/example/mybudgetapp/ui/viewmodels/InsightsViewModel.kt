package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.ui.screens.InsightsDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

class InsightsViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val scope: String = checkNotNull(savedStateHandle[InsightsDestination.scope])
    private val year: Int = checkNotNull(savedStateHandle[InsightsDestination.year])
    private val month: Int = checkNotNull(savedStateHandle[InsightsDestination.month])
    private val selectedCategoryKeys = MutableStateFlow<Set<String>?>(null)

    val uiState: StateFlow<InsightsUiState> = if (scope == InsightScope.Year.routeValue) {
        combine(
            itemRepository.getCategoryTotalsByTypeForYear(TRANSACTION_TYPE_EXPENSE, year, includeArchived = true),
            itemRepository.getAllCategories(includeArchived = true),
            itemRepository.getTransactionsForYear(year),
            itemRepository.getTransactionsForYear(year - 1),
            selectedCategoryKeys,
        ) { categoryTotals, categories, currentTransactions, previousTransactions, storedSelectedKeys ->
            val summaries = categoryTotals.toCategorySummaryUi()
            val categoryNames = categories.associate { it.categoryKey to it.name }
            val availableCategories = summaries
                .filter { it.total > 0.0 && !it.isArchived }
                .map { it.toInsightFilterCategoryUi() }
            val resolvedSelectedKeys = resolveSelectedCategoryKeys(
                availableCategoryKeys = availableCategories.mapTo(linkedSetOf()) { it.categoryKey },
                storedSelectedKeys = storedSelectedKeys,
            )
            val filteredTransactions = currentTransactions.filterByCategories(resolvedSelectedKeys)
            val filteredPreviousTransactions = previousTransactions.filterByCategories(resolvedSelectedKeys)
            val filteredCategoryTotals = summaries.filter { it.categoryKey in resolvedSelectedKeys && it.total > 0.0 }
            val totalSpendingAmount = filteredTransactions.sumOf { it.amount }
            buildInsightsUiState(
                periodLabel = year.toString(),
                scope = InsightScope.Year,
                totalSpendingAmount = totalSpendingAmount,
                comparison = buildYearlyComparison(
                    currentTotal = totalSpendingAmount,
                    previousTotal = filteredPreviousTransactions.sumOf { it.amount },
                    year = year,
                ),
                trendTitle = "Monthly spending trend",
                trendSubtitle = "See how spending moved across the year",
                trendPoints = buildYearTrendPointsFromTransactions(filteredTransactions),
                allInsights = yearInsights(filteredTransactions, totalSpendingAmount, year, categoryNames),
                categoryTotals = filteredCategoryTotals,
                availableCategories = availableCategories,
                selectedCategoryKeys = resolvedSelectedKeys,
            )
        }
    } else {
        val selectedMonth = month.coerceIn(1, 12)
        val previousPeriod = YearMonth.of(year, selectedMonth).minusMonths(1)
        combine(
            itemRepository.getCategoryTotalsByType(TRANSACTION_TYPE_EXPENSE, year, selectedMonth, includeArchived = true),
            itemRepository.getAllCategories(includeArchived = true),
            itemRepository.getTransactions(selectedMonth, year),
            itemRepository.getTransactions(previousPeriod.monthValue, previousPeriod.year),
            selectedCategoryKeys,
        ) { categoryTotals, categories, currentTransactions, previousTransactions, storedSelectedKeys ->
            val summaries = categoryTotals.toCategorySummaryUi()
            val categoryNames = categories.associate { it.categoryKey to it.name }
            val availableCategories = summaries
                .filter { it.total > 0.0 && !it.isArchived }
                .map { it.toInsightFilterCategoryUi() }
            val resolvedSelectedKeys = resolveSelectedCategoryKeys(
                availableCategoryKeys = availableCategories.mapTo(linkedSetOf()) { it.categoryKey },
                storedSelectedKeys = storedSelectedKeys,
            )
            val filteredTransactions = currentTransactions.filterByCategories(resolvedSelectedKeys)
            val filteredPreviousTransactions = previousTransactions.filterByCategories(resolvedSelectedKeys)
            val filteredCategoryTotals = summaries.filter { it.categoryKey in resolvedSelectedKeys && it.total > 0.0 }
            val totalSpendingAmount = filteredTransactions.sumOf { it.amount }
            val yearMonth = YearMonth.of(year, selectedMonth)
            buildInsightsUiState(
                periodLabel = "${Month.of(selectedMonth).name.capitalized()} $year",
                scope = InsightScope.Month,
                totalSpendingAmount = totalSpendingAmount,
                comparison = buildMonthlyComparison(
                    currentTotal = totalSpendingAmount,
                    previousTotal = filteredPreviousTransactions.sumOf { it.amount },
                    selected = yearMonth,
                ),
                trendTitle = "Daily spending trend",
                trendSubtitle = "See which days pushed spending up",
                trendPoints = buildMonthTrendPointsFromTransactions(yearMonth, filteredTransactions),
                allInsights = monthInsights(filteredTransactions, totalSpendingAmount, year, selectedMonth, categoryNames),
                categoryTotals = filteredCategoryTotals,
                availableCategories = availableCategories,
                selectedCategoryKeys = resolvedSelectedKeys,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = InsightsUiState(),
    )

    fun toggleCategory(categoryKey: String) {
        val current = uiState.value.selectedCategoryKeys
        selectedCategoryKeys.value = if (categoryKey in current) {
            current - categoryKey
        } else {
            current + categoryKey
        }
    }

    fun selectAllCategories() {
        selectedCategoryKeys.value = uiState.value.availableCategories.mapTo(linkedSetOf()) { it.categoryKey }
    }

    fun clearAllCategories() {
        selectedCategoryKeys.value = emptySet()
    }

    private fun buildInsightsUiState(
        periodLabel: String,
        scope: InsightScope,
        totalSpendingAmount: Double,
        comparison: ComparisonInsightUi,
        trendTitle: String,
        trendSubtitle: String,
        trendPoints: List<TrendPointUi>,
        allInsights: List<StatInsightUi>,
        categoryTotals: List<CategorySummaryUi>,
        availableCategories: List<InsightFilterCategoryUi>,
        selectedCategoryKeys: Set<String>,
    ): InsightsUiState {
        val hasNoIncludedCategories = availableCategories.isNotEmpty() && selectedCategoryKeys.isEmpty()
        return InsightsUiState(
            periodLabel = periodLabel,
            totalSpendingAmount = totalSpendingAmount,
            totalSpending = formatCompactCurrencyIraqiDinar(totalSpendingAmount),
            scope = scope,
            trendTitle = trendTitle,
            trendSubtitle = trendSubtitle,
            trendPoints = trendPoints,
            comparison = if (hasNoIncludedCategories) {
                ComparisonInsightUi(
                    title = comparison.title,
                    summary = "No categories included",
                    direction = ComparisonDirection.Flat,
                )
            } else {
                comparison
            },
            overviewInsights = allInsights.take(3),
            habitInsights = allInsights.drop(2),
            categoryTotals = categoryTotals.map { summary ->
                CategoryTotalUi(
                    category = summary.categoryKey,
                    label = summary.categoryLabel,
                    total = summary.total,
                    iconKey = summary.iconKey,
                    colorHex = summary.colorHex,
                )
            },
            availableCategories = availableCategories.map { category ->
                category.copy(isSelected = category.categoryKey in selectedCategoryKeys)
            },
            selectedCategoryKeys = selectedCategoryKeys,
            isFilterActive = availableCategories.isNotEmpty() && selectedCategoryKeys.size < availableCategories.size,
            hasNoIncludedCategories = hasNoIncludedCategories,
            selectedCategoriesSummary = when {
                availableCategories.isEmpty() -> "No categories"
                selectedCategoryKeys.isEmpty() -> "No categories"
                selectedCategoryKeys.size == availableCategories.size -> "All categories"
                else -> "${selectedCategoryKeys.size} selected"
            },
        )
    }

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

data class InsightFilterCategoryUi(
    val categoryKey: String,
    val label: String,
    val totalAmount: Double,
    val totalLabel: String,
    val iconKey: String,
    val colorHex: String,
    val isSelected: Boolean = false,
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
    val availableCategories: List<InsightFilterCategoryUi> = emptyList(),
    val selectedCategoryKeys: Set<String> = emptySet(),
    val isFilterActive: Boolean = false,
    val hasNoIncludedCategories: Boolean = false,
    val selectedCategoriesSummary: String = "",
)

private fun CategorySummaryUi.toInsightFilterCategoryUi(): InsightFilterCategoryUi = InsightFilterCategoryUi(
    categoryKey = categoryKey,
    label = categoryLabel,
    totalAmount = total,
    totalLabel = formatCompactCurrencyIraqiDinar(total),
    iconKey = iconKey,
    colorHex = colorHex,
)

private fun resolveSelectedCategoryKeys(
    availableCategoryKeys: Set<String>,
    storedSelectedKeys: Set<String>?,
): Set<String> = when {
    availableCategoryKeys.isEmpty() -> emptySet()
    storedSelectedKeys == null -> availableCategoryKeys
    else -> storedSelectedKeys.intersect(availableCategoryKeys)
}

private fun List<BudgetTransaction>.filterByCategories(selectedCategoryKeys: Set<String>): List<BudgetTransaction> =
    filter { it.category in selectedCategoryKeys }

private fun buildMonthTrendPointsFromTransactions(
    yearMonth: YearMonth,
    transactions: List<BudgetTransaction>,
): List<TrendPointUi> {
    val totalsByDay = transactions
        .groupBy { LocalDate.parse(it.transactionDate).dayOfMonth }
        .mapValues { (_, dailyTransactions) -> dailyTransactions.sumOf { it.amount } }
    return buildMonthTrendPoints(yearMonth, totalsByDay)
}

private fun buildYearTrendPointsFromTransactions(
    transactions: List<BudgetTransaction>,
): List<TrendPointUi> {
    val totalsByMonth = transactions
        .groupBy { LocalDate.parse(it.transactionDate).monthValue }
        .mapValues { (_, monthlyTransactions) -> monthlyTransactions.sumOf { it.amount } }
    return buildYearTrendPoints(totalsByMonth)
}
