package com.example.mybudgetapp.ui.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.ui.theme.BudgetTheme

private val SelectionMotionSpec = spring<Dp>(
    dampingRatio = 0.9f,
    stiffness = Spring.StiffnessMediumLow,
)

@Composable
fun AnimatedSegmentedControl(
    selectedIndex: Int,
    itemCount: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = Color.Transparent,
    shape: Shape = RoundedCornerShape(BudgetTheme.radii.pill),
    itemShape: Shape = RoundedCornerShape(BudgetTheme.radii.pill),
    shadowElevation: Dp = 0.dp,
    tonalElevation: Dp = 0.dp,
    contentPadding: Dp = 6.dp,
    itemSpacing: Dp = 6.dp,
    itemMinHeight: Dp = 44.dp,
    itemContent: @Composable (index: Int, selected: Boolean) -> Unit,
) {
    if (itemCount <= 0) return

    val clampedIndex = selectedIndex.coerceIn(0, itemCount - 1)
    var trackWidthPx by remember { mutableIntStateOf(0) }
    var trackHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val spacingPx = with(density) { itemSpacing.roundToPx() }
    val segmentWidthPx = if (trackWidthPx > 0) {
        (trackWidthPx - spacingPx * (itemCount - 1)) / itemCount
    } else {
        0
    }
    val animatedIndicatorOffset by animateDpAsState(
        targetValue = with(density) {
            ((segmentWidthPx + spacingPx) * clampedIndex).toDp()
        },
        animationSpec = SelectionMotionSpec,
        label = "segmentedIndicatorOffset",
    )
    val indicatorWidth = with(density) { segmentWidthPx.coerceAtLeast(0).toDp() }
    val indicatorHeight = with(density) { trackHeightPx.coerceAtLeast(0).toDp() }

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = shape,
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation,
        border = if (borderColor == Color.Transparent) {
            null
        } else {
            BorderStroke(width = 1.dp, color = borderColor)
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            if (trackWidthPx > 0 && trackHeightPx > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = animatedIndicatorOffset)
                        .width(indicatorWidth)
                        .defaultMinSize(minHeight = indicatorHeight)
                        .clip(itemShape)
                        .background(indicatorColor)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        trackWidthPx = size.width
                        trackHeightPx = size.height
                    },
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            ) {
                repeat(itemCount) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(itemShape)
                            .clickable { onItemSelected(index) }
                            .defaultMinSize(minHeight = itemMinHeight),
                        contentAlignment = Alignment.Center,
                    ) {
                        itemContent(index, index == clampedIndex)
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedTextLabel(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val contentColor = animateColorAsState(
        targetValue = if (selected) selectedColor else unselectedColor,
        label = "segmentedTextColor",
    )

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        color = contentColor.value,
    )
}
