package com.example.mybudgetapp.ui.viewmodels

import androidx.compose.ui.graphics.Color
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.CategorySpendingTotal
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import java.time.YearMonth
import kotlin.math.abs

enum class ComparisonDirection {
    Up,
    Down,
    Flat,
}

data class TrendPointUi(
    val label: String,
    val value: Double,
    val detailLabel: String = label,
)

data class ComparisonInsightUi(
    val title: String = "",
    val summary: String = "",
    val direction: ComparisonDirection = ComparisonDirection.Flat,
)

data class StatInsightUi(
    val title: String,
    val value: String,
    val subtitle: String = "",
    val isMonetary: Boolean = false,
)

fun categoryLabel(
    category: String,
    categoryNames: Map<String, String> = emptyMap(),
): String = categoryNames[category] ?: category.capitalized()

data class CategorySummaryUi(
    val categoryKey: String,
    val categoryLabel: String,
    val total: Double,
    val type: String,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean,
)

fun List<CategorySpendingTotal>.toCategorySummaryUi(): List<CategorySummaryUi> = map { category ->
    CategorySummaryUi(
        categoryKey = category.categoryKey,
        categoryLabel = category.categoryName,
        total = category.total,
        type = category.type,
        iconKey = category.iconKey,
        colorHex = category.colorHex,
        isArchived = category.isArchived,
    )
}

fun List<CategorySummaryUi>.amountFor(categoryKey: String): Double =
    firstOrNull { it.categoryKey == categoryKey }?.total ?: 0.0

fun buildMonthTrendPoints(yearMonth: YearMonth, values: Map<Int, Double>): List<TrendPointUi> {
    val daysInMonth = yearMonth.lengthOfMonth()
    return (1..daysInMonth).map { day ->
        val date = yearMonth.atDay(day)
        val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        TrendPointUi(
            label = day.toString(),
            value = values[day] ?: 0.0,
            detailLabel = "${weekday.capitalized()} $day",
        )
    }
}

fun buildYearTrendPoints(values: Map<Int, Double>): List<TrendPointUi> {
    return (1..12).map { month ->
        val monthLabel = Month.of(month).name.take(3).capitalized()
        TrendPointUi(
            label = monthLabel,
            value = values[month] ?: 0.0,
            detailLabel = monthLabel,
        )
    }
}

fun buildMonthlyComparison(currentTotal: Double, previousTotal: Double, selected: YearMonth): ComparisonInsightUi {
    val previous = selected.minusMonths(1)
    return buildComparisonInsight(
        title = "Vs ${previous.month.name.take(3).capitalized()} ${previous.year}",
        currentTotal = currentTotal,
        previousTotal = previousTotal,
    )
}

fun buildYearlyComparison(currentTotal: Double, previousTotal: Double, year: Int): ComparisonInsightUi {
    return buildComparisonInsight(
        title = "Vs ${year - 1}",
        currentTotal = currentTotal,
        previousTotal = previousTotal,
    )
}

private fun buildComparisonInsight(title: String, currentTotal: Double, previousTotal: Double): ComparisonInsightUi {
    if (previousTotal == 0.0 && currentTotal == 0.0) {
        return ComparisonInsightUi(title = title, summary = "No spending in either period", direction = ComparisonDirection.Flat)
    }
    if (previousTotal == 0.0) {
        return ComparisonInsightUi(title = title, summary = "Started spending in this period", direction = ComparisonDirection.Up)
    }
    val delta = currentTotal - previousTotal
    if (delta == 0.0) {
        return ComparisonInsightUi(title = title, summary = "No change from the previous period", direction = ComparisonDirection.Flat)
    }
    val percent = abs(delta / previousTotal) * 100
    val summary = String.format("%.0f%% %s", percent, if (delta > 0) "higher" else "lower")
    return ComparisonInsightUi(
        title = title,
        summary = summary,
        direction = if (delta > 0) ComparisonDirection.Up else ComparisonDirection.Down,
    )
}

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
