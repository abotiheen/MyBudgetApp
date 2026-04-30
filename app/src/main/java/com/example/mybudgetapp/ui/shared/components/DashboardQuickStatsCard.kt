package com.example.mybudgetapp.ui.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.ui.shared.models.DashboardQuickStat
import com.example.mybudgetapp.ui.shared.widgets.BudgetValueText
import com.example.mybudgetapp.ui.theme.BudgetTheme

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
