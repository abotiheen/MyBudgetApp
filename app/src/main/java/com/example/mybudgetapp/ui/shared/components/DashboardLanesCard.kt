package com.example.mybudgetapp.ui.shared.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.ui.shared.models.DashboardCardAction
import com.example.mybudgetapp.ui.shared.models.DashboardLaneUi
import com.example.mybudgetapp.ui.shared.widgets.BudgetValueText
import com.example.mybudgetapp.ui.shared.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.shared.widgets.CategoryIcon
import com.example.mybudgetapp.ui.shared.widgets.FractionProgressBar
import com.example.mybudgetapp.ui.theme.BudgetTheme

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
                        CategoryIcon(
                            iconKey = lane.iconKey,
                            fallbackCategoryKey = lane.categoryKey,
                            tint = lane.accent,
                            size = 24.dp,
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
