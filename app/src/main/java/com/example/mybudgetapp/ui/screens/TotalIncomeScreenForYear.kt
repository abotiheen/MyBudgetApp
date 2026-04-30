package com.example.mybudgetapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.TotalSpendingScreenForYearViewModel
import com.example.mybudgetapp.ui.viewmodels.TotalSpendingUiState
import com.example.mybudgetapp.ui.widgets.AnimatedSegmentedControl
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.BudgetValueText
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.ItemCard
import com.example.mybudgetapp.ui.widgets.SegmentedTextLabel
import com.example.mybudgetapp.ui.widgets.SectionHeading
import com.example.mybudgetapp.ui.widgets.categoryAccentColor

object TotalIncomeDestinationForYear : NavigationDestination {
    override val route = "TotalIncomeForYear"
    override val titleRes = R.string.total_income_screen
    const val year = 0
    const val isIncome: Boolean = true
    val routeWithArgs = "$route/{$year}/{$isIncome}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotalIncomeScreenForYear(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    navigateToAddItem: (String) -> Unit,
    navigateToItemDates: (String, String, String, Int, Int) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: TotalSpendingScreenForYearViewModel =
        viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                title = if (uiState.value.isIncome) "Income" else "Expenses",
                navigateBack = navigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.value.isThisMonthCurrent) {
                        navigateToAddItem(if (uiState.value.isIncome) "income" else "all")
                    } else {
                        Toast.makeText(
                            context,
                            R.string.you_cant_add_item_archived,
                            Toast.LENGTH_SHORT
                        ).show()
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
            TotalIncomeYearBody(
                uiState = uiState.value,
                onToggleType = viewModel::selectTransactionType,
                deleteItem = { viewModel.deleteItem(it) },
                navigateToItemDates = navigateToItemDates,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TotalIncomeYearBody(
    modifier: Modifier = Modifier,
    uiState: TotalSpendingUiState,
    onToggleType: (Boolean) -> Unit,
    deleteItem: (Long) -> Unit,
    navigateToItemDates: (String, String, String, Int, Int) -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val items = if (uiState.isIncome) uiState.incomeItemList else uiState.spendingItemList
    val groups = if (uiState.isIncome) uiState.incomeGroups else uiState.spendingGroups
    val accent = if (uiState.isIncome) {
        BudgetTheme.extendedColors.income
    } else {
        BudgetTheme.extendedColors.danger
    }
    val totalValue = if (uiState.isIncome) uiState.totalIncome else uiState.totalSpending
    val largestTransaction = items.maxByOrNull { it.amountValue }?.totalCost ?: totalValue

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            top = spacing.lg,
            bottom = 40.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            Box(modifier = Modifier.padding(horizontal = spacing.lg)) {
                YearTransactionTypeSwitch(
                    isIncome = uiState.isIncome,
                    onToggleType = onToggleType,
                )
            }
        }
        item {
            Box(modifier = Modifier.padding(horizontal = spacing.lg)) {
                YearTransactionSummaryCard(
                    isIncome = uiState.isIncome,
                    yearLabel = uiState.month,
                    totalValue = totalValue,
                    itemCount = items.size,
                    isCurrentPeriod = uiState.isThisMonthCurrent,
                    accent = accent,
                )
            }
        }
        item {
            Box(modifier = Modifier.padding(horizontal = spacing.lg)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    YearTransactionStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Entries",
                        value = items.size.toString(),
                        subtitle = if (uiState.isIncome) "Income records" else "Expense records",
                    )
                    YearTransactionStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Largest",
                        value = largestTransaction,
                        subtitle = if (uiState.isIncome) "Biggest income" else "Biggest expense",
                    )
                }
            }
        }
        item {
            Box(modifier = Modifier.padding(horizontal = spacing.lg)) {
                SectionHeading(
                    title = if (uiState.isIncome) "Income feed" else "Expense feed",
                    subtitle = "The transactions that shaped ${uiState.month}.",
                )
            }
        }
        if (items.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = spacing.lg)) {
                    YearEmptyTransactionCard(
                        isIncome = uiState.isIncome,
                        accent = accent,
                    )
                }
            }
        } else {
            groups.forEach { group ->
                stickyHeader(key = "detail-group-${group.key}") {
                    DetailGroupSummaryCard(
                        title = group.label,
                        displayTotal = group.displayTotal,
                        totalLabel = group.totalLabel,
                        accent = accent,
                    )
                }
                items(
                    items = group.items,
                    key = { item -> "${group.key}-${item.itemId}" },
                ) { item ->
                    val itemAccent = if (uiState.isIncome) {
                        BudgetTheme.extendedColors.income
                    } else {
                        categoryAccentColor(item.categoryColorHex, item.category)
                    }
                    Box(modifier = Modifier.padding(horizontal = spacing.lg)) {
                        ItemCard(
                            title = item.name,
                            totalSpending = item.totalCost,
                            deleteItem = { deleteItem(item.itemId) },
                            date = item.date,
                            imagePath = item.imagePath,
                            accentColor = itemAccent,
                            navigateToItemDates = {
                                navigateToItemDates(
                                    item.name,
                                    item.category,
                                    item.type,
                                    item.year,
                                    item.month,
                                )
                            },
                            iconKey = item.categoryIconKey,
                            fallbackCategoryKey = item.category,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearTransactionTypeSwitch(
    isIncome: Boolean,
    onToggleType: (Boolean) -> Unit,
) {
    AnimatedSegmentedControl(
        selectedIndex = if (isIncome) 1 else 0,
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
            text = if (index == 0) "Expenses" else "Income",
            selected = selected,
        )
    }
}

@Composable
private fun YearTransactionSummaryCard(
    isIncome: Boolean,
    yearLabel: String,
    totalValue: String,
    itemCount: Int,
    isCurrentPeriod: Boolean,
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
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = yearLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = if (isIncome) "Income center" else "Expense center",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(BudgetTheme.radii.pill),
                    ) {
                        Text(
                            text = if (isCurrentPeriod) "Live year" else "Archived year",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                        )
                    }
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
                        text = if (isIncome) {
                            "Total income recorded in this year"
                        } else {
                            "Total expenses recorded in this year"
                        },
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
                            .padding(
                                horizontal = BudgetTheme.spacing.md,
                                vertical = BudgetTheme.spacing.md
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Feed size",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "$itemCount entries",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(accent.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isIncome) {
                                        R.drawable.baseline_attach_money_24
                                    } else {
                                        R.drawable.baseline_money_off_24
                                    }
                                ),
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
private fun YearTransactionStatCard(
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
private fun YearEmptyTransactionCard(
    isIncome: Boolean,
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
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape)
            )
            Text(
                text = if (isIncome) "No income entries yet" else "No expense entries yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (isIncome) {
                    "Income records will show up here as soon as you add them for this year."
                } else {
                    "Expense records will show up here as soon as you add them for this year."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
