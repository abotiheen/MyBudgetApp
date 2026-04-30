package com.example.mybudgetapp.domain.insights

import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.database.BudgetTransaction
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class InsightSeverity {
    Positive,
    Neutral,
    Warning,
    Danger,
}

sealed interface InsightAction {
    data class OpenCategory(val categoryKey: String) : InsightAction
    data object OpenTransactions : InsightAction
    data object OpenTrend : InsightAction
    data object OpenComparison : InsightAction
    data object None : InsightAction
}

data class InsightText(
    val title: String,
    val value: String,
    val subtitle: String,
    val explanation: String = "",
    val recommendation: String = "",
)

sealed class InsightUi {
    abstract val id: String
    abstract val priority: Int
    abstract val severity: InsightSeverity
    abstract val action: InsightAction?
    abstract val text: InsightText

    data class Forecast(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val projectedTotal: Double,
        val baselineTotal: Double?,
        val deltaPercent: Double?,
        val confidence: InsightConfidence,
    ) : InsightUi()

    data class BaselineComparison(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val currentTotal: Double,
        val baselineTotal: Double?,
        val deltaPercent: Double?,
    ) : InsightUi()

    data class CategoryAnomaly(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val categoryKey: String,
        val categoryLabel: String,
        val currentTotal: Double,
        val baselineTotal: Double,
        val deltaPercent: Double,
    ) : InsightUi()

    data class SavingOpportunity(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val categoryKey: String,
        val categoryLabel: String,
        val estimatedSaving: Double,
    ) : InsightUi()

    data class DriverAnalysis(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val drivers: List<CategoryDelta>,
    ) : InsightUi()

    data class RecurringVsFlexible(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val fixedShare: Double?,
        val flexibleCategoryKeys: List<String>,
    ) : InsightUi()

    data class TimePattern(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val dayOfWeek: DayOfWeek,
        val dayTotal: Double,
    ) : InsightUi()

    data class SpendingStreak(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val days: Int,
    ) : InsightUi()

    data class TransactionBehavior(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val transactionCount: Int,
        val averageTransaction: Double,
        val medianTransaction: Double,
        val topTransactionShare: Double,
    ) : InsightUi()

    data class ConcentrationRisk(
        override val id: String,
        override val priority: Int,
        override val severity: InsightSeverity,
        override val action: InsightAction?,
        override val text: InsightText,
        val categoryKey: String,
        val categoryLabel: String,
        val share: Double,
    ) : InsightUi()
}

enum class InsightConfidence {
    Low,
    Medium,
    High,
}

enum class InsightPeriodScope {
    Month,
    Year,
}

data class InsightPeriod(
    val scope: InsightPeriodScope,
    val year: Int,
    val month: Int? = null,
) {
    val yearMonth: YearMonth?
        get() = month?.let { YearMonth.of(year, it) }
}

data class PeriodTransactions(
    val period: InsightPeriod,
    val transactions: List<BudgetTransaction>,
)

data class InsightSettings(
    val today: LocalDate = LocalDate.now(),
    val currencyFormatter: (Double) -> String = { amount -> amount.roundToInt().toString() },
    val minMeaningfulAmount: Double = 25_000.0,
    val anomalyThresholdPercent: Double = 25.0,
    val concentrationRiskShare: Double = 0.40,
    val maxInsights: Int = 7,
)

data class InsightStatus(
    val label: String,
    val explanation: String,
    val severity: InsightSeverity,
    val monthProgress: Float? = null,
)

data class InsightsEngineResult(
    val status: InsightStatus,
    val insights: List<InsightUi>,
    val trendAnnotation: String,
    val hasBaseline: Boolean,
)

data class CategoryDelta(
    val categoryKey: String,
    val categoryLabel: String,
    val currentAmount: Double,
    val comparisonAmount: Double,
    val deltaAmount: Double,
)

class InsightsEngine {

    fun generateInsights(
        transactions: List<BudgetTransaction>,
        selectedPeriod: InsightPeriod,
        previousPeriods: List<PeriodTransactions>,
        settings: InsightSettings = InsightSettings(),
        categoryLabels: Map<String, String> = emptyMap(),
    ): InsightsEngineResult {
        val current = transactions
            .mapNotNull { it.toInsightTransaction(categoryLabels) }
            .filter { it.amount > 0.0 }
        val previous = previousPeriods.map { period ->
            period.copy(
                transactions = period.transactions.filter { it.amount > 0.0 },
            )
        }
        val currentTotal = current.sumOf { it.amount }
        val baselineTotals = previous
            .map { period -> period.transactions.sumOf { it.amount } }
            .filter { it > 0.0 }
        val baselineTotal = baselineTotals.averageOrNull()
        val previousTotal = baselineTotals.firstOrNull()
        val comparisonTotal = baselineTotal ?: previousTotal
        val currentCategoryTotals = current.categoryTotals()
        val baselineCategoryTotals = previous.categoryBaselines(categoryLabels)
        val comparisonCategoryTotals = if (baselineCategoryTotals.isNotEmpty()) {
            baselineCategoryTotals
        } else {
            previous.firstOrNull()?.transactions
                ?.mapNotNull { it.toInsightTransaction(categoryLabels) }
                ?.categoryTotals()
                ?: emptyMap()
        }

        if (current.isEmpty()) {
            return InsightsEngineResult(
                status = InsightStatus(
                    label = "No spending yet",
                    explanation = "Add expenses for this period and insights will appear here.",
                    severity = InsightSeverity.Neutral,
                    monthProgress = selectedPeriod.monthProgress(settings.today),
                ),
                insights = emptyList(),
                trendAnnotation = "No spending has been recorded for this period yet.",
                hasBaseline = baselineTotal != null,
            )
        }

        val allInsights = buildList {
            buildForecastInsight(
                selectedPeriod = selectedPeriod,
                currentTotal = currentTotal,
                baselineTotal = baselineTotal,
                transactionCount = current.size,
                settings = settings,
            )?.let(::add)
            add(
                buildBaselineComparisonInsight(
                    currentTotal = currentTotal,
                    baselineTotal = baselineTotal,
                    fallbackTotal = previousTotal,
                    settings = settings,
                )
            )
            buildCategoryAnomalies(
                currentCategoryTotals = currentCategoryTotals,
                baselineCategoryTotals = baselineCategoryTotals,
                settings = settings,
            ).forEach(::add)
            buildSavingOpportunity(
                currentCategoryTotals = currentCategoryTotals,
                baselineCategoryTotals = baselineCategoryTotals,
                settings = settings,
            )?.let(::add)
            buildDriverAnalysis(
                currentCategoryTotals = currentCategoryTotals,
                comparisonCategoryTotals = comparisonCategoryTotals,
                settings = settings,
            )?.let(::add)
            buildTimePattern(current, settings)?.let(::add)
            buildSpendingStreak(
                selectedPeriod = selectedPeriod,
                transactions = current,
                baselineTotal = baselineTotal,
                settings = settings,
            )?.let(::add)
            buildTransactionBehavior(current, currentTotal, settings)?.let(::add)
            buildConcentrationRisk(currentCategoryTotals, currentTotal, settings)?.let(::add)
        }

        return InsightsEngineResult(
            status = buildStatus(
                selectedPeriod = selectedPeriod,
                currentTotal = currentTotal,
                baselineTotal = baselineTotal,
                settings = settings,
            ),
            insights = allInsights
                .distinctBy { it.id }
                .sortedWith(compareByDescending<InsightUi> { it.priority }.thenBy { it.id })
                .take(settings.maxInsights),
            trendAnnotation = buildTrendAnnotation(selectedPeriod, current, settings),
            hasBaseline = baselineTotal != null,
        )
    }

    private fun buildForecastInsight(
        selectedPeriod: InsightPeriod,
        currentTotal: Double,
        baselineTotal: Double?,
        transactionCount: Int,
        settings: InsightSettings,
    ): InsightUi.Forecast? {
        val yearMonth = selectedPeriod.yearMonth ?: return null
        val elapsedDays = selectedPeriod.elapsedDays(settings.today)
        if (elapsedDays <= 0 || currentTotal < settings.minMeaningfulAmount) return null
        if (elapsedDays < 5 && transactionCount < 5) return null
        val projected = currentTotal / elapsedDays * yearMonth.lengthOfMonth()
        val deltaPercent = baselineTotal?.percentDelta(projected)
        val confidence = when {
            elapsedDays < 5 -> InsightConfidence.Low
            elapsedDays < 10 -> InsightConfidence.Medium
            else -> InsightConfidence.High
        }
        val severity = when {
            deltaPercent == null -> InsightSeverity.Neutral
            deltaPercent >= 20.0 -> InsightSeverity.Danger
            deltaPercent >= 10.0 -> InsightSeverity.Warning
            deltaPercent <= -10.0 -> InsightSeverity.Positive
            else -> InsightSeverity.Neutral
        }
        val subtitle = when {
            deltaPercent == null -> "${confidence.label()} confidence based on pace so far"
            deltaPercent >= 0 -> "${deltaPercent.formatPercent()} above your usual month"
            else -> "${abs(deltaPercent).formatPercent()} below your usual month"
        }
        val recommendation = if (baselineTotal != null && projected > baselineTotal) {
            val remainingDays = (yearMonth.lengthOfMonth() - elapsedDays).coerceAtLeast(1)
            val dailyTarget = ((baselineTotal - currentTotal).coerceAtLeast(0.0)) / remainingDays
            "Keep daily spending near ${settings.currencyFormatter(dailyTarget)} IQD to stay close to normal."
        } else {
            "Keep the current pace if this level still fits your month."
        }
        return InsightUi.Forecast(
            id = "forecast",
            priority = if (severity == InsightSeverity.Danger) 98 else 88,
            severity = severity,
            action = InsightAction.OpenTrend,
            text = InsightText(
                title = "Projected month-end",
                value = settings.currencyFormatter(projected),
                subtitle = subtitle,
                explanation = "Projection uses spending so far divided by elapsed days, then extends that pace to the full month.",
                recommendation = recommendation,
            ),
            projectedTotal = projected,
            baselineTotal = baselineTotal,
            deltaPercent = deltaPercent,
            confidence = confidence,
        )
    }

    private fun buildBaselineComparisonInsight(
        currentTotal: Double,
        baselineTotal: Double?,
        fallbackTotal: Double?,
        settings: InsightSettings,
    ): InsightUi.BaselineComparison {
        val comparison = baselineTotal ?: fallbackTotal
        val deltaPercent = comparison?.percentDelta(currentTotal)
        val severity = when {
            deltaPercent == null -> InsightSeverity.Neutral
            deltaPercent >= 20.0 -> InsightSeverity.Danger
            deltaPercent >= 10.0 -> InsightSeverity.Warning
            deltaPercent <= -10.0 -> InsightSeverity.Positive
            else -> InsightSeverity.Neutral
        }
        val value = when {
            deltaPercent == null -> "New baseline"
            deltaPercent >= 0 -> "+${deltaPercent.formatPercent()}"
            else -> "-${abs(deltaPercent).formatPercent()}"
        }
        val subtitle = when {
            baselineTotal != null -> "Compared with your recent monthly average"
            fallbackTotal != null -> "Compared with your previous period"
            else -> "Not enough history yet"
        }
        val explanation = when {
            baselineTotal != null -> "This compares the selected period against your average from previous completed periods."
            fallbackTotal != null -> "There is not enough history for a stable baseline, so this uses the previous period as a fallback."
            else -> "After more periods have spending, this card will compare against your personal baseline."
        }
        return InsightUi.BaselineComparison(
            id = "baseline",
            priority = if (deltaPercent == null) 35 else 90,
            severity = severity,
            action = InsightAction.OpenComparison,
            text = InsightText(
                title = "Against your usual",
                value = value,
                subtitle = subtitle,
                explanation = explanation,
                recommendation = baselineRecommendation(deltaPercent),
            ),
            currentTotal = currentTotal,
            baselineTotal = comparison,
            deltaPercent = deltaPercent,
        )
    }

    private fun buildCategoryAnomalies(
        currentCategoryTotals: Map<String, CategoryAmount>,
        baselineCategoryTotals: Map<String, CategoryAmount>,
        settings: InsightSettings,
    ): List<InsightUi.CategoryAnomaly> {
        return currentCategoryTotals.values.mapNotNull { current ->
            val baseline = baselineCategoryTotals[current.categoryKey] ?: return@mapNotNull null
            if (baseline.amount <= 0.0) return@mapNotNull null
            val delta = current.amount - baseline.amount
            val deltaPercent = baseline.amount.percentDelta(current.amount) ?: return@mapNotNull null
            val meaningfulDelta = abs(delta) >= settings.minMeaningfulAmount
            if (abs(deltaPercent) <= settings.anomalyThresholdPercent || !meaningfulDelta) return@mapNotNull null
            val severity = when {
                deltaPercent >= 45.0 -> InsightSeverity.Danger
                deltaPercent > 0.0 -> InsightSeverity.Warning
                else -> InsightSeverity.Positive
            }
            InsightUi.CategoryAnomaly(
                id = "anomaly-${current.categoryKey}",
                priority = if (severity == InsightSeverity.Danger) 96 else 84,
                severity = severity,
                action = InsightAction.OpenCategory(current.categoryKey),
                text = InsightText(
                    title = "${current.categoryLabel} is ${if (delta > 0) "unusually high" else "lower than usual"}",
                    value = if (delta > 0) "+${deltaPercent.formatPercent()}" else "-${abs(deltaPercent).formatPercent()}",
                    subtitle = "Compared with your recent average",
                    explanation = "${current.categoryLabel} is ${settings.currencyFormatter(abs(delta))} IQD ${if (delta > 0) "above" else "below"} its baseline.",
                    recommendation = if (delta > 0) {
                        "Review recent ${current.categoryLabel} transactions to see what changed."
                    } else {
                        "This category is moving in a better direction than usual."
                    },
                ),
                categoryKey = current.categoryKey,
                categoryLabel = current.categoryLabel,
                currentTotal = current.amount,
                baselineTotal = baseline.amount,
                deltaPercent = deltaPercent,
            )
        }.sortedByDescending { abs(it.deltaPercent) }.take(2)
    }

    private fun buildSavingOpportunity(
        currentCategoryTotals: Map<String, CategoryAmount>,
        baselineCategoryTotals: Map<String, CategoryAmount>,
        settings: InsightSettings,
    ): InsightUi.SavingOpportunity? {
        val opportunity = currentCategoryTotals.values
            .filter { it.amount >= settings.minMeaningfulAmount * 2 }
            .map { current ->
                val baseline = baselineCategoryTotals[current.categoryKey]?.amount
                val aboveBaseline = baseline?.let { (current.amount - it).coerceAtLeast(0.0) }
                val estimatedSaving = minOf(current.amount * 0.20, aboveBaseline?.takeIf { it > 0.0 } ?: current.amount * 0.20)
                current to estimatedSaving
            }
            .filter { (_, saving) -> saving >= settings.minMeaningfulAmount }
            .maxByOrNull { (_, saving) -> saving }
            ?: return null
        val (category, saving) = opportunity
        return InsightUi.SavingOpportunity(
            id = "saving-${category.categoryKey}",
            priority = 92,
            severity = InsightSeverity.Positive,
            action = InsightAction.OpenCategory(category.categoryKey),
            text = InsightText(
                title = "Best saving opportunity",
                value = "Save ~${settings.currencyFormatter(saving)}",
                subtitle = "By reducing ${category.categoryLabel} spending by 20%",
                explanation = "This looks for the category where a modest reduction would make the biggest difference.",
                recommendation = "Start with recent ${category.categoryLabel} transactions and keep the cuts realistic.",
            ),
            categoryKey = category.categoryKey,
            categoryLabel = category.categoryLabel,
            estimatedSaving = saving,
        )
    }

    private fun buildDriverAnalysis(
        currentCategoryTotals: Map<String, CategoryAmount>,
        comparisonCategoryTotals: Map<String, CategoryAmount>,
        settings: InsightSettings,
    ): InsightUi.DriverAnalysis? {
        if (comparisonCategoryTotals.isEmpty()) return null
        val keys = currentCategoryTotals.keys + comparisonCategoryTotals.keys
        val drivers = keys.mapNotNull { key ->
            val current = currentCategoryTotals[key]
            val comparison = comparisonCategoryTotals[key]
            val label = current?.categoryLabel ?: comparison?.categoryLabel ?: key.capitalized()
            val currentAmount = current?.amount ?: 0.0
            val comparisonAmount = comparison?.amount ?: 0.0
            val delta = currentAmount - comparisonAmount
            if (abs(delta) < settings.minMeaningfulAmount) return@mapNotNull null
            CategoryDelta(
                categoryKey = key,
                categoryLabel = label,
                currentAmount = currentAmount,
                comparisonAmount = comparisonAmount,
                deltaAmount = delta,
            )
        }.sortedByDescending { abs(it.deltaAmount) }.take(3)
        if (drivers.isEmpty()) return null
        val totalDelta = drivers.sumOf { it.deltaAmount }
        val severity = when {
            totalDelta > settings.minMeaningfulAmount -> InsightSeverity.Warning
            totalDelta < -settings.minMeaningfulAmount -> InsightSeverity.Positive
            else -> InsightSeverity.Neutral
        }
        val leading = drivers.joinToString { driver ->
            "${driver.categoryLabel} ${if (driver.deltaAmount >= 0) "+" else "-"}${settings.currencyFormatter(abs(driver.deltaAmount))}"
        }
        return InsightUi.DriverAnalysis(
            id = "drivers",
            priority = 86,
            severity = severity,
            action = InsightAction.OpenComparison,
            text = InsightText(
                title = "What changed",
                value = if (totalDelta >= 0) "Spending increased" else "Spending decreased",
                subtitle = leading,
                explanation = "These categories explain most of the difference versus your comparison period.",
                recommendation = "Open the largest driver first if you want to understand the change quickly.",
            ),
            drivers = drivers,
        )
    }

    private fun buildTimePattern(
        transactions: List<InsightTransaction>,
        settings: InsightSettings,
    ): InsightUi.TimePattern? {
        val byDay = transactions
            .groupBy { it.date.dayOfWeek }
            .mapValues { (_, dayTransactions) -> dayTransactions.sumOf { it.amount } }
        if (byDay.size < 3) return null
        val average = byDay.values.average()
        val peak = byDay.maxByOrNull { it.value } ?: return null
        if (peak.value < settings.minMeaningfulAmount || peak.value < average * 1.35) return null
        val dayLabel = peak.key.displayName()
        return InsightUi.TimePattern(
            id = "weekday-${peak.key.name.lowercase()}",
            priority = 68,
            severity = InsightSeverity.Neutral,
            action = InsightAction.OpenTrend,
            text = InsightText(
                title = "$dayLabel is your highest spending day",
                value = settings.currencyFormatter(peak.value),
                subtitle = "Based on date patterns in this period",
                explanation = "The app only has date-level data today, so this insight uses weekdays and does not infer time of day.",
                recommendation = "Check whether $dayLabel includes planned expenses or avoidable repeat spending.",
            ),
            dayOfWeek = peak.key,
            dayTotal = peak.value,
        )
    }

    private fun buildSpendingStreak(
        selectedPeriod: InsightPeriod,
        transactions: List<InsightTransaction>,
        baselineTotal: Double?,
        settings: InsightSettings,
    ): InsightUi.SpendingStreak? {
        val yearMonth = selectedPeriod.yearMonth ?: return null
        val endDay = selectedPeriod.elapsedDays(settings.today).coerceIn(1, yearMonth.lengthOfMonth())
        val dailyTarget = (baselineTotal ?: transactions.sumOf { it.amount })
            .takeIf { it > 0.0 }
            ?.div(yearMonth.lengthOfMonth())
            ?: return null
        val totalsByDay = transactions
            .groupBy { it.date.dayOfMonth }
            .mapValues { (_, dayTransactions) -> dayTransactions.sumOf { it.amount } }
        var streak = 0
        for (day in endDay downTo 1) {
            val total = totalsByDay[day] ?: 0.0
            if (total <= dailyTarget) {
                streak += 1
            } else {
                break
            }
        }
        if (streak < 3) return null
        return InsightUi.SpendingStreak(
            id = "streak-under-daily-average",
            priority = 52,
            severity = InsightSeverity.Positive,
            action = InsightAction.OpenTrend,
            text = InsightText(
                title = "$streak days under daily average",
                value = "$streak days",
                subtitle = "Recent pace is lighter than your daily target",
                explanation = "A day counts when total expenses are at or below your usual daily pace.",
                recommendation = "Keep the same rhythm for the next few days if it feels sustainable.",
            ),
            days = streak,
        )
    }

    private fun buildTransactionBehavior(
        transactions: List<InsightTransaction>,
        currentTotal: Double,
        settings: InsightSettings,
    ): InsightUi.TransactionBehavior? {
        if (transactions.size < 4 || currentTotal < settings.minMeaningfulAmount) return null
        val amounts = transactions.map { it.amount }.sorted()
        val average = currentTotal / amounts.size
        val median = amounts.median()
        val topShare = amounts.last() / currentTotal
        val largeCount = amounts.count { it >= average * 1.8 }
        val (value, subtitle, severity, recommendation) = when {
            topShare >= 0.40 -> Quad(
                "Few large purchases",
                "Top transaction is ${(topShare * 100).roundToInt()}% of spending",
                InsightSeverity.Warning,
                "Review the largest purchase before cutting everyday spending.",
            )
            median < average * 0.70 && largeCount <= 1 -> Quad(
                "Frequent small purchases",
                "Median transaction is ${settings.currencyFormatter(median)} IQD",
                InsightSeverity.Neutral,
                "Small repeat purchases are the better place to look first.",
            )
            else -> Quad(
                "Balanced transactions",
                "No single purchase dominates this period",
                InsightSeverity.Positive,
                "Your spending is not concentrated in one transaction pattern.",
            )
        }
        return InsightUi.TransactionBehavior(
            id = "transaction-behavior",
            priority = 58,
            severity = severity,
            action = InsightAction.OpenTransactions,
            text = InsightText(
                title = "Transaction behavior",
                value = value,
                subtitle = subtitle,
                explanation = "This compares transaction count, average, median, and the largest transaction share.",
                recommendation = recommendation,
            ),
            transactionCount = transactions.size,
            averageTransaction = average,
            medianTransaction = median,
            topTransactionShare = topShare,
        )
    }

    private fun buildConcentrationRisk(
        currentCategoryTotals: Map<String, CategoryAmount>,
        currentTotal: Double,
        settings: InsightSettings,
    ): InsightUi.ConcentrationRisk? {
        if (currentTotal < settings.minMeaningfulAmount * 2) return null
        val top = currentCategoryTotals.values.maxByOrNull { it.amount } ?: return null
        val share = top.amount / currentTotal
        if (share < settings.concentrationRiskShare) return null
        val severity = if (share >= 0.55) InsightSeverity.Warning else InsightSeverity.Neutral
        return InsightUi.ConcentrationRisk(
            id = "concentration-${top.categoryKey}",
            priority = if (severity == InsightSeverity.Warning) 74 else 62,
            severity = severity,
            action = InsightAction.OpenCategory(top.categoryKey),
            text = InsightText(
                title = "${top.categoryLabel} dominates spending",
                value = "${(share * 100).roundToInt()}%",
                subtitle = "High category concentration",
                explanation = "When one category takes a large share, small changes there can affect the whole budget.",
                recommendation = "Review ${top.categoryLabel} before spreading attention across smaller categories.",
            ),
            categoryKey = top.categoryKey,
            categoryLabel = top.categoryLabel,
            share = share,
        )
    }

    private fun buildStatus(
        selectedPeriod: InsightPeriod,
        currentTotal: Double,
        baselineTotal: Double?,
        settings: InsightSettings,
    ): InsightStatus {
        val comparisonValue = if (selectedPeriod.scope == InsightPeriodScope.Month) {
            val yearMonth = selectedPeriod.yearMonth
            if (yearMonth != null) {
                val elapsedDays = selectedPeriod.elapsedDays(settings.today)
                currentTotal / elapsedDays.coerceAtLeast(1) * yearMonth.lengthOfMonth()
            } else {
                currentTotal
            }
        } else {
            currentTotal
        }
        val delta = baselineTotal?.percentDelta(comparisonValue)
        val label = when {
            delta == null -> "Building baseline"
            delta >= 10.0 -> "Above usual"
            delta <= -10.0 -> "Below usual"
            else -> "On track"
        }
        val severity = when {
            delta == null -> InsightSeverity.Neutral
            delta >= 20.0 -> InsightSeverity.Danger
            delta >= 10.0 -> InsightSeverity.Warning
            delta <= -10.0 -> InsightSeverity.Positive
            else -> InsightSeverity.Neutral
        }
        val explanation = when {
            delta == null -> "More history will make this comparison smarter."
            delta >= 10.0 -> "Spending is running ${delta.formatPercent()} above your usual pace."
            delta <= -10.0 -> "Spending is running ${abs(delta).formatPercent()} below your usual pace."
            else -> "Spending is close to your usual pace."
        }
        return InsightStatus(
            label = label,
            explanation = explanation,
            severity = severity,
            monthProgress = selectedPeriod.monthProgress(settings.today),
        )
    }

    private fun buildTrendAnnotation(
        selectedPeriod: InsightPeriod,
        transactions: List<InsightTransaction>,
        settings: InsightSettings,
    ): String {
        val peak = when (selectedPeriod.scope) {
            InsightPeriodScope.Month -> transactions
                .groupBy { it.date.dayOfMonth }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
                .maxByOrNull { it.value }
                ?.let { "Peak day ${it.key}: ${settings.currencyFormatter(it.value)} IQD" }
            InsightPeriodScope.Year -> transactions
                .groupBy { it.date.month }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
                .maxByOrNull { it.value }
                ?.let { "${it.key.getDisplayName(TextStyle.SHORT, Locale.getDefault())} carried the highest spending." }
        }
        return peak ?: "The trend will become clearer as more transactions are added."
    }
}

private data class InsightTransaction(
    val amount: Double,
    val categoryKey: String,
    val categoryLabel: String,
    val date: LocalDate,
)

private data class CategoryAmount(
    val categoryKey: String,
    val categoryLabel: String,
    val amount: Double,
)

private data class Quad(
    val value: String,
    val subtitle: String,
    val severity: InsightSeverity,
    val recommendation: String,
)

private fun BudgetTransaction.toInsightTransaction(
    categoryLabels: Map<String, String>,
): InsightTransaction? {
    val date = runCatching { LocalDate.parse(transactionDate) }.getOrNull() ?: return null
    val normalizedCategory = category.trim().ifBlank { UNCATEGORIZED_KEY }
    return InsightTransaction(
        amount = amount,
        categoryKey = normalizedCategory,
        categoryLabel = categoryLabels[normalizedCategory] ?: normalizedCategory.capitalized(),
        date = date,
    )
}

private fun List<InsightTransaction>.categoryTotals(): Map<String, CategoryAmount> =
    groupBy { it.categoryKey }.mapValues { (key, items) ->
        CategoryAmount(
            categoryKey = key,
            categoryLabel = items.first().categoryLabel,
            amount = items.sumOf { it.amount },
        )
    }

private fun List<PeriodTransactions>.categoryBaselines(
    categoryLabels: Map<String, String>,
): Map<String, CategoryAmount> {
    val nonEmptyPeriods = filter { period -> period.transactions.any { it.amount > 0.0 } }
    if (nonEmptyPeriods.isEmpty()) return emptyMap()
    val totalsByCategory = mutableMapOf<String, Double>()
    val labels = mutableMapOf<String, String>()
    nonEmptyPeriods.forEach { period ->
        period.transactions
            .mapNotNull { it.toInsightTransaction(categoryLabels) }
            .categoryTotals()
            .forEach { (key, amount) ->
                totalsByCategory[key] = (totalsByCategory[key] ?: 0.0) + amount.amount
                labels[key] = amount.categoryLabel
            }
    }
    return totalsByCategory.mapValues { (key, total) ->
        CategoryAmount(
            categoryKey = key,
            categoryLabel = labels[key] ?: key.capitalized(),
            amount = total / nonEmptyPeriods.size,
        )
    }
}

private fun InsightPeriod.elapsedDays(today: LocalDate): Int {
    val yearMonth = yearMonth ?: return 365
    return when {
        yearMonth == YearMonth.from(today) -> today.dayOfMonth
        yearMonth.isBefore(YearMonth.from(today)) -> yearMonth.lengthOfMonth()
        else -> 1
    }
}

private fun InsightPeriod.monthProgress(today: LocalDate): Float? {
    val yearMonth = yearMonth ?: return null
    return (elapsedDays(today).toFloat() / yearMonth.lengthOfMonth()).coerceIn(0f, 1f)
}

private fun Double.percentDelta(current: Double): Double? =
    if (this == 0.0) null else ((current - this) / this) * 100.0

private fun Iterable<Double>.averageOrNull(): Double? {
    val values = toList()
    return values.takeIf { it.isNotEmpty() }?.average()
}

private fun List<Double>.median(): Double {
    if (isEmpty()) return 0.0
    val middle = size / 2
    return if (size % 2 == 0) {
        (this[middle - 1] + this[middle]) / 2.0
    } else {
        this[middle]
    }
}

private fun Double.formatPercent(): String = "${abs(this).roundToInt()}%"

private fun InsightConfidence.label(): String = name.lowercase().capitalized()

private fun DayOfWeek.displayName(): String =
    getDisplayName(TextStyle.FULL, Locale.getDefault())

private fun baselineRecommendation(deltaPercent: Double?): String = when {
    deltaPercent == null -> "Keep tracking; the app will learn your normal range over time."
    deltaPercent >= 10.0 -> "Look at the largest category changes before making broad cuts."
    deltaPercent <= -10.0 -> "This is a good pace; keep the changes that made it possible."
    else -> "No major adjustment is needed right now."
}

private const val UNCATEGORIZED_KEY = "uncategorized"
