package com.example.mybudgetapp.ui.shared.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.ui.theme.BudgetTheme

@Composable
fun BudgetBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val extendedColors = BudgetTheme.extendedColors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        extendedColors.mist,
                        extendedColors.canvas,
                        MaterialTheme.colorScheme.surface,
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 12.dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                            Color.Transparent,
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 2.dp, top = 112.dp)
                .size(width = 186.dp, height = 140.dp)
                .clip(RoundedCornerShape(52.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.52f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 136.dp)
                .size(168.dp)
                .clip(CircleShape)
                .background(extendedColors.accentGold.copy(alpha = 0.13f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, bottom = 220.dp)
                .size(width = 124.dp, height = 18.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
        )
        content()
    }
}
