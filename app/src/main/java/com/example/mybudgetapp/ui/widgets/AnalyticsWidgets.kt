package com.example.mybudgetapp.ui.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.ComparisonDirection
import com.example.mybudgetapp.ui.viewmodels.ComparisonInsightUi
import com.example.mybudgetapp.ui.viewmodels.InsightSection
import com.example.mybudgetapp.ui.viewmodels.StatInsightUi
import com.example.mybudgetapp.ui.viewmodels.TrendPointUi
import com.example.mybudgetapp.ui.viewmodels.comparisonColor
import kotlin.math.roundToInt

@Composable
fun TrendChartCard(
    title: String,
    subtitle: String,
    points: List<TrendPointUi>,
    modifier: Modifier = Modifier,
) {
    val maxValue = points.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1.0
    var selectedPointIndex by remember(points) { mutableIntStateOf(-1) }
    val selectedPoint = points.getOrNull(selectedPointIndex)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = BudgetTheme.extendedColors.edge,
                    shape = RoundedCornerShape(30.dp),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SectionHeading(title = title, subtitle = subtitle)
            AnimatedVisibility(
                visible = selectedPoint != null,
                enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                    expandVertically(
                        animationSpec = tween(durationMillis = 280),
                        expandFrom = Alignment.Top,
                    ),
                exit = fadeOut(animationSpec = tween(durationMillis = 140)) +
                    shrinkVertically(
                        animationSpec = tween(durationMillis = 220),
                        shrinkTowards = Alignment.Top,
                    ),
                label = "selectedTrendPointVisibility",
            ) {
                selectedPoint?.let { point ->
                    SelectedTrendPointSummary(point = point)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                points.forEachIndexed { index, point ->
                    val heightFraction = (point.value / maxValue).toFloat().coerceIn(0f, 1f)
                    val animatedHeightFraction = animateFloatAsState(
                        targetValue = heightFraction,
                        animationSpec = tween(durationMillis = 650),
                        label = "trendBarHeight",
                    )
                    val isSelected = selectedPointIndex == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(148.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Button,
                                    onClickLabel = "Show spending for ${point.detailLabel}",
                                ) { selectedPointIndex = index }
                                .semantics {
                                    selected = isSelected
                                    contentDescription = buildString {
                                        append(point.detailLabel)
                                        append(", ")
                                        append(formatCurrencyIraqiDinar(point.value))
                                        append(" IQD")
                                        if (isSelected) {
                                            append(", selected")
                                        }
                                    }
                                },
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                                    .background(
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                                        },
                                        shape = RoundedCornerShape(18.dp),
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
                                        },
                                        shape = RoundedCornerShape(18.dp),
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(
                                        (148 * animatedHeightFraction.value)
                                            .roundToInt()
                                            .coerceAtLeast(if (point.value > 0) 10 else 0)
                                            .dp
                                    )
                                    .background(
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                                        },
                                        shape = RoundedCornerShape(18.dp),
                                    )
                            )
                        }
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedTrendPointSummary(
    point: TrendPointUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 240)),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        ),
    ) {
        AnimatedContent(
            targetState = point,
            transitionSpec = {
                (
                    fadeIn(animationSpec = tween(durationMillis = 220)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = 260),
                            initialOffsetY = { it / 3 },
                        )
                    ).togetherWith(
                        fadeOut(animationSpec = tween(durationMillis = 140)) +
                            slideOutVertically(
                                animationSpec = tween(durationMillis = 180),
                                targetOffsetY = { -it / 4 },
                            )
                    ).using(
                        SizeTransform(clip = false)
                    )
            },
            label = "selectedTrendPointSummary",
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = it.detailLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BudgetValueText(
                    text = formatCurrencyIraqiDinar(it.value),
                    tone = BudgetValueTone.Card,
                    color = MaterialTheme.colorScheme.onSurface,
                    unitLabel = "IQD",
                )
            }
        }
    }
}

@Composable
fun ComparisonCard(
    comparison: ComparisonInsightUi,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = BudgetTheme.extendedColors.edge,
                    shape = RoundedCornerShape(30.dp),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = comparison.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BudgetValueText(
                text = comparison.summary,
                tone = BudgetValueTone.Prominent,
                color = comparisonColor(comparison.direction),
            )
            Surface(
                color = comparisonColor(comparison.direction).copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = comparisonFootnote(comparison.direction),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = comparisonColor(comparison.direction),
                )
            }
        }
    }
}

@Composable
fun InsightGrid(
    insights: List<StatInsightUi>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        insights.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowItems.forEach { insight ->
                    InsightCard(
                        insight = insight,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InsightCard(
    insight: StatInsightUi,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = BudgetTheme.extendedColors.edge,
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BudgetValueText(
                text = insight.value,
                tone = BudgetValueTone.Card,
                unitLabel = if (insight.isMonetary) "IQD" else null,
            )
            if (insight.subtitle.isNotBlank()) {
                Text(
                    text = insight.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun InsightsEntryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
fun InsightsSectionRow(
    selectedSection: InsightSection,
    onSectionSelected: (InsightSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InsightSection.entries.forEach { section ->
            FilterChip(
                selected = section == selectedSection,
                onClick = { onSectionSelected(section) },
                label = { Text(section.name) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = section == selectedSection,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

private fun comparisonFootnote(direction: ComparisonDirection): String = when (direction) {
    ComparisonDirection.Up -> "Spending increased"
    ComparisonDirection.Down -> "Spending decreased"
    ComparisonDirection.Flat -> "No significant change"
}
