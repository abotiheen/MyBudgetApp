package com.example.mybudgetapp.ui.shared.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mybudgetapp.ui.theme.BudgetTheme

@Composable
fun ItemCardForDates(
    modifier: Modifier = Modifier,
    title: String,
    iconKey: String,
    fallbackCategoryKey: String = "",
    accentColor: Color,
    imagePath: String?,
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentColor.copy(alpha = 0.14f))
                    .border(
                        width = 1.dp,
                        color = BudgetTheme.extendedColors.edge,
                        shape = RoundedCornerShape(20.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (imagePath != null) {
                    AsyncImage(
                        model = imagePath,
                        contentDescription = null,
                        placeholder = categoryPlaceholderPainter(iconKey, fallbackCategoryKey),
                        error = categoryPlaceholderPainter(iconKey, fallbackCategoryKey),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    CategoryIcon(
                        iconKey = iconKey,
                        fallbackCategoryKey = fallbackCategoryKey,
                        tint = accentColor,
                        size = 28.dp,
                    )
                }
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
