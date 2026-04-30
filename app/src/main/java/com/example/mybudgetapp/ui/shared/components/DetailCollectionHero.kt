package com.example.mybudgetapp.ui.shared.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.ui.shared.models.DetailHeroChipUi
import com.example.mybudgetapp.ui.shared.widgets.BudgetValueText
import com.example.mybudgetapp.ui.shared.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.shared.widgets.CategoryIcon
import com.example.mybudgetapp.ui.theme.BudgetTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailCollectionHero(
    title: String,
    periodLabel: String,
    totalValue: String,
    subtitle: String,
    badgeLabel: String,
    accent: Color,
    iconKey: String,
    fallbackCategoryKey: String = "",
    chips: List<DetailHeroChipUi>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = accent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(BudgetTheme.radii.md),
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CategoryIcon(
                            iconKey = iconKey,
                            fallbackCategoryKey = fallbackCategoryKey,
                            tint = accent,
                            size = 24.dp,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(BudgetTheme.radii.pill),
            ) {
                Text(
                    text = badgeLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BudgetValueText(
                    text = totalValue,
                    modifier = Modifier.fillMaxWidth(),
                    tone = BudgetValueTone.Hero,
                    color = MaterialTheme.colorScheme.onSurface,
                    unitLabel = "IQD",
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                chips.forEach { chip ->
                    DetailHeroChip(
                        label = chip.label,
                        value = chip.value,
                        isMonetary = chip.isMonetary,
                        accent = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHeroChip(
    label: String,
    value: String,
    isMonetary: Boolean,
    accent: Color,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BudgetValueText(
                text = value,
                tone = BudgetValueTone.Compact,
                color = MaterialTheme.colorScheme.onSurface,
                unitLabel = if (isMonetary) "IQD" else null,
            )
        }
    }
}
