package com.example.mybudgetapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.CategoryBreakdownCategoryUi
import com.example.mybudgetapp.ui.viewmodels.CategoryBreakdownUiState
import com.example.mybudgetapp.ui.viewmodels.CategoryBreakdownViewModel
import com.example.mybudgetapp.ui.viewmodels.InsightScope
import com.example.mybudgetapp.ui.widgets.AnimatedSegmentedControl
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.BudgetValueText
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.CategoryIcon
import com.example.mybudgetapp.ui.widgets.SegmentedTextLabel
import com.example.mybudgetapp.ui.widgets.categoryAccentColor

object CategoryBreakdownDestination : NavigationDestination {
    override val route = "CategoryBreakdown"
    override val titleRes = com.example.mybudgetapp.R.string.spending_on_category_screen
    const val scope = "scope"
    const val year = "year"
    const val month = "month"
    val routeWithArgs = "$route/{$scope}/{$year}/{$month}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBreakdownScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    navigateToCategories: () -> Unit,
    navigateToSpendingOnCategory: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: CategoryBreakdownViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                title = "Category breakdown",
                navigateBack = navigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        BudgetBackdrop(modifier = Modifier.padding(paddingValues)) {
            CategoryBreakdownBody(
                uiState = uiState.value,
                onToggleType = viewModel::selectType,
                navigateToCategories = navigateToCategories,
                navigateToSpendingOnCategory = navigateToSpendingOnCategory,
            )
        }
    }
}

@Composable
private fun CategoryBreakdownBody(
    modifier: Modifier = Modifier,
    uiState: CategoryBreakdownUiState,
    onToggleType: (Boolean) -> Unit,
    navigateToCategories: () -> Unit,
    navigateToSpendingOnCategory: (String) -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val heroAccent = if (uiState.isIncome) {
        BudgetTheme.extendedColors.income
    } else {
        BudgetTheme.extendedColors.danger
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = spacing.lg,
            bottom = 40.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            AnimatedSegmentedControl(
                selectedIndex = if (uiState.isIncome) 1 else 0,
                itemCount = 2,
                onItemSelected = { index -> onToggleType(index == 1) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                indicatorColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(BudgetTheme.radii.pill),
                itemShape = RoundedCornerShape(BudgetTheme.radii.pill),
                shadowElevation = BudgetTheme.elevations.level2,
                itemSpacing = 6.dp,
                itemMinHeight = 46.dp,
            ) { index, selected ->
                SegmentedTextLabel(
                    text = if (index == 0) "Spending" else "Income",
                    selected = selected,
                )
            }
        }
        item {
            CategoryBreakdownHero(
                periodLabel = uiState.periodLabel,
                isIncome = uiState.isIncome,
                totalLabel = uiState.totalLabel,
                categoryCount = uiState.categories.size,
                accent = heroAccent,
                iconKey = if (uiState.isIncome) "income" else "bills",
                fallbackCategoryKey = if (uiState.isIncome) "income" else "others",
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (uiState.isIncome) "Income categories" else "Spending categories",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (uiState.isIncome) {
                            "See how each income category contributes to the total for this period."
                        } else {
                            "See which categories are carrying the most weight in this period."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "Manage",
                    modifier = Modifier.clickable(onClick = navigateToCategories),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (uiState.categories.isEmpty()) {
            item {
                EmptyBreakdownCard(
                    isIncome = uiState.isIncome,
                    accent = heroAccent,
                    iconKey = if (uiState.isIncome) "income" else "bills",
                    fallbackCategoryKey = if (uiState.isIncome) "income" else "others",
                )
            }
        } else {
            items(uiState.categories, key = { it.categoryKey }) { category ->
                CategoryBreakdownRow(
                    category = category,
                    isIncome = uiState.isIncome,
                    onClick = {
                        if (category.canOpenDetails) {
                            navigateToSpendingOnCategory(category.categoryKey)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownHero(
    periodLabel: String,
    isIncome: Boolean,
    totalLabel: String,
    categoryCount: Int,
    accent: Color,
    iconKey: String,
    fallbackCategoryKey: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        text = if (isIncome) "Income breakdown" else "Spending breakdown",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            BudgetValueText(
                text = totalLabel,
                modifier = Modifier.fillMaxWidth(),
                tone = BudgetValueTone.Hero,
                color = MaterialTheme.colorScheme.onSurface,
                unitLabel = "IQD",
            )

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(BudgetTheme.radii.pill),
            ) {
                Text(
                    text = "$categoryCount categories in this view",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
    category: CategoryBreakdownCategoryUi,
    isIncome: Boolean,
    onClick: () -> Unit,
) {
    val accent = categoryAccentColor(category.colorHex, category.categoryKey)
    val progress = animateFloatAsState(
        targetValue = category.ratio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 420),
        label = "categoryBreakdownProgress",
    )

    Surface(
        onClick = onClick,
        enabled = category.canOpenDetails,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
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
                        color = accent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(BudgetTheme.radii.md),
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CategoryIcon(
                                iconKey = category.iconKey,
                                fallbackCategoryKey = category.categoryKey,
                                tint = accent,
                                size = 24.dp,
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (category.isArchived) {
                                Text(
                                    text = "Archived",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            text = if (isIncome) "Share of income" else "Share of spending",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    BudgetValueText(
                        text = category.totalLabel,
                        tone = BudgetValueTone.Compact,
                        color = MaterialTheme.colorScheme.onSurface,
                        unitLabel = "IQD",
                    )
                    Text(
                        text = "${(category.ratio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(BudgetTheme.radii.pill),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.value)
                            .height(8.dp)
                            .background(accent),
                    )
                }
            }

            if (category.canOpenDetails) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Open details",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyBreakdownCard(
    isIncome: Boolean,
    accent: Color,
    iconKey: String,
    fallbackCategoryKey: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = if (isIncome) "No income categories yet" else "No spending categories yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isIncome) {
                        "Income category totals will appear here once entries are recorded."
                    } else {
                        "Spending category totals will appear here once expenses are recorded."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
