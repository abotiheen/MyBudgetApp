package com.example.mybudgetapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.SpendingCategoryDisplayObject
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.SpendingItem
import com.example.mybudgetapp.ui.viewmodels.TotalSpendingScreenViewModel
import com.example.mybudgetapp.ui.viewmodels.TotalSpendingUiState
import com.example.mybudgetapp.ui.widgets.AnimatedSegmentedControl
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.SegmentedTextLabel

object TotalIncomeDestination : NavigationDestination {
    override val route = "TotalIncome"
    override val titleRes = R.string.total_income_screen
    const val month = "month"
    const val year = "year"
    const val isIncome = "isIncome"
    val routeWithArgs = "$route/{$month}/{$year}/{$isIncome}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotalIncomeScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    navigateToTransactionType: (Boolean) -> Unit,
    navigateToAddItem: (String) -> Unit,
    navigateToItemDates: (Long) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: TotalSpendingScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
            TotalIncomeBody(
                uiState = uiState.value,
                onToggleType = navigateToTransactionType,
                deleteItem = { viewModel.deleteItem(it) },
                navigateToItemDates = navigateToItemDates,
            )
        }
    }
}

@Composable
fun TotalIncomeBody(
    modifier: Modifier = Modifier,
    uiState: TotalSpendingUiState,
    onToggleType: (Boolean) -> Unit,
    deleteItem: (Long) -> Unit,
    navigateToItemDates: (Long) -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val items = if (uiState.isIncome) uiState.incomeItemList else uiState.spendingItemList
    val accent = if (uiState.isIncome) BudgetTheme.extendedColors.income else BudgetTheme.extendedColors.danger
    val totalValue = if (uiState.isIncome) uiState.totalIncome else uiState.totalSpending
    val largestTransaction = items.maxByOrNull { it.amountValue }?.totalCost ?: totalValue
    val heroIcon = if (uiState.isIncome) R.drawable.baseline_attach_money_24 else R.drawable.baseline_money_off_24

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = spacing.lg,
            bottom = 40.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item {
            TransactionTypeSwitch(
                isIncome = uiState.isIncome,
                onToggleType = onToggleType,
            )
        }
        item {
            DetailCollectionHero(
                title = if (uiState.isIncome) "Income flow" else "Expense flow",
                periodLabel = uiState.month,
                totalValue = totalValue,
                subtitle = if (uiState.isIncome) {
                    "Every income entry for this month in one place."
                } else {
                    "Every expense entry for this month in one place."
                },
                badgeLabel = if (uiState.isThisMonthCurrent) "Live month" else "Archived",
                accent = accent,
                iconRes = heroIcon,
                chips = listOf(
                    DetailHeroChipUi("Entries", items.size.toString()),
                    DetailHeroChipUi("Largest", largestTransaction, isMonetary = true),
                ),
            )
        }
        item {
            DetailListHeader(
                title = if (uiState.isIncome) "Entries" else "Transactions",
                subtitle = if (items.isEmpty()) {
                    "Nothing recorded for ${uiState.month} yet."
                } else {
                    "${items.size} items, sorted for quick scanning."
                },
            )
        }
        if (items.isEmpty()) {
            item {
                DetailEmptyStateCard(
                    title = if (uiState.isIncome) "No income entries yet" else "No expense entries yet",
                    message = if (uiState.isIncome) {
                        "Income records will show up here as soon as you add them."
                    } else {
                        "Expense records will show up here as soon as you add them."
                    },
                    accent = accent,
                    iconRes = heroIcon,
                )
            }
        } else {
            items(items) { item ->
                TransactionEntryRow(
                    item = item,
                    isIncome = uiState.isIncome,
                    accent = accent,
                    onDelete = { deleteItem(item.itemId) },
                    onOpen = { navigateToItemDates(item.itemId) },
                )
            }
        }
    }
}

@Composable
private fun TransactionTypeSwitch(
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
private fun TransactionEntryRow(
    item: SpendingItem,
    isIncome: Boolean,
    accent: Color,
    onDelete: () -> Unit,
    onOpen: () -> Unit,
) {
    val categoryDisplay = when (item.category) {
        "food" -> SpendingCategoryDisplayObject.items[0]
        "others" -> SpendingCategoryDisplayObject.items[2]
        "transportation" -> SpendingCategoryDisplayObject.items[1]
        else -> SpendingCategoryDisplayObject.items[3]
    }

    DetailEntryRow(
        title = item.name,
        amount = item.totalCost,
        meta = if (isIncome) {
            "Income • ${item.date}"
        } else {
            "${item.category.replaceFirstChar { it.uppercase() }} • ${item.date}"
        },
        imagePath = item.imagePath,
        iconRes = categoryDisplay.spendingIcon,
        accent = accent,
        onOpen = onOpen,
        onDelete = onDelete,
    )
}
