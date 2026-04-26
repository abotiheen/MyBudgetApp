package com.example.mybudgetapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.SpendingOnCategoryScreenViewModel
import com.example.mybudgetapp.ui.viewmodels.SpendingOnCategoryUiState
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.categoryAccentColor

object SpendingOnCategoryDestination : NavigationDestination {
    override val route = "SpendingOnCategory"
    override val titleRes = R.string.spending_on_category_screen
    const val category = "category"
    const val month = "month"
    const val year = "year"
    val routeWithArgs = "$route/{$category}/{$month}/{$year}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingOnCategoryScreen(
    modifier: Modifier = Modifier,
    navigateToAddItem: (String) -> Unit,
    navigateBack: () -> Unit,
    navigateToItemDates: (String, String, String, Int, Int) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: SpendingOnCategoryScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                title = uiState.value.category,
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
            SpendingOnCategoryBody(
                uiState = uiState.value,
                deleteItem = { viewModel.deleteItem(it) },
                navigateToItemDates = navigateToItemDates,
            )
        }
    }
}

@Composable
fun SpendingOnCategoryBody(
    modifier: Modifier = Modifier,
    uiState: SpendingOnCategoryUiState,
    deleteItem: (Long) -> Unit,
    navigateToItemDates: (String, String, String, Int, Int) -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val categoryLabel = uiState.category
    val accent = categoryAccentColor(uiState.categoryColorHex, uiState.sentCategory)

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
            DetailCollectionHero(
                title = categoryLabel,
                periodLabel = uiState.periodLabel,
                totalValue = uiState.totalCategory,
                subtitle = "Spending concentrated in ${categoryLabel.lowercase()} for this month.",
                badgeLabel = "${(uiState.spendingRatio * 100).toInt()}% share",
                accent = accent,
                iconKey = uiState.categoryIconKey,
                fallbackCategoryKey = uiState.sentCategory,
                chips = listOf(
                    DetailHeroChipUi("Entries", uiState.transactionCount.toString()),
                    DetailHeroChipUi("Average", uiState.averageTransaction, isMonetary = true),
                    DetailHeroChipUi("Largest", uiState.biggestTransaction, isMonetary = true),
                ),
            )
        }
        item {
            DetailListHeader(
                title = "$categoryLabel entries",
                subtitle = if (uiState.itemList.isEmpty()) {
                    "Nothing recorded here yet."
                } else {
                    "${uiState.totalSpending} overall this month. Tap any row to inspect it."
                },
            )
        }
        if (uiState.itemList.isEmpty()) {
            item {
                DetailEmptyStateCard(
                    title = "No ${categoryLabel.lowercase()} entries yet",
                    message = "When entries land here, this view becomes much easier to review.",
                    accent = accent,
                    iconKey = uiState.categoryIconKey,
                    fallbackCategoryKey = uiState.sentCategory,
                )
            }
        } else {
            items(uiState.itemList) { item ->
                DetailEntryRow(
                    title = item.name,
                    amount = item.totalCost,
                    meta = "$categoryLabel • ${item.date}",
                    imagePath = item.imagePath,
                    iconKey = uiState.categoryIconKey,
                    fallbackCategoryKey = uiState.sentCategory,
                    accent = accent,
                    onOpen = {
                        navigateToItemDates(
                            item.name,
                            item.category,
                            item.type,
                            item.year,
                            item.month,
                        )
                    },
                    onDelete = { deleteItem(item.itemId) },
                )
            }
        }
    }
}
