package com.example.mybudgetapp.ui.screens

import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.SpendingCategoryDisplayObject
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.MonthPeriodOption
import com.example.mybudgetapp.ui.viewmodels.ThisMonthScreenUiState
import com.example.mybudgetapp.ui.viewmodels.ThisMonthScreenViewModel
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.BottomNavigationBar
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.SectionHeading
import com.example.mybudgetapp.ui.widgets.TrendChartCard

object ThisMonthDestination : NavigationDestination {
    override val route = "ThisMonthScreen"
    override val titleRes = R.string.this_month_screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThisMonthScreen(
    navigateToSpendingOnCategory: (String, Int, Int) -> Unit,
    navigateToTotalIncome: (Int, Int, Boolean) -> Unit,
    navigateToInsights: (Int, Int) -> Unit,
    navigateToCloudBackup: () -> Unit,
    navigateToThisMonthScreen: () -> Unit,
    navigateToThisYearScreen: () -> Unit,
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
        bottomBar = {
            BottomNavigationBar(
                navigateToThisMonthScreen = navigateToThisMonthScreen,
                navigateToThisYearScreen = navigateToThisYearScreen,
                navigateToCloudBackupScreen = navigateToCloudBackup,
                selectedItemIndex = 1,
            )
        },
    ) { innerPadding ->
        BudgetBackdrop(modifier = Modifier.padding(innerPadding)) {
            ThisMonthScreenBody(
                uiState = uiState.value,
                onPreviousPeriod = viewModel::selectPreviousPeriod,
                onNextPeriod = viewModel::selectNextPeriod,
                onOpenPeriodPicker = { isPeriodPickerVisible = true },
                navigateToSpendingOnCategory = navigateToSpendingOnCategory,
                navigateToTotalIncome = navigateToTotalIncome,
                navigateToInsights = navigateToInsights,
                navigateToThisYearScreen = navigateToThisYearScreen,
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
    navigateToTotalIncome: (Int, Int, Boolean) -> Unit,
    navigateToInsights: (Int, Int) -> Unit,
    navigateToThisYearScreen: () -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val netBalance = formatCurrencyIraqiDinar(uiState.totalIncomeAmount - uiState.totalSpendingAmount)
    val totalSpending = uiState.totalSpendingAmount.takeIf { it > 0 } ?: 1.0
    val topItem = uiState.insights.getOrNull(1)
    val averageDaily = uiState.insights.getOrNull(3)

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
                subtitle = "A lighter overview focused on balance, latest activity, and the three spending categories that matter.",
                currentViewLabel = "Month",
                alternateViewLabel = "Year",
                onOpenAlternateView = navigateToThisYearScreen,
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
                subtitle = "Open a category to inspect every entry behind the total.",
                lanes = listOf(
                    DashboardLaneUi(
                        label = stringResource(id = SpendingCategoryDisplayObject.items[0].title),
                        amount = uiState.totalSpendingOnFood,
                        progress = (uiState.totalFoodAmount / totalSpending).toFloat().coerceIn(0f, 1f),
                        accent = BudgetTheme.extendedColors.food,
                        icon = SpendingCategoryDisplayObject.items[0].spendingIcon,
                        onClick = {
                            navigateToSpendingOnCategory("food", uiState.selectedMonth, uiState.selectedYear)
                        },
                    ),
                    DashboardLaneUi(
                        label = stringResource(id = SpendingCategoryDisplayObject.items[1].title),
                        amount = uiState.totalSpendingOnTransportation,
                        progress = (uiState.totalTransportationAmount / totalSpending).toFloat().coerceIn(0f, 1f),
                        accent = BudgetTheme.extendedColors.transit,
                        icon = SpendingCategoryDisplayObject.items[1].spendingIcon,
                        onClick = {
                            navigateToSpendingOnCategory("transportation", uiState.selectedMonth, uiState.selectedYear)
                        },
                    ),
                    DashboardLaneUi(
                        label = stringResource(id = SpendingCategoryDisplayObject.items[2].title),
                        amount = uiState.totalSpendingOnOthers,
                        progress = (uiState.totalOthersAmount / totalSpending).toFloat().coerceIn(0f, 1f),
                        accent = BudgetTheme.extendedColors.others,
                        icon = SpendingCategoryDisplayObject.items[2].spendingIcon,
                        onClick = {
                            navigateToSpendingOnCategory("others", uiState.selectedMonth, uiState.selectedYear)
                        },
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
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.background,
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
                    onClick = onJumpToCurrent,
                )
            }
            items(periods.size) { index ->
                PeriodSheetRow(
                    label = periods[index].label,
                    onClick = { onSelectPeriod(periods[index]) },
                )
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
