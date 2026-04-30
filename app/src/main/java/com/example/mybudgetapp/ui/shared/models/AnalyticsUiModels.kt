package com.example.mybudgetapp.ui.shared.models

import androidx.compose.ui.graphics.Color
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.database.BudgetTransaction
import java.time.YearMonth
import kotlin.math.abs

enum class ComparisonDirection {
    Up,
    Down,
    Flat,
}

enum class InsightSection {
    Overview,
    Habits,
}

data class TrendPointUi(
    val label: String,
    val value: Double,
)

data class ComparisonInsightUi(
    val title: String = "Vs last period",
    val summary: String = "",
    val direction: ComparisonDirection = ComparisonDirection.Flat,
)

data class StatInsightUi(
    val title: String,
    val value: String,
    val subtitle: String,
    val isMonetary: Boolean = false,
)

data class CategorySummaryUi(
    val categoryKey: String,
    val categoryLabel: String,
    val total: Double,
    val colorHex: String,
    val iconKey: String,
)

fun List<com.example.mybudgetapp.database.CategorySpendingTotal>.toCategorySummaryUi(): List<CategorySummaryUi> =
    this.map {
        CategorySummaryUi(
            categoryKey = it.categoryKey,
            categoryLabel = it.categoryName,
            total = it.total,
            colorHex = it.colorHex,
            iconKey = it.iconKey,
        )
    }

fun List<CategorySummaryUi>.amountFor(key: String): Double =
    this.firstOrNull { it.categoryKey == key }?.total ?: 0.0

fun buildMonthTrendPoints(daysInMonth: Int, dayTotals: Map<Int, Double>): List<TrendPointUi> =
    (1..daysInMonth).map { day ->
        TrendPointUi(
            label = day.toString(),
            value = dayTotals[day] ?: 0.0
        )
    }

fun buildYearTrendPoints(monthTotals: Map<Int, Double>): List<TrendPointUi> =
    (1..12).map { month ->
        TrendPointUi(
            label = java.time.Month.of(month).name.take(3).lowercase().capitalized(),
            value = monthTotals[month] ?: 0.0
        )
    }

fun buildMonthlyComparison(currentTotal: Double, previousTotal: Double, yearMonth: YearMonth): ComparisonInsightUi {
    return buildComparisonInsight(
        title = "Vs last month",
        currentTotal = currentTotal,
        previousTotal = previousTotal
    )
}

fun buildYearlyComparison(currentTotal: Double, previousTotal: Double, year: Int): ComparisonInsightUi {
    return buildComparisonInsight(
        title = "Vs last year",
        currentTotal = currentTotal,
        previousTotal = previousTotal
    )
}

private fun buildComparisonInsight(title: String, currentTotal: Double, previousTotal: Double): ComparisonInsightUi {
    if (previousTotal == 0.0 && currentTotal == 0.0) {
        return ComparisonInsightUi(title = title, summary = "No spending", direction = ComparisonDirection.Flat)
    }
    if (previousTotal == 0.0) {
        return ComparisonInsightUi(title = title, summary = "Started spending", direction = ComparisonDirection.Up)
    }
    val delta = currentTotal - previousTotal
    if (delta == 0.0) {
        return ComparisonInsightUi(title = title, summary = "No change", direction = ComparisonDirection.Flat)
    }
    val percent = abs(delta / previousTotal) * 100
    val summary = String.format("%.0f%% %s", percent, if (delta > 0) "higher" else "lower")
    return ComparisonInsightUi(
        title = title,
        summary = summary,
        direction = if (delta > 0) ComparisonDirection.Up else ComparisonDirection.Down,
    )
}

fun categoryLabel(key: String, names: Map<String, String>): String =
    names[key] ?: key.capitalized()

fun monthInsights(
    expenseTransactions: List<BudgetTransaction>,
    totalSpending: Double,
    year: Int,
    month: Int,
    categoryNames: Map<String, String> = emptyMap(),
): List<StatInsightUi> {
    val yearMonth = YearMonth.of(year, month)
    val topCategory = expenseTransactions
        .groupBy { it.category }
        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
    val biggestExpense = expenseTransactions.maxByOrNull { it.amount }
    val spendingDays = expenseTransactions.map { it.transactionDate }.distinct().size
    val averageTransaction = if (expenseTransactions.isEmpty()) 0.0 else totalSpending / expenseTransactions.size
    val averageDaily = if (expenseTransactions.isEmpty()) 0.0 else totalSpending / yearMonth.lengthOfMonth()

    return listOf(
        StatInsightUi(
            title = "Top category",
            value = topCategory?.let { categoryLabel(it.key, categoryNames) } ?: "None",
            subtitle = topCategory?.let { formatCompactCurrencyIraqiDinar(it.value.sumOf { tx -> tx.amount }) } ?: "",
        ),
        StatInsightUi(
            title = "Biggest expense",
            value = biggestExpense?.let { formatCompactCurrencyIraqiDinar(it.amount) } ?: formatCompactCurrencyIraqiDinar(0.0),
            subtitle = biggestExpense?.title ?: biggestExpense?.let { categoryLabel(it.category, categoryNames) } ?: "No expenses",
            isMonetary = true,
        ),
        StatInsightUi(
            title = "Spending days",
            value = spendingDays.toString(),
            subtitle = "Days with at least one expense",
        ),
        StatInsightUi(
            title = "Avg daily spend",
            value = formatCompactCurrencyIraqiDinar(averageDaily),
            subtitle = "Across ${yearMonth.lengthOfMonth()} days",
            isMonetary = true,
        ),
        StatInsightUi(
            title = "Avg transaction",
            value = formatCompactCurrencyIraqiDinar(averageTransaction),
            subtitle = if (expenseTransactions.isEmpty()) "No expenses" else "Across ${expenseTransactions.size} expenses",
            isMonetary = true,
        ),
    )
}

fun yearInsights(
    expenseTransactions: List<BudgetTransaction>,
    totalSpending: Double,
    year: Int,
    categoryNames: Map<String, String> = emptyMap(),
): List<StatInsightUi> {
    val topCategory = expenseTransactions
        .groupBy { it.category }
        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
    val biggestExpense = expenseTransactions.maxByOrNull { it.amount }
    val activeMonths = expenseTransactions
        .map { it.transactionDate.take(7) }
        .distinct()
        .size
    val averageMonthly = totalSpending / 12.0
    val averageTransaction = if (expenseTransactions.isEmpty()) 0.0 else totalSpending / expenseTransactions.size

    return listOf(
        StatInsightUi(
            title = "Top category",
            value = topCategory?.let { categoryLabel(it.key, categoryNames) } ?: "None",
            subtitle = topCategory?.let { formatCompactCurrencyIraqiDinar(it.value.sumOf { tx -> tx.amount }) } ?: "",
        ),
        StatInsightUi(
            title = "Biggest expense",
            value = biggestExpense?.let { formatCompactCurrencyIraqiDinar(it.amount) } ?: formatCompactCurrencyIraqiDinar(0.0),
            subtitle = biggestExpense?.title ?: biggestExpense?.let { categoryLabel(it.category, categoryNames) } ?: "No expenses",
            isMonetary = true,
        ),
        StatInsightUi(
            title = "Active months",
            value = activeMonths.toString(),
            subtitle = "Months with spending in $year",
        ),
        StatInsightUi(
            title = "Avg monthly spend",
            value = formatCompactCurrencyIraqiDinar(averageMonthly),
            subtitle = "Across 12 months",
            isMonetary = true,
        ),
        StatInsightUi(
            title = "Avg transaction",
            value = formatCompactCurrencyIraqiDinar(averageTransaction),
            subtitle = if (expenseTransactions.isEmpty()) "No expenses" else "Across ${expenseTransactions.size} expenses",
            isMonetary = true,
        ),
    )
}

fun comparisonColor(direction: ComparisonDirection): Color = when (direction) {
    ComparisonDirection.Up -> Color(0xFFB3261E)
    ComparisonDirection.Down -> Color(0xFF2E7D32)
    ComparisonDirection.Flat -> Color(0xFF5F6368)
}
