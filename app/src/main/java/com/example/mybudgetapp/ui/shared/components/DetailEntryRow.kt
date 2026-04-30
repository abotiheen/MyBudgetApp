package com.example.mybudgetapp.ui.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mybudgetapp.ui.shared.widgets.BudgetValueText
import com.example.mybudgetapp.ui.shared.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.shared.widgets.CategoryIcon
import com.example.mybudgetapp.ui.shared.widgets.categoryPlaceholderPainter
import com.example.mybudgetapp.ui.theme.BudgetTheme

@Composable
fun DetailEntryRow(
    title: String,
    amount: String,
    meta: String,
    imagePath: String?,
    iconKey: String,
    fallbackCategoryKey: String = "",
    accent: Color,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onOpen,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = accent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(BudgetTheme.radii.md),
            ) {
                if (imagePath != null) {
                    AsyncImage(
                        model = imagePath,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = categoryPlaceholderPainter(iconKey, fallbackCategoryKey),
                        error = categoryPlaceholderPainter(iconKey, fallbackCategoryKey),
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(BudgetTheme.radii.md)),
                    )
                } else {
                    Box(
                        modifier = Modifier.size(52.dp),
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
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                BudgetValueText(
                    text = amount,
                    tone = BudgetValueTone.Compact,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    unitLabel = "IQD",
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
