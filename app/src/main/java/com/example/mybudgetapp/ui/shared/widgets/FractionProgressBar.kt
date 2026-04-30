package com.example.mybudgetapp.ui.shared.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import com.example.mybudgetapp.ui.theme.BudgetTheme
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FractionProgressBar(
    progress: Float,
    fillColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    barHeight: Dp = 10.dp,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight),
        color = trackColor,
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedProgress)
                    .height(barHeight)
                    .background(fillColor),
            )
        }
    }
}
