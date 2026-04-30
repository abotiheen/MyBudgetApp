package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.domain.insights.InsightPeriod
import com.example.mybudgetapp.domain.insights.InsightPeriodScope
import com.example.mybudgetapp.domain.insights.InsightSettings
import com.example.mybudgetapp.domain.insights.InsightSeverity
import com.example.mybudgetapp.domain.insights.InsightStatus
import com.example.mybudgetapp.domain.insights.InsightUi
import com.example.mybudgetapp.domain.insights.InsightsEngine
import com.example.mybudgetapp.domain.insights.PeriodTransactions
import com.example.mybudgetapp.ui.screens.InsightsDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

class InsightsViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle,
    private val insightsEngine: InsightsEngine = InsightsEngine(),
) : ViewModel() {

    private val scope: String = checkNotNull(savedStateHandle[InsightsDestination.scope])
    private val year: Int = checkNotNull(savedStateHandle[InsightsDestination.year])
    private val month: Int = checkNotNull(savedStateHandle[InsightsDestination.month])
    private val selectedCategoryKeys = MutableStateFlow<Set<String>?>(null)

    val uiState: StateFlow<InsightsUiState> = if (scope == InsightScope.Year.routeValue) {
        yearUiState()
    } else {
        monthUiState()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = InsightsUiState(isLoading = true),
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

    private fun monthUiState(): Flow<InsightsUiState> {
        val selectedMonth = month.coerceIn(1, 12)
        val selectedYearMonth = YearMonth.of(year, selectedMonth)
        return combine(
            itemRepository.getCategoryTotalsByType(
                type = TRANSACTION_TYPE_EXPENSE,
                year = year,
                month = selectedMonth,
                includeArchived = true,
            ),
            itemRepository.getAllCategories(includeArchived = true),
            itemRepository.getTransactions(selectedMonth, year),
            previousMonthPeriods(selectedYearMonth),
            selectedCategoryKeys,
        ) { categoryTotals, categories, currentTransactions, previousPeriods, storedSelectedKeys ->
            val summaries = categoryTotals.toCategorySummaryUi()
            val availableCategories = summaries
                .filter { it.total > 0.0 && !it.isArchived }
                .map { it.toInsightFilterCategoryUi() }
            val resolvedSelectedKeys = resolveSelectedCategoryKeys(
                availableCategoryKeys = availableCategories.mapTo(linkedSetOf()) { it.categoryKey },
                storedSelectedKeys = storedSelectedKeys,
            )
            val categoryNames = categories.associate { it.categoryKey to it.name }
            val filteredTransactions = currentTransactions.filterByCategories(resolvedSelectedKeys)
            val filteredPreviousPeriods = previousPeriods.map { period ->
                period.copy(transactions = period.transactions.filterByCategories(resolvedSelectedKeys))
            }
            val filteredCategoryTotals = summaries.filter {
                it.categoryKey in resolvedSelectedKeys && it.total > 0.0
            }

            buildInsightsUiState(
                periodLabel = "${Month.of(selectedMonth).name.capitalized()} $year",
                scope = InsightScope.Month,
                selectedPeriod = InsightPeriod(
                    scope = InsightPeriodScope.Month,
                    year = year,
                    month = selectedMonth,
                ),
                currentTransactions = filteredTransactions,
                previousPeriods = filteredPreviousPeriods,
                categoryTotals = filteredCategoryTotals,
                categoryNames = categoryNames,
                availableCategories = availableCategories,
                selectedCategoryKeys = resolvedSelectedKeys,
            )
        }
    }

    private fun yearUiState(): Flow<InsightsUiState> {
        return combine(
            itemRepository.getCategoryTotalsByTypeForYear(
                type = TRANSACTION_TYPE_EXPENSE,
                year = year,
                includeArchived = true,
            ),
            itemRepository.getAllCategories(includeArchived = true),
            itemRepository.getTransactionsForYear(year),
            itemRepository.getTransactionsForYear(year - 1),
            selectedCategoryKeys,
        ) { categoryTotals, categories, currentTransactions, previousTransactions, storedSelectedKeys ->
            val summaries = categoryTotals.toCategorySummaryUi()
            val availableCategories = summaries
                .filter { it.total > 0.0 && !it.isArchived }
                .map { it.toInsightFilterCategoryUi() }
            val resolvedSelectedKeys = resolveSelectedCategoryKeys(
                availableCategoryKeys = availableCategories.mapTo(linkedSetOf()) { it.categoryKey },
                storedSelectedKeys = storedSelectedKeys,
            )
            val categoryNames = categories.associate { it.categoryKey to it.name }
            val filteredTransactions = currentTransactions.filterByCategories(resolvedSelectedKeys)
            val filteredPreviousTransactions = previousTransactions.filterByCategories(resolvedSelectedKeys)
            val filteredCategoryTotals = summaries.filter {
                it.categoryKey in resolvedSelectedKeys && it.total > 0.0
            }

            buildInsightsUiState(
                periodLabel = year.toString(),
                scope = InsightScope.Year,
                selectedPeriod = InsightPeriod(
                    scope = InsightPeriodScope.Year,
                    year = year,
                ),
                currentTransactions = filteredTransactions,
                previousPeriods = listOf(
                    PeriodTransactions(
                        period = InsightPeriod(
                            scope = InsightPeriodScope.Year,
                            year = year - 1,
                        ),
                        transactions = filteredPreviousTransactions,
                    )
                ),
                categoryTotals = filteredCategoryTotals,
                categoryNames = categoryNames,
                availableCategories = availableCategories,
                selectedCategoryKeys = resolvedSelectedKeys,
            )
        }
    }

    private fun previousMonthPeriods(selected: YearMonth): Flow<List<PeriodTransactions>> {
        val periods = (1..6).map { selected.minusMonths(it.toLong()) }
        if (periods.isEmpty()) return flowOf(emptyList())
        val flows = periods.map { period ->
            itemRepository.getTransactions(period.monthValue, period.year)
        }
        return combine(flows) { transactionsByPeriod ->
            transactionsByPeriod.mapIndexed { index, transactions ->
                val period = periods[index]
                PeriodTransactions(
                    period = InsightPeriod(
                        scope = InsightPeriodScope.Month,
                        year = period.year,
                        month = period.monthValue,
                    ),
                    transactions = transactions,
                )
            }
        }
    }

    private fun buildInsightsUiState(
        periodLabel: String,
        scope: InsightScope,
        selectedPeriod: InsightPeriod,
        currentTransactions: List<BudgetTransaction>,
        previousPeriods: List<PeriodTransactions>,
        categoryTotals: List<CategorySummaryUi>,
        categoryNames: Map<String, String>,
        availableCategories: List<InsightFilterCategoryUi>,
        selectedCategoryKeys: Set<String>,
    ): InsightsUiState {
        val hasNoIncludedCategories = availableCategories.isNotEmpty() && selectedCategoryKeys.isEmpty()
        val totalSpendingAmount = currentTransactions.sumOf { it.amount }
        val trendPoints = when (scope) {
            InsightScope.Month -> buildMonthTrendPointsFromTransactions(
                yearMonth = YearMonth.of(selectedPeriod.year, selectedPeriod.month ?: 1),
                transactions = currentTransactions,
            )
            InsightScope.Year -> buildYearTrendPointsFromTransactions(currentTransactions)
        }
        val engineResult = if (hasNoIncludedCategories) {
            null
        } else {
            insightsEngine.generateInsights(
                transactions = currentTransactions,
                selectedPeriod = selectedPeriod,
                previousPeriods = previousPeriods,
                settings = InsightSettings(
                    today = LocalDate.now(),
                    currencyFormatter = ::formatCompactCurrencyIraqiDinar,
                ),
                categoryLabels = categoryNames,
            )
        }
        val insights = engineResult?.insights.orEmpty()
        val savingOpportunity = insights.filterIsInstance<InsightUi.SavingOpportunity>().firstOrNull()
        val primaryInsights = insights
            .filterNot { it is InsightUi.SavingOpportunity }
            .take(5)
        val primaryIds = primaryInsights.mapTo(mutableSetOf()) { it.id }
        val secondaryInsights = insights
            .filterNot { it.id in primaryIds }
            .filterNot { it == savingOpportunity }
        val emptyState = when {
            hasNoIncludedCategories -> InsightsEmptyState(
                title = "No categories included",
                message = "Choose at least one category to restore insight totals, patterns, and breakdowns.",
                actionLabel = "Choose categories",
            )
            totalSpendingAmount == 0.0 -> InsightsEmptyState(
                title = "No spending yet",
                message = "Add expenses for this period and Insights V2 will start explaining what changed.",
                actionLabel = "Manage categories",
            )
            else -> null
        }

        return InsightsUiState(
            isLoading = false,
            periodLabel = periodLabel,
            totalSpendingAmount = totalSpendingAmount,
            totalSpending = formatCompactCurrencyIraqiDinar(totalSpendingAmount),
            scope = scope,
            status = engineResult?.status ?: InsightStatus(
                label = if (hasNoIncludedCategories) "Filtered out" else "No spending yet",
                explanation = if (hasNoIncludedCategories) {
                    "All categories are excluded from this view."
                } else {
                    "Add expenses for this period and insights will appear here."
                },
                severity = InsightSeverity.Neutral,
            ),
            primaryInsights = primaryInsights,
            secondaryInsights = secondaryInsights,
            savingOpportunity = savingOpportunity,
            categoryTotals = categoryTotals.map { summary ->
                CategoryTotalUi(
                    category = summary.categoryKey,
                    label = summary.categoryLabel,
                    total = summary.total,
                    iconKey = summary.iconKey,
                    colorHex = summary.colorHex,
                )
            },
            trendTitle = if (scope == InsightScope.Month) "Daily spending trend" else "Monthly spending trend",
            trendSubtitle = if (scope == InsightScope.Month) {
                "See which days pushed spending up."
            } else {
                "See which months carried the most weight."
            },
            trendAnnotation = engineResult?.trendAnnotation
                ?: "The trend will become clearer when spending is included.",
            trendPoints = trendPoints,
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
            emptyState = emptyState,
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

data class InsightsEmptyState(
    val title: String,
    val message: String,
    val actionLabel: String,
)

data class InsightsUiState(
    val isLoading: Boolean = false,
    val periodLabel: String = "",
    val totalSpendingAmount: Double = 0.0,
    val totalSpending: String = "",
    val scope: InsightScope = InsightScope.Month,
    val status: InsightStatus = InsightStatus(
        label = "Loading",
        explanation = "Preparing insights.",
        severity = InsightSeverity.Neutral,
    ),
    val primaryInsights: List<InsightUi> = emptyList(),
    val secondaryInsights: List<InsightUi> = emptyList(),
    val savingOpportunity: InsightUi.SavingOpportunity? = null,
    val categoryTotals: List<CategoryTotalUi> = emptyList(),
    val trendTitle: String = "",
    val trendSubtitle: String = "",
    val trendAnnotation: String = "",
    val trendPoints: List<TrendPointUi> = emptyList(),
    val availableCategories: List<InsightFilterCategoryUi> = emptyList(),
    val selectedCategoryKeys: Set<String> = emptySet(),
    val isFilterActive: Boolean = false,
    val hasNoIncludedCategories: Boolean = false,
    val selectedCategoriesSummary: String = "",
    val emptyState: InsightsEmptyState? = null,
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
        .mapNotNull { transaction ->
            runCatching { LocalDate.parse(transaction.transactionDate).dayOfMonth to transaction.amount }.getOrNull()
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, amounts) -> amounts.sum() }
    return buildMonthTrendPoints(yearMonth, totalsByDay)
}

private fun buildYearTrendPointsFromTransactions(
    transactions: List<BudgetTransaction>,
): List<TrendPointUi> {
    val totalsByMonth = transactions
        .mapNotNull { transaction ->
            runCatching { LocalDate.parse(transaction.transactionDate).monthValue to transaction.amount }.getOrNull()
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, amounts) -> amounts.sum() }
    return buildYearTrendPoints(totalsByMonth)
}
