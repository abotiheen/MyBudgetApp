package com.example.mybudgetapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.domain.insights.CategoryDelta
import com.example.mybudgetapp.domain.insights.InsightAction
import com.example.mybudgetapp.domain.insights.InsightSeverity
import com.example.mybudgetapp.domain.insights.InsightUi
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.shared.widgets.FractionProgressBar
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.CategoryTotalUi
import com.example.mybudgetapp.ui.viewmodels.InsightFilterCategoryUi
import com.example.mybudgetapp.ui.viewmodels.InsightScope
import com.example.mybudgetapp.ui.viewmodels.InsightsEmptyState
import com.example.mybudgetapp.ui.viewmodels.InsightsUiState
import com.example.mybudgetapp.ui.viewmodels.InsightsViewModel
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.BudgetValueText
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.CategoryIcon
import com.example.mybudgetapp.ui.widgets.TrendChartCard
import com.example.mybudgetapp.ui.widgets.categoryAccentColor
import kotlin.math.abs
import kotlin.math.roundToInt

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
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

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
            InsightsV2Screen(
                uiState = uiState,
                onOpenFilter = { showFilterSheet = true },
                onManageCategories = navigateToCategories,
                onInsightAction = { action ->
                    if (action is InsightAction.OpenCategory) {
                        navigateToSpendingOnCategory(action.categoryKey)
                    }
                },
                navigateToSpendingOnCategory = navigateToSpendingOnCategory,
            )
        }
    }

    if (showFilterSheet) {
        InsightsCategoryFilterBottomSheet(
            categories = uiState.availableCategories,
            onToggleCategory = viewModel::toggleCategory,
            onSelectAll = viewModel::selectAllCategories,
            onClearAll = viewModel::clearAllCategories,
            onDismissRequest = { showFilterSheet = false },
        )
    }
}

@Composable
fun InsightsV2Screen(
    uiState: InsightsUiState,
    onOpenFilter: () -> Unit,
    onManageCategories: () -> Unit,
    onInsightAction: (InsightAction?) -> Unit,
    navigateToSpendingOnCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.xl),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = BudgetTheme.spacing.lg,
            end = BudgetTheme.spacing.lg,
            top = BudgetTheme.spacing.lg,
            bottom = 48.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.lg),
    ) {
        item {
            InsightsHeroCard(uiState = uiState)
        }
        item {
            InsightsFilterBar(
                summary = uiState.selectedCategoriesSummary,
                isFilterActive = uiState.isFilterActive,
                onOpenFilter = onOpenFilter,
            )
        }
        uiState.emptyState?.let { emptyState ->
            item {
                InsightsEmptyStateCard(
                    emptyState = emptyState,
                    onPrimaryAction = if (uiState.hasNoIncludedCategories) onOpenFilter else onManageCategories,
                )
            }
        }
        if (uiState.emptyState == null) {
            item {
                InsightSectionHeader(
                    title = "What needs attention",
                    subtitle = "Ranked by surprise, usefulness, and whether there is a clear next step.",
                )
            }
            if (uiState.primaryInsights.isEmpty()) {
                item {
                    NoUrgentInsightsCard()
                }
            } else {
                items(uiState.primaryInsights, key = { it.id }) { insight ->
                    InsightCard(
                        insight = insight,
                        onAction = onInsightAction,
                    )
                }
            }
            uiState.savingOpportunity?.let { opportunity ->
                item {
                    InsightSectionHeader(
                        title = "Opportunity",
                        subtitle = "A realistic place to save without turning the whole budget upside down.",
                    )
                }
                item {
                    SavingOpportunityCard(
                        insight = opportunity,
                        onAction = onInsightAction,
                    )
                }
            }
            item {
                InsightSectionHeader(
                    title = "Trend",
                    subtitle = uiState.trendAnnotation,
                )
            }
            item {
                TrendChartCard(
                    title = uiState.trendTitle,
                    subtitle = uiState.trendSubtitle,
                    points = uiState.trendPoints,
                )
            }
            if (uiState.secondaryInsights.isNotEmpty()) {
                item {
                    InsightSectionHeader(
                        title = "More signals",
                        subtitle = "Secondary patterns that can help explain the period.",
                    )
                }
                items(uiState.secondaryInsights, key = { it.id }) { insight ->
                    InsightCard(
                        insight = insight,
                        onAction = onInsightAction,
                    )
                }
            }
            item {
                InsightSectionHeader(
                    title = "Category breakdown",
                    subtitle = "Use this after the insight cards to inspect the raw distribution.",
                    actionLabel = "Manage",
                    onAction = onManageCategories,
                )
            }
            items(uiState.categoryTotals, key = { it.category }) { category ->
                CategoryBreakdownRow(
                    category = category,
                    totalSpendingAmount = uiState.totalSpendingAmount,
                    onClick = { navigateToSpendingOnCategory(category.category) },
                )
            }
        }
    }
}

@Composable
fun InsightsHeroCard(
    uiState: InsightsUiState,
    modifier: Modifier = Modifier,
) {
    val severityColor = severityColor(uiState.status.severity)
    val statusIcon = when (uiState.status.severity) {
        InsightSeverity.Positive -> Icons.AutoMirrored.Filled.TrendingDown
        InsightSeverity.Neutral -> Icons.AutoMirrored.Filled.TrendingFlat
        InsightSeverity.Warning,
        InsightSeverity.Danger -> Icons.AutoMirrored.Filled.TrendingUp
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
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
                        text = if (uiState.scope == InsightScope.Month) "Insights V2" else "Year insights",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                    )
                    Text(
                        text = uiState.periodLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                InsightSeverityPill(
                    severity = uiState.status.severity,
                    label = uiState.status.label,
                )
            }

            BudgetValueText(
                text = uiState.totalSpending,
                modifier = Modifier.fillMaxWidth(),
                tone = BudgetValueTone.Hero,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                unitLabel = "IQD",
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = severityColor.copy(alpha = 0.14f),
                    shape = CircleShape,
                ) {
                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = severityColor,
                        )
                    }
                }
                Text(
                    text = uiState.status.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                )
            }

            uiState.status.monthProgress?.let { progress ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Month progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                        )
                        Text(
                            text = "${(progress * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    InsightProgressBar(
                        progress = progress,
                        accent = severityColor,
                    )
                }
            }
        }
    }
}

@Composable
fun InsightCard(
    insight: InsightUi,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (insight) {
        is InsightUi.Forecast -> ForecastInsightCard(insight, onAction, modifier)
        is InsightUi.BaselineComparison -> BaselineInsightCard(insight, onAction, modifier)
        is InsightUi.CategoryAnomaly -> CategoryAnomalyCard(insight, onAction, modifier)
        is InsightUi.SavingOpportunity -> SavingOpportunityCard(insight, onAction, modifier)
        is InsightUi.DriverAnalysis -> DriverAnalysisCard(insight, onAction, modifier)
        is InsightUi.RecurringVsFlexible -> RecurringFlexibleCard(insight, onAction, modifier)
        is InsightUi.TimePattern -> TimePatternCard(insight, onAction, modifier)
        is InsightUi.SpendingStreak -> SpendingStreakCard(insight, onAction, modifier)
        is InsightUi.TransactionBehavior -> TransactionBehaviorCard(insight, onAction, modifier)
        is InsightUi.ConcentrationRisk -> ConcentrationRiskCard(insight, onAction, modifier)
    }
}

@Composable
fun ForecastInsightCard(
    insight: InsightUi.Forecast,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.Default.QueryStats,
        actionLabel = "Show pace",
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
fun BaselineInsightCard(
    insight: InsightUi.BaselineComparison,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.Default.Assessment,
        actionLabel = "Compare",
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
fun CategoryAnomalyCard(
    insight: InsightUi.CategoryAnomaly,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.Default.ReportProblem,
        actionLabel = "View ${insight.categoryLabel}",
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
fun SavingOpportunityCard(
    insight: InsightUi.SavingOpportunity,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.Default.Savings,
        actionLabel = "View ${insight.categoryLabel}",
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
fun DriverAnalysisCard(
    insight: InsightUi.DriverAnalysis,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.AutoMirrored.Filled.AltRoute,
        actionLabel = "Show details",
        onAction = onAction,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            insight.drivers.forEach { driver ->
                CategoryDeltaRow(driver = driver)
            }
        }
    }
}

@Composable
fun RecurringFlexibleCard(
    insight: InsightUi.RecurringVsFlexible,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.Default.Repeat,
        actionLabel = "Show details",
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
fun TimePatternCard(
    insight: InsightUi.TimePattern,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.Default.CalendarToday,
        actionLabel = "See trend",
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
fun SpendingStreakCard(
    insight: InsightUi.SpendingStreak,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.Default.CheckCircle,
        actionLabel = "See trend",
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
fun TransactionBehaviorCard(
    insight: InsightUi.TransactionBehavior,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        actionLabel = "Show details",
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
fun ConcentrationRiskCard(
    insight: InsightUi.ConcentrationRisk,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TypedInsightCard(
        insight = insight,
        icon = Icons.Default.PieChart,
        actionLabel = "View ${insight.categoryLabel}",
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
private fun TypedInsightCard(
    insight: InsightUi,
    icon: ImageVector,
    actionLabel: String,
    onAction: (InsightAction?) -> Unit,
    modifier: Modifier = Modifier,
    extraContent: @Composable (() -> Unit)? = null,
) {
    var expanded by rememberSaveable(insight.id) { mutableStateOf(false) }
    val accent = severityColor(insight.severity)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 220)),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.14f)),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                verticalAlignment = Alignment.Top,
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
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = insight.text.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        InsightSeverityPill(
                            severity = insight.severity,
                            compact = true,
                        )
                    }
                    Text(
                        text = insight.text.value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = insight.text.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            extraContent?.invoke()

            AnimatedVisibility(visible = expanded) {
                InsightExplanationSection(insight = insight)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide details" else "Why this?")
                }
                if (insight.action != null && insight.action != InsightAction.None) {
                    TextButton(
                        onClick = {
                            if (insight.action is InsightAction.OpenCategory) {
                                onAction(insight.action)
                            } else {
                                expanded = true
                                onAction(insight.action)
                            }
                        },
                    ) {
                        Text(actionLabel)
                        Spacer(modifier = Modifier.size(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsightExplanationSheet(
    insight: InsightUi,
    modifier: Modifier = Modifier,
) {
    InsightExplanationSection(insight = insight, modifier = modifier)
}

@Composable
private fun InsightExplanationSection(
    insight: InsightUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(BudgetTheme.radii.md),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (insight.text.explanation.isNotBlank()) {
                Text(
                    text = insight.text.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (insight.text.recommendation.isNotBlank()) {
                Text(
                    text = insight.text.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
fun InsightSeverityPill(
    severity: InsightSeverity,
    modifier: Modifier = Modifier,
    label: String? = null,
    compact: Boolean = false,
) {
    val tint = severityColor(severity)
    val text = label ?: when (severity) {
        InsightSeverity.Positive -> "Good"
        InsightSeverity.Neutral -> "Info"
        InsightSeverity.Warning -> "Watch"
        InsightSeverity.Danger -> "High"
    }
    Surface(
        modifier = modifier,
        color = tint.copy(alpha = 0.12f),
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 6.dp else 8.dp,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun InsightProgressBar(
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val animatedProgress = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "insightProgress",
    )

    FractionProgressBar(
        progress = animatedProgress.value,
        fillColor = accent,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        barHeight = 10.dp,
        modifier = modifier,
    )
}

@Composable
fun CategoryDeltaRow(
    driver: CategoryDelta,
    modifier: Modifier = Modifier,
) {
    val isIncrease = driver.deltaAmount >= 0.0
    val tint = if (isIncrease) BudgetTheme.extendedColors.danger else BudgetTheme.extendedColors.success
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = tint.copy(alpha = 0.08f),
        shape = RoundedCornerShape(BudgetTheme.radii.md),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = BudgetTheme.spacing.md, vertical = BudgetTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = driver.categoryLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${if (isIncrease) "+" else "-"}${formatCompactCurrencyIraqiDinar(abs(driver.deltaAmount))} IQD",
                style = MaterialTheme.typography.labelLarge,
                color = tint,
                fontWeight = FontWeight.Bold,
            )
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
    val progress = if (totalSpendingAmount > 0.0) {
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
                            CategoryIcon(
                                iconKey = category.iconKey,
                                fallbackCategoryKey = category.category,
                                tint = accent,
                                size = 24.dp,
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
                        text = "${(progress * 100).roundToInt()}%",
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
            InsightProgressBar(progress = progress, accent = accent)
        }
    }
}

@Composable
private fun InsightsFilterBar(
    summary: String,
    isFilterActive: Boolean,
    onOpenFilter: () -> Unit,
) {
    Surface(
        onClick = onOpenFilter,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        tonalElevation = BudgetTheme.elevations.level1,
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BudgetTheme.spacing.lg, vertical = BudgetTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(BudgetTheme.radii.md),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Included categories",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (isFilterActive) {
                InsightSeverityPill(
                    severity = InsightSeverity.Neutral,
                    label = "Filtered",
                    compact = true,
                )
            }
        }
    }
}

@Composable
private fun InsightsEmptyStateCard(
    emptyState: InsightsEmptyState,
    onPrimaryAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = emptyState.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = emptyState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = onPrimaryAction,
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            ) {
                Text(emptyState.actionLabel)
            }
        }
    }
}

@Composable
private fun NoUrgentInsightsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        tonalElevation = BudgetTheme.elevations.level1,
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "No urgent attention needed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Spending is not showing a strong anomaly yet. The trend and categories below still show the raw context.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InsightsCategoryFilterBottomSheet(
    categories: List<InsightFilterCategoryUi>,
    onToggleCategory: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BudgetTheme.spacing.lg)
                .padding(bottom = BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Text(
                text = "Include categories",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Only selected categories contribute to the insights on this screen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm)) {
                OutlinedButton(
                    onClick = onSelectAll,
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    Text("Select all")
                }
                OutlinedButton(
                    onClick = onClearAll,
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    Text("Clear all")
                }
            }
            if (categories.isEmpty()) {
                Text(
                    text = "No categories with spending are available in this period.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                categories.forEach { category ->
                    InsightCategoryFilterRow(
                        category = category,
                        onClick = { onToggleCategory(category.categoryKey) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(BudgetTheme.spacing.sm))
            Button(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            ) {
                Text("Apply")
            }
        }
    }
}

@Composable
private fun InsightCategoryFilterRow(
    category: InsightFilterCategoryUi,
    onClick: () -> Unit,
) {
    val accent = categoryAccentColor(category.colorHex, category.categoryKey)
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        tonalElevation = BudgetTheme.elevations.level1,
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BudgetTheme.spacing.md, vertical = BudgetTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = accent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(BudgetTheme.radii.md),
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CategoryIcon(
                        iconKey = category.iconKey,
                        fallbackCategoryKey = category.categoryKey,
                        tint = accent,
                        size = 24.dp,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = category.totalLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilterChip(
                selected = category.isSelected,
                onClick = onClick,
                label = {
                    Text(if (category.isSelected) "Included" else "Excluded")
                },
                leadingIcon = if (category.isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accent.copy(alpha = 0.18f),
                    selectedLabelColor = accent,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = category.isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = accent.copy(alpha = 0.24f),
                ),
            )
        }
    }
}

@Composable
private fun severityColor(severity: InsightSeverity): Color = when (severity) {
    InsightSeverity.Positive -> BudgetTheme.extendedColors.success
    InsightSeverity.Neutral -> MaterialTheme.colorScheme.primary
    InsightSeverity.Warning -> Color(0xFFB27A00)
    InsightSeverity.Danger -> MaterialTheme.colorScheme.error
}
