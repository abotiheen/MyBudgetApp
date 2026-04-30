package com.example.mybudgetapp.ui.shared.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.data.NavigationItems
import com.example.mybudgetapp.ui.theme.BudgetTheme

@Composable
fun BottomNavigationBar(
    navigateToThisMonthScreen: () -> Unit,
    navigateToThisYearScreen: () -> Unit,
    navigateToCloudBackupScreen: () -> Unit,
    selectedItemIndex: Int,
) {
    val spacing = BudgetTheme.spacing
    AnimatedSegmentedControl(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.xs),
        selectedIndex = selectedItemIndex,
        itemCount = NavigationItems.items.size,
        onItemSelected = { index ->
            when (index) {
                0 -> navigateToThisYearScreen()
                1 -> navigateToThisMonthScreen()
                else -> navigateToCloudBackupScreen()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        indicatorColor = MaterialTheme.colorScheme.primary,
        borderColor = BudgetTheme.extendedColors.edge,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        itemShape = RoundedCornerShape(BudgetTheme.radii.md),
        shadowElevation = BudgetTheme.elevations.level3,
        contentPadding = 6.dp,
        itemSpacing = 4.dp,
        itemMinHeight = 48.dp,
    ) { index, selected ->
        val item = NavigationItems.items[index]
        val contentColor = animateColorAsState(
            targetValue = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            animationSpec = tween(durationMillis = 220),
            label = "bottomNavContentColor",
        )

        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                painter = if (selected) {
                    painterResource(id = item.selectedIcon)
                } else {
                    painterResource(id = item.unSelectedIcon)
                },
                contentDescription = item.title,
                modifier = Modifier.size(20.dp),
                tint = contentColor.value,
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.value,
            )
        }
    }
}
