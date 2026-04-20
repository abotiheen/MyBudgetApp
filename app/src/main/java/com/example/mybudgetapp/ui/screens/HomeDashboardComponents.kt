package com.example.mybudgetapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.HomeTransactionPreview
import com.example.mybudgetapp.ui.widgets.BudgetValueText
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.FractionProgressBar
import com.example.mybudgetapp.ui.widgets.categoryAccentColor
import com.example.mybudgetapp.ui.widgets.categoryIconPainter

data class DashboardQuickStat(
    val label: String,
    val value: String,
    val note: String,
    val highlight: Color,
    val valueTone: BudgetValueTone? = null,
    val noteTone: BudgetValueTone? = null,
    val valueUnitLabel: String? = null,
    val noteUnitLabel: String? = null,
)

data class DashboardLaneUi(
    val label: String,
    val amount: String,
    val progress: Float,
    val accent: Color,
    val iconKey: String,
    val categoryKey: String,
    val onClick: () -> Unit,
)

data class DashboardCardAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun DashboardCommandDeck(
    title: String,
    selectedPeriodLabel: String,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenPicker: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Surface(
                modifier = Modifier.clickable(onClick = onOpenPicker),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onPrevious,
                        enabled = canNavigatePrevious,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = if (canNavigatePrevious) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                            },
                        )
                    }
                    AnimatedContent(
                        targetState = selectedPeriodLabel,
                        label = "dashboardPeriodLabel",
                    ) { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(
                        onClick = onNext,
                        enabled = canNavigateNext,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (canNavigateNext) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardBalanceHero(
    periodLabel: String,
    balanceLabel: String,
    balanceValue: String,
    statusLabel: String,
    incomeValue: String,
    spendingValue: String,
    onOpenIncome: () -> Unit,
    onOpenSpending: () -> Unit,
    onOpenInsights: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
        onClick = onOpenInsights,
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
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.xs)) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                    )
                    Text(
                        text = balanceLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(BudgetTheme.radii.pill),
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            BudgetValueText(
                text = balanceValue,
                modifier = Modifier.fillMaxWidth(),
                tone = BudgetValueTone.Hero,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                unitLabel = "IQD",
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                DashboardFlowChip(
                    label = "Income",
                    value = incomeValue,
                    accent = BudgetTheme.extendedColors.income,
                    onClick = onOpenIncome,
                )
                DashboardFlowChip(
                    label = "Spent",
                    value = spendingValue,
                    accent = BudgetTheme.extendedColors.danger,
                    onClick = onOpenSpending,
                )
            }
            Surface(
                modifier = Modifier.clickable(onClick = onOpenInsights),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                shape = RoundedCornerShape(BudgetTheme.radii.pill),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Open insights",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardFlowChip(
    label: String,
    value: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
            )
            BudgetValueText(
                text = value,
                tone = BudgetValueTone.Prominent,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                unitLabel = "IQD",
            )
        }
    }
}

@Composable
fun DashboardQuickStatsCard(
    stats: List<DashboardQuickStat>,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level1),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            stats.forEach { stat ->
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stat.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (stat.valueTone != null) {
                            BudgetValueText(
                                text = stat.value,
                                tone = stat.valueTone,
                                color = stat.highlight,
                                unitLabel = stat.valueUnitLabel,
                            )
                        } else {
                            Text(
                                text = stat.value,
                                style = MaterialTheme.typography.titleMedium,
                                color = stat.highlight,
                                maxLines = 2,
                            )
                        }
                        if (stat.noteTone != null) {
                            BudgetValueText(
                                text = stat.note,
                                tone = stat.noteTone,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                unitLabel = stat.noteUnitLabel,
                            )
                        } else {
                            Text(
                                text = stat.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardLanesCard(
    title: String,
    subtitle: String,
    lanes: List<DashboardLaneUi>,
    actions: List<DashboardCardAction> = emptyList(),
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
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
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (actions.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        actions.forEach { action ->
                            Text(
                                text = action.label,
                                modifier = Modifier.clickable(onClick = action.onClick),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            lanes.forEachIndexed { index, lane ->
                DashboardLaneRow(lane = lane)
                if (index != lanes.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BudgetTheme.extendedColors.edge)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardLaneRow(
    lane: DashboardLaneUi,
) {
    val animatedProgress = animateFloatAsState(
        targetValue = lane.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "dashboardLaneProgress",
    )
    val iconPainter = categoryIconPainter(lane.iconKey, lane.categoryKey)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = lane.onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = lane.accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(BudgetTheme.radii.md),
                ) {
                    Box(
                        modifier = Modifier.size(46.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = null,
                            tint = lane.accent,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = lane.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    BudgetValueText(
                        text = lane.amount,
                        tone = BudgetValueTone.Compact,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        unitLabel = "IQD",
                    )
                }
            }
            Text(
                text = "${(lane.progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = lane.accent,
            )
        }
        FractionProgressBar(
            progress = animatedProgress.value,
            fillColor = lane.accent,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            barHeight = 8.dp,
        )
    }
}

@Composable
fun DashboardActivityCard(
    title: String,
    subtitle: String,
    items: List<HomeTransactionPreview>,
    onViewAll: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level1),
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "View all",
                    modifier = Modifier.clickable(onClick = onViewAll),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (items.isEmpty()) {
                Text(
                    text = "No entries yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                items.forEachIndexed { index, item ->
                    DashboardActivityRow(item = item)
                    if (index != items.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BudgetTheme.extendedColors.edge)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardActivityRow(
    item: HomeTransactionPreview,
) {
    val accent = categoryAccentColor(item.categoryColorHex, item.categoryKey)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accent, CircleShape)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${item.category} • ${item.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        BudgetValueText(
            text = item.amount,
            tone = BudgetValueTone.Compact,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            unitLabel = "IQD",
        )
    }
}
