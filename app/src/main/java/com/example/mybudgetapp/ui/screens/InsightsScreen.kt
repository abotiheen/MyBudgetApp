package com.example.mybudgetapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.CategoryTotalUi
import com.example.mybudgetapp.ui.viewmodels.ComparisonDirection
import com.example.mybudgetapp.ui.viewmodels.InsightScope
import com.example.mybudgetapp.ui.viewmodels.InsightsUiState
import com.example.mybudgetapp.ui.viewmodels.InsightsViewModel
import com.example.mybudgetapp.ui.viewmodels.StatInsightUi
import com.example.mybudgetapp.ui.viewmodels.TrendPointUi
import com.example.mybudgetapp.ui.viewmodels.categoryLabel
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.BudgetValueText
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.TrendChartCard
import com.example.mybudgetapp.ui.widgets.categoryAccentColor
import com.example.mybudgetapp.ui.widgets.categoryIconPainter

object InsightsDestination : NavigationDestination {
    override val route = "Insights"
    override val titleRes = R.string.insights_screen
    const val scope = "scope"
    const val year = "year"
    const val month = "month"
    val routeWithArgs = "$route/{$scope}/{$year}/{$month}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    navigateToSpendingOnCategory: (String) -> Unit,
    navigateToCategories: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: InsightsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                title = stringResource(id = R.string.insights_screen),
                navigateBack = navigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        BudgetBackdrop(modifier = Modifier.padding(paddingValues)) {
            InsightsBody(
                uiState = uiState.value,
                navigateToSpendingOnCategory = navigateToSpendingOnCategory,
                navigateToCategories = navigateToCategories,
            )
        }
    }
}

@Composable
private fun InsightsBody(
    modifier: Modifier = Modifier,
    uiState: InsightsUiState,
    navigateToSpendingOnCategory: (String) -> Unit,
    navigateToCategories: () -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val allInsights = (uiState.overviewInsights + uiState.habitInsights)
        .distinctBy { it.title }
    val topCategory = uiState.categoryTotals.maxByOrNull { it.total }
    val topShare = if (uiState.totalSpendingAmount > 0) {
        ((topCategory?.total ?: 0.0) / uiState.totalSpendingAmount).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    val biggestExpense = allInsights.find { it.title.contains("Biggest", ignoreCase = true) }
    val rateInsight = allInsights.find { it.title.contains("Avg daily", ignoreCase = true) || it.title.contains("Avg monthly", ignoreCase = true) }
    val rhythmInsight = allInsights.find { it.title.contains("Spending days", ignoreCase = true) || it.title.contains("Active months", ignoreCase = true) }
    val transactionInsight = allInsights.find { it.title.contains("Avg transaction", ignoreCase = true) }
    val peakPoint = uiState.trendPoints.maxByOrNull { it.value }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = spacing.lg,
            bottom = 48.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            InsightsPulseHero(
                uiState = uiState,
                topCategory = topCategory,
                topShare = topShare,
            )
        }
        item {
            InsightSectionHeader(
                title = "What matters",
                subtitle = "The signals most people need before diving into transaction lists.",
            )
        }
        item {
            FocusSignalsRow(
                biggestExpense = biggestExpense,
                rateInsight = rateInsight,
                rhythmInsight = rhythmInsight,
            )
        }
        item {
            TrendNarrativeCard(
                scope = uiState.scope,
                point = peakPoint,
                comparison = uiState.comparison.summary,
            )
        }
        item {
            TrendChartCard(
                title = if (uiState.scope == InsightScope.Month) "Daily pace" else "Monthly pace",
                subtitle = if (uiState.scope == InsightScope.Month) {
                    "See which days pushed spending hardest."
                } else {
                    "See which months carried the most weight."
                },
                points = uiState.trendPoints,
            )
        }
        item {
            InsightSectionHeader(
                title = "Where It Went",
                subtitle = "Category concentration matters more than raw counts when you want to adjust behavior.",
                actionLabel = "Manage",
                onAction = navigateToCategories,
            )
        }
        item {
            CategoryFocusCard(
                topCategory = topCategory,
                topShare = topShare,
                transactionInsight = transactionInsight,
            )
        }
        items(uiState.categoryTotals) { category ->
            CategoryBreakdownRow(
                category = category,
                totalSpendingAmount = uiState.totalSpendingAmount,
                onClick = { navigateToSpendingOnCategory(category.category) },
            )
        }
    }
}

@Composable
private fun InsightsPulseHero(
    uiState: InsightsUiState,
    topCategory: CategoryTotalUi?,
    topShare: Float,
) {
    val accent = comparisonAccent(uiState.comparison.direction)
    val summaryLine = buildSummaryLine(
        scope = uiState.scope,
        comparison = uiState.comparison.summary,
        topCategory = topCategory,
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            BudgetTheme.extendedColors.heroStart,
                            BudgetTheme.extendedColors.heroEnd,
                            MaterialTheme.colorScheme.primaryContainer,
                        )
                    )
                )
                .padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (uiState.scope == InsightScope.Month) "Spending pulse" else "Year pulse",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.74f),
                    )
                    Text(
                        text = uiState.periodLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                InsightHeroBadge(
                    label = if (uiState.comparison.summary.isBlank()) "No comparison" else uiState.comparison.summary,
                    tint = accent,
                )
            }

            BudgetValueText(
                text = uiState.totalSpending,
                modifier = Modifier.fillMaxWidth(),
                tone = BudgetValueTone.Hero,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                unitLabel = "IQD",
            )

            Text(
                text = summaryLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
            )

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            ) {
                Column(
                    modifier = Modifier.padding(BudgetTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Main concentration",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = topCategory?.label ?: "No category yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            BudgetValueText(
                                text = topCategory?.let { formatCompactCurrencyIraqiDinar(it.total) }
                                    ?: formatCompactCurrencyIraqiDinar(0.0),
                                tone = BudgetValueTone.Compact,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                unitLabel = "IQD",
                            )
                        }
                        Text(
                            text = "${(topShare * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    InsightProgressBar(
                        progress = topShare,
                        accent = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusSignalsRow(
    biggestExpense: StatInsightUi?,
    rateInsight: StatInsightUi?,
    rhythmInsight: StatInsightUi?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
    ) {
        FocusSignalCard(
            modifier = Modifier.weight(1f),
            label = biggestExpense?.title ?: "Biggest expense",
            value = biggestExpense?.value ?: formatCompactCurrencyIraqiDinar(0.0),
            note = biggestExpense?.subtitle ?: "No expense yet",
            accent = BudgetTheme.extendedColors.danger,
            isMonetary = biggestExpense?.isMonetary ?: true,
        )
        FocusSignalCard(
            modifier = Modifier.weight(1f),
            label = rateInsight?.title ?: "Spend rate",
            value = rateInsight?.value ?: formatCompactCurrencyIraqiDinar(0.0),
            note = rateInsight?.subtitle ?: "No pace yet",
            accent = BudgetTheme.extendedColors.transit,
            isMonetary = rateInsight?.isMonetary ?: true,
        )
        FocusSignalCard(
            modifier = Modifier.weight(1f),
            label = rhythmInsight?.title ?: "Rhythm",
            value = rhythmInsight?.value ?: "0",
            note = rhythmInsight?.subtitle ?: "No activity yet",
            accent = BudgetTheme.extendedColors.food,
            isMonetary = rhythmInsight?.isMonetary ?: false,
        )
    }
}

@Composable
private fun FocusSignalCard(
    label: String,
    value: String,
    note: String,
    accent: Color,
    isMonetary: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BudgetValueText(
                text = value,
                tone = BudgetValueTone.Compact,
                color = accent,
                unitLabel = if (isMonetary) "IQD" else null,
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun TrendNarrativeCard(
    scope: InsightScope,
    point: TrendPointUi?,
    comparison: String,
) {
    val peakLabel = when (scope) {
        InsightScope.Month -> point?.let { "Peak day ${it.label}" } ?: "No peak yet"
        InsightScope.Year -> point?.let { "Peak month ${it.label}" } ?: "No peak yet"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            Text(
                text = "Pattern read",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = peakLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (comparison.isBlank()) {
                    "Use the chart below to see whether spending was steady or clustered."
                } else {
                    "$comparison. The chart below shows where the pressure landed."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CategoryFocusCard(
    topCategory: CategoryTotalUi?,
    topShare: Float,
    transactionInsight: StatInsightUi?,
) {
    val accent = categoryAccentColor(topCategory?.colorHex.orEmpty(), topCategory?.category.orEmpty())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Top category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = topCategory?.label ?: "None",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = "${(topShare * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                )
            }

            BudgetValueText(
                text = topCategory?.let { formatCompactCurrencyIraqiDinar(it.total) } ?: formatCompactCurrencyIraqiDinar(0.0),
                tone = BudgetValueTone.Prominent,
                color = accent,
                unitLabel = "IQD",
            )

            if (transactionInsight != null) {
                Surface(
                    color = accent.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(BudgetTheme.radii.md),
                ) {
                    Column(
                        modifier = Modifier.padding(BudgetTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = transactionInsight.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        BudgetValueText(
                            text = transactionInsight.value,
                            tone = BudgetValueTone.Compact,
                            color = MaterialTheme.colorScheme.onSurface,
                            unitLabel = if (transactionInsight.isMonetary) "IQD" else null,
                        )
                        Text(
                            text = transactionInsight.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
    category: CategoryTotalUi,
    totalSpendingAmount: Double,
    onClick: () -> Unit,
) {
    val accent = categoryAccentColor(category.colorHex, category.category)
    val iconPainter = categoryIconPainter(category.iconKey, category.category)
    val progress = if (totalSpendingAmount > 0) {
        (category.total / totalSpendingAmount).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = accent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(BudgetTheme.radii.md),
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = null,
                                tint = accent,
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        BudgetValueText(
                            text = formatCompactCurrencyIraqiDinar(category.total),
                            tone = BudgetValueTone.Compact,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            unitLabel = "IQD",
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            InsightProgressBar(
                progress = progress,
                accent = accent,
            )
        }
    }
}

@Composable
private fun InsightSectionHeader(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                modifier = Modifier.clickable(onClick = onAction),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun InsightHeroBadge(
    label: String,
    tint: Color,
) {
    Surface(
        color = tint.copy(alpha = 0.12f),
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = tint,
        )
    }
}

@Composable
private fun InsightProgressBar(
    progress: Float,
    accent: Color,
) {
    val animatedProgress = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "insightProgress",
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress.value)
                .height(10.dp)
                .background(accent)
        )
    }
}

@Composable
private fun comparisonAccent(direction: ComparisonDirection): Color = when (direction) {
    ComparisonDirection.Up -> BudgetTheme.extendedColors.danger
    ComparisonDirection.Down -> BudgetTheme.extendedColors.success
    ComparisonDirection.Flat -> MaterialTheme.colorScheme.primary
}

private fun buildSummaryLine(
    scope: InsightScope,
    comparison: String,
    topCategory: CategoryTotalUi?,
): String {
    val categoryPart = topCategory?.label ?: "your categories"
    return when {
        comparison.isBlank() && scope == InsightScope.Month ->
            "This month is still forming. Watch $categoryPart to see where spending starts to collect."

        comparison.isBlank() ->
            "This year is still forming. Watch $categoryPart to see where the most weight is building."

        scope == InsightScope.Month ->
            "$comparison versus the last month, with the strongest pull coming from $categoryPart."

        else ->
            "$comparison versus the last year, with the strongest pull coming from $categoryPart."
    }
}
