package com.example.mybudgetapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.SpendingCategoryDisplayObject
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.SpendingOnCategoryForYearScreenViewModel
import com.example.mybudgetapp.ui.viewmodels.SpendingOnCategoryUiState
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.BudgetValueText
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.ItemCard
import com.example.mybudgetapp.ui.widgets.SectionHeading

object SpendingOnCategoryForYearDestination : NavigationDestination {
    override val route = "SpendingOnCategory"
    override val titleRes = R.string.spending_on_category_screen
    const val category = "category"
    const val year = 0
    val routeWithArgs = "$route/{$category}/{$year}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingOnCategoryScreenForYear(
    modifier: Modifier = Modifier,
    navigateToAddItem: (String) -> Unit,
    navigateBack: () -> Unit,
    navigateToItemDates: (Long) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: SpendingOnCategoryForYearScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val categoryLabel = compactCategoryLabel(uiState.value.sentCategory, uiState.value.category)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                title = stringResource(id = R.string.spending_on_category_screen, categoryLabel),
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.value.isThisMonthCurrent) {
                        navigateToAddItem(uiState.value.sentCategory)
                    } else {
                        Toast.makeText(context, R.string.you_cant_add_item_archived, Toast.LENGTH_SHORT).show()
                    }
                },
                shape = CircleShape,
                modifier = Modifier.padding(BudgetTheme.spacing.lg),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
    ) { paddingValues ->
        BudgetBackdrop(modifier = Modifier.padding(paddingValues)) {
            SpendingOnCategoryYearBody(
                uiState = uiState.value,
                deleteItem = { viewModel.deleteItem(it) },
                navigateToItemDates = navigateToItemDates,
            )
        }
    }
}

@Composable
private fun SpendingOnCategoryYearBody(
    modifier: Modifier = Modifier,
    uiState: SpendingOnCategoryUiState,
    deleteItem: (Long) -> Unit,
    navigateToItemDates: (Long) -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val categoryLabel = compactCategoryLabel(uiState.sentCategory, uiState.category)
    val categoryDisplay = when (uiState.sentCategory) {
        "food" -> SpendingCategoryDisplayObject.items[0]
        "others" -> SpendingCategoryDisplayObject.items[2]
        else -> SpendingCategoryDisplayObject.items[1]
    }
    val accent = when (uiState.sentCategory) {
        "food" -> BudgetTheme.extendedColors.food
        "others" -> BudgetTheme.extendedColors.others
        else -> BudgetTheme.extendedColors.transit
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
            YearCategorySnapshotCard(
                category = categoryLabel,
                yearLabel = uiState.periodLabel,
                totalCategory = uiState.totalCategory,
                totalSpending = uiState.totalSpending,
                spendingRatio = uiState.spendingRatio,
                icon = categoryDisplay.spendingIcon,
                accent = accent,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                YearCategoryStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Entries",
                    value = uiState.transactionCount.toString(),
                    subtitle = "Recorded this year",
                )
                YearCategoryStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Average",
                    value = uiState.averageTransaction,
                    subtitle = "Typical ticket size",
                )
            }
        }
        item {
            YearCategoryStatCard(
                label = "Largest transaction",
                value = uiState.biggestTransaction,
                subtitle = "Highest single spend this year",
            )
        }
        item {
            SectionHeading(
                title = "$categoryLabel feed",
                subtitle = "The recorded entries behind this annual total.",
            )
        }
        if (uiState.itemList.isEmpty()) {
            item {
                YearEmptyCategoryFeedCard(
                    category = categoryLabel,
                    accent = accent,
                )
            }
        } else {
            items(uiState.itemList) { item ->
                ItemCard(
                    title = item.name,
                    totalSpending = item.totalCost,
                    deleteItem = { deleteItem(item.itemId) },
                    date = item.date,
                    imagePath = item.imagePath,
                    navigateToItemDates = { navigateToItemDates(item.itemId) },
                    displayItem = categoryDisplay,
                )
            }
        }
    }
}

private fun compactCategoryLabel(categoryKey: String, fallback: String): String = when (categoryKey) {
    "transportation" -> "Transit"
    else -> fallback
}

@Composable
private fun YearCategorySnapshotCard(
    category: String,
    yearLabel: String,
    totalCategory: String,
    totalSpending: String,
    spendingRatio: Float,
    icon: Int,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(BudgetTheme.spacing.xl)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.lg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
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
                                modifier = Modifier.size(52.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(id = icon),
                                    contentDescription = null,
                                    tint = accent,
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = yearLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(BudgetTheme.radii.pill),
                    ) {
                        Text(
                            text = "${(spendingRatio * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BudgetValueText(
                        text = totalCategory,
                        modifier = Modifier.fillMaxWidth(),
                        tone = BudgetValueTone.Hero,
                        color = MaterialTheme.colorScheme.onSurface,
                        unitLabel = "IQD",
                    )
                    Text(
                        text = "Spent on $category in $yearLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = BudgetTheme.spacing.md, vertical = BudgetTheme.spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Share of yearly spend",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            BudgetValueText(
                                text = totalSpending,
                                tone = BudgetValueTone.Compact,
                                color = MaterialTheme.colorScheme.onSurface,
                                unitLabel = "IQD",
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(accent.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = accent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearCategoryStatCard(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun YearEmptyCategoryFeedCard(
    category: String,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            horizontalAlignment = Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape)
            )
            Text(
                text = "No $category entries yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "When entries land here, this annual feed becomes your view for the full year.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
