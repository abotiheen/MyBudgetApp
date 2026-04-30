package com.example.mybudgetapp.ui.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.ui.shared.widgets.CategoryIcon
import com.example.mybudgetapp.ui.theme.BudgetTheme

@Composable
fun DetailEmptyStateCard(
    title: String,
    message: String,
    accent: Color,
    iconKey: String,
    fallbackCategoryKey: String = "",
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level1),
    ) {
        Row(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
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
                        iconKey = iconKey,
                        fallbackCategoryKey = fallbackCategoryKey,
                        tint = accent,
                        size = 22.dp,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
