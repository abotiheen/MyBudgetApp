package com.example.mybudgetapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.MonthPeriodOption
import com.example.mybudgetapp.ui.viewmodels.ThisMonthScreenUiState
import com.example.mybudgetapp.ui.viewmodels.ThisMonthScreenViewModel
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.SectionHeading
import com.example.mybudgetapp.ui.widgets.TrendChartCard
import com.example.mybudgetapp.ui.widgets.categoryAccentColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ThisMonthDestination : NavigationDestination {
    override val route = "ThisMonthScreen"
    override val titleRes = R.string.this_month_screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThisMonthScreen(
    navigateToSpendingOnCategory: (String, Int, Int) -> Unit,
    navigateToCategoryBreakdown: (Int, Int) -> Unit,
    navigateToTotalIncome: (Int, Int, Boolean) -> Unit,
    navigateToInsights: (Int, Int) -> Unit,
    navigateToCategories: () -> Unit,
) {
    val viewModel: ThisMonthScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    var isQuickAddVisible by rememberSaveable { mutableStateOf(false) }
    var isPeriodPickerVisible by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.value.isCurrentPeriod) {
                        isQuickAddVisible = true
                    } else {
                        Toast.makeText(context, R.string.you_cant_add_item_archived, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(BudgetTheme.spacing.lg),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
    ) { innerPadding ->
        BudgetBackdrop(modifier = Modifier.padding(innerPadding)) {
            ThisMonthScreenBody(
                uiState = uiState.value,
                onPreviousPeriod = viewModel::selectPreviousPeriod,
                onNextPeriod = viewModel::selectNextPeriod,
                onOpenPeriodPicker = { isPeriodPickerVisible = true },
                navigateToSpendingOnCategory = navigateToSpendingOnCategory,
                navigateToCategoryBreakdown = navigateToCategoryBreakdown,
                navigateToTotalIncome = navigateToTotalIncome,
                navigateToInsights = navigateToInsights,
                navigateToCategories = navigateToCategories,
            )
        }

        if (isQuickAddVisible) {
            QuickAddBottomSheet(
                viewModelKey = "homeQuickAddMonth",
                onDismissRequest = { isQuickAddVisible = false },
            )
        }

        if (isPeriodPickerVisible) {
            MonthPeriodPickerBottomSheet(
                periods = uiState.value.availablePeriods,
                onDismissRequest = { isPeriodPickerVisible = false },
                onSelectPeriod = {
                    viewModel.selectPeriod(it)
                    isPeriodPickerVisible = false
                },
                onJumpToCurrent = {
                    viewModel.jumpToCurrentPeriod()
                    isPeriodPickerVisible = false
                },
            )
        }
    }
}

@Composable
fun ThisMonthScreenBody(
    modifier: Modifier = Modifier,
    uiState: ThisMonthScreenUiState,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onOpenPeriodPicker: () -> Unit,
    navigateToSpendingOnCategory: (String, Int, Int) -> Unit,
    navigateToCategoryBreakdown: (Int, Int) -> Unit,
    navigateToTotalIncome: (Int, Int, Boolean) -> Unit,
    navigateToInsights: (Int, Int) -> Unit,
    navigateToCategories: () -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val netBalance = formatCurrencyIraqiDinar(uiState.totalIncomeAmount - uiState.totalSpendingAmount)
    val topItem = uiState.insights.getOrNull(1)
    val averageDaily = uiState.insights.getOrNull(3)
    val topCategoryLanes = uiState.categoryTotals
        .filter { it.total > 0 }
        .take(3)
        .map { category ->
            DashboardLaneUi(
                label = category.categoryLabel,
                amount = formatCompactCurrencyIraqiDinar(category.total),
                progress = if (uiState.totalSpendingAmount > 0) {
                    (category.total / uiState.totalSpendingAmount).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                },
                accent = categoryAccentColor(category.colorHex, category.categoryKey),
                iconKey = category.iconKey,
                categoryKey = category.categoryKey,
                onClick = {
                    navigateToSpendingOnCategory(category.categoryKey, uiState.selectedMonth, uiState.selectedYear)
                },
            )
        }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = spacing.lg,
            bottom = 136.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            DashboardCommandDeck(
                title = "This month",
                selectedPeriodLabel = uiState.periodLabel,
                canNavigatePrevious = uiState.canNavigatePrevious,
                canNavigateNext = uiState.canNavigateNext,
                onPrevious = onPreviousPeriod,
                onNext = onNextPeriod,
                onOpenPicker = onOpenPeriodPicker,
            )
        }
        item {
            DashboardBalanceHero(
                periodLabel = uiState.periodLabel,
                balanceLabel = "Working balance",
                balanceValue = netBalance,
                statusLabel = if (uiState.isCurrentPeriod) "Live month" else "Archived month",
                incomeValue = uiState.totalIncome,
                spendingValue = uiState.totalSpending,
                onOpenIncome = {
                    navigateToTotalIncome(uiState.selectedMonth, uiState.selectedYear, true)
                },
                onOpenSpending = {
                    navigateToTotalIncome(uiState.selectedMonth, uiState.selectedYear, false)
                },
                onOpenInsights = {
                    navigateToInsights(uiState.selectedMonth, uiState.selectedYear)
                },
            )
        }
        item {
            DashboardQuickStatsCard(
                stats = listOf(
                    DashboardQuickStat(
                        label = "Top item",
                        value = topItem?.subtitle ?: "No item yet",
                        note = topItem?.value ?: formatCompactCurrencyIraqiDinar(0.0),
                        highlight = BudgetTheme.extendedColors.food,
                        noteTone = BudgetValueTone.Compact,
                        noteUnitLabel = "IQD",
                    ),
                    DashboardQuickStat(
                        label = "Daily average",
                        value = averageDaily?.value ?: formatCompactCurrencyIraqiDinar(0.0),
                        note = averageDaily?.subtitle ?: "Across the month",
                        highlight = BudgetTheme.extendedColors.transit,
                        valueTone = BudgetValueTone.Card,
                        valueUnitLabel = "IQD",
                    ),
                ),
            )
        }
        item {
            DashboardActivityCard(
                title = "Recent activity",
                subtitle = "The latest entries for this month, without leaving the overview.",
                items = uiState.recentTransactions,
                onViewAll = {
                    navigateToTotalIncome(uiState.selectedMonth, uiState.selectedYear, false)
                },
            )
        }
        item {
            DashboardLanesCard(
                title = "Spending categories",
                subtitle = if (topCategoryLanes.isEmpty()) {
                    "No category totals yet. Add a few entries to build this section."
                } else {
                    "Open the strongest categories for this period or manage the full category library."
                },
                lanes = topCategoryLanes,
                actions = listOf(
                    DashboardCardAction(
                        label = "View all",
                        onClick = { navigateToCategoryBreakdown(uiState.selectedYear, uiState.selectedMonth) },
                    ),
                    DashboardCardAction(
                        label = "Manage",
                        onClick = navigateToCategories,
                    ),
                ),
            )
        }
        item {
            TrendChartCard(
                title = "Daily rhythm",
                subtitle = "See the heavy days first, then decide whether the feed needs a deeper look.",
                points = uiState.spendingTrend,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthPeriodPickerBottomSheet(
    periods: List<MonthPeriodOption>,
    onDismissRequest: () -> Unit,
    onSelectPeriod: (MonthPeriodOption) -> Unit,
    onJumpToCurrent: () -> Unit,
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var sheetContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sheetContentVisible = true
        sheetState.show()
    }

    fun dismissSheet(action: () -> Unit = {}) {
        coroutineScope.launch {
            sheetContentVisible = false
            delay(120)
            sheetState.hide()
            action()
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { dismissSheet() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        AnimatedVisibility(
            visible = sheetContentVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 260),
                    initialOffsetY = { it / 8 },
                ),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                slideOutVertically(
                    animationSpec = tween(durationMillis = 180),
                    targetOffsetY = { it / 8 },
                ),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = BudgetTheme.spacing.xl,
                    end = BudgetTheme.spacing.xl,
                    top = BudgetTheme.spacing.sm,
                    bottom = BudgetTheme.spacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                item {
                    SectionHeading(
                        title = "Jump to month",
                        subtitle = "Move across your timeline without stepping through each month one by one.",
                    )
                }
                item {
                    PeriodSheetRow(
                        label = "This month",
                        onClick = { dismissSheet(onJumpToCurrent) },
                    )
                }
                items(periods.size) { index ->
                    PeriodSheetRow(
                        label = periods[index].label,
                        onClick = { dismissSheet { onSelectPeriod(periods[index]) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodSheetRow(
    label: String,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(BudgetTheme.radii.md),
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        androidx.compose.material3.Text(
            text = label,
            modifier = Modifier.padding(horizontal = BudgetTheme.spacing.lg, vertical = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
