package com.example.mybudgetapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.mybudgetapp.ui.viewmodels.ThisYearScreenUiState
import com.example.mybudgetapp.ui.viewmodels.ThisYearScreenViewModel
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.BottomNavigationBar
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.DropDownItem
import com.example.mybudgetapp.ui.widgets.SectionHeading
import com.example.mybudgetapp.ui.widgets.TrendChartCard
import java.time.Year

object ThisYearDestination : NavigationDestination {
    override val route = "ThisYearScreen"
    override val titleRes = R.string.this_month_screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThisYearScreen(
    navigateToSpendingOnCategoryForYear: (String, Int) -> Unit,
    navigateToTotalIncomeForYear: (Int, Boolean) -> Unit,
    navigateToInsights: (Int) -> Unit,
    navigateToCloudBackup: () -> Unit,
    navigateToThisMonthScreen: () -> Unit,
    navigateToThisYearScreen: () -> Unit,
) {
    val viewModel: ThisYearScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    var isQuickAddVisible by rememberSaveable { mutableStateOf(false) }
    var isYearPickerVisible by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.value.isCurrentYear) {
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
                selectedItemIndex = 0,
            )
        },
    ) { innerPadding ->
        BudgetBackdrop(modifier = Modifier.padding(innerPadding)) {
            ThisYearScreenBody(
                uiState = uiState.value,
                onPreviousYear = viewModel::selectPreviousYear,
                onNextYear = viewModel::selectNextYear,
                onOpenYearPicker = { isYearPickerVisible = true },
                navigateToSpendingOnCategoryForYear = navigateToSpendingOnCategoryForYear,
                navigateToTotalIncomeForYear = navigateToTotalIncomeForYear,
                navigateToInsights = navigateToInsights,
            )
        }

        if (isQuickAddVisible) {
            QuickAddBottomSheet(
                viewModelKey = "homeQuickAddYear",
                onDismissRequest = { isQuickAddVisible = false },
            )
        }

        if (isYearPickerVisible) {
            YearPickerBottomSheet(
                years = uiState.value.years,
                onDismissRequest = { isYearPickerVisible = false },
                onSelectYear = {
                    viewModel.selectYear(it.number)
                    isYearPickerVisible = false
                },
                onJumpToCurrent = {
                    viewModel.jumpToCurrentYear()
                    isYearPickerVisible = false
                },
            )
        }
    }
}

@Composable
fun ThisYearScreenBody(
    modifier: Modifier = Modifier,
    uiState: ThisYearScreenUiState,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onOpenYearPicker: () -> Unit,
    navigateToSpendingOnCategoryForYear: (String, Int) -> Unit,
    navigateToTotalIncomeForYear: (Int, Boolean) -> Unit,
    navigateToInsights: (Int) -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val netBalance = formatCurrencyIraqiDinar(uiState.totalIncomeAmountForYear - uiState.totalSpendingAmountForYear)
    val totalSpending = uiState.totalSpendingAmountForYear.takeIf { it > 0 } ?: 1.0
    val topCategory = uiState.insights.getOrNull(0)
    val daysInYear = if (Year.isLeap(uiState.selectedYear.toLong())) 366.0 else 365.0
    val averageDaily = formatCompactCurrencyIraqiDinar(uiState.totalSpendingAmountForYear / daysInYear)

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
                title = "This year",
                selectedPeriodLabel = uiState.currentYear,
                canNavigatePrevious = uiState.canNavigatePrevious,
                canNavigateNext = uiState.canNavigateNext,
                onPrevious = onPreviousYear,
                onNext = onNextYear,
                onOpenPicker = onOpenYearPicker,
            )
        }
        item {
            DashboardBalanceHero(
                periodLabel = uiState.currentYear,
                balanceLabel = "Yearly balance",
                balanceValue = netBalance,
                statusLabel = if (uiState.isCurrentYear) "Live year" else "Archived year",
                incomeValue = uiState.totalIncomeForYear,
                spendingValue = uiState.totalSpendingForYear,
                onOpenIncome = {
                    navigateToTotalIncomeForYear(uiState.selectedYear, true)
                },
                onOpenSpending = {
                    navigateToTotalIncomeForYear(uiState.selectedYear, false)
                },
                onOpenInsights = { navigateToInsights(uiState.selectedYear) },
            )
        }
        item {
            DashboardQuickStatsCard(
                stats = listOf(
                    DashboardQuickStat(
                        label = "Top category",
                        value = topCategory?.value ?: "None",
                        note = topCategory?.subtitle ?: "No category leader yet",
                        highlight = BudgetTheme.extendedColors.food,
                        noteTone = BudgetValueTone.Compact,
                        noteUnitLabel = "IQD",
                    ),
                    DashboardQuickStat(
                        label = "Daily average",
                        value = averageDaily,
                        note = "Across the year",
                        highlight = BudgetTheme.extendedColors.transit,
                        valueTone = BudgetValueTone.Card,
                        valueUnitLabel = "IQD",
                    ),
                ),
            )
        }
        item {
            TrendChartCard(
                title = "Monthly rhythm",
                subtitle = "See drift, spikes, and recovery months before opening the full yearly feed.",
                points = uiState.spendingTrend,
            )
        }
        item {
            DashboardLanesCard(
                title = "Spending categories",
                subtitle = "The three category groups carrying the most weight this year.",
                lanes = listOf(
                    DashboardLaneUi(
                        label = stringResource(id = SpendingCategoryDisplayObject.items[0].title),
                        amount = uiState.totalSpendingOnFoodForYear,
                        progress = (uiState.totalFoodAmountForYear / totalSpending).toFloat().coerceIn(0f, 1f),
                        accent = BudgetTheme.extendedColors.food,
                        icon = SpendingCategoryDisplayObject.items[0].spendingIcon,
                        onClick = { navigateToSpendingOnCategoryForYear("food", uiState.selectedYear) },
                    ),
                    DashboardLaneUi(
                        label = stringResource(id = SpendingCategoryDisplayObject.items[1].title),
                        amount = uiState.totalSpendingOnTransportationForYear,
                        progress = (uiState.totalTransportationAmountForYear / totalSpending).toFloat().coerceIn(0f, 1f),
                        accent = BudgetTheme.extendedColors.transit,
                        icon = SpendingCategoryDisplayObject.items[1].spendingIcon,
                        onClick = { navigateToSpendingOnCategoryForYear("transportation", uiState.selectedYear) },
                    ),
                    DashboardLaneUi(
                        label = stringResource(id = SpendingCategoryDisplayObject.items[2].title),
                        amount = uiState.totalSpendingOnOthersForYear,
                        progress = (uiState.totalOthersAmountForYear / totalSpending).toFloat().coerceIn(0f, 1f),
                        accent = BudgetTheme.extendedColors.others,
                        icon = SpendingCategoryDisplayObject.items[2].spendingIcon,
                        onClick = { navigateToSpendingOnCategoryForYear("others", uiState.selectedYear) },
                    ),
                ),
            )
        }
        item {
            DashboardActivityCard(
                title = "Latest entries",
                subtitle = "A quick year-level preview of the most recent activity.",
                items = uiState.recentTransactions,
                onViewAll = { navigateToTotalIncomeForYear(uiState.selectedYear, false) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearPickerBottomSheet(
    years: List<DropDownItem>,
    onDismissRequest: () -> Unit,
    onSelectYear: (DropDownItem) -> Unit,
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
                    title = "Jump to year",
                    subtitle = "Move across annual views without stepping through each year one by one.",
                )
            }
            item {
                YearSheetRow(
                    label = "This year",
                    onClick = onJumpToCurrent,
                )
            }
            items(years.size) { index ->
                YearSheetRow(
                    label = years[index].title,
                    onClick = { onSelectYear(years[index]) },
                )
            }
        }
    }
}

@Composable
private fun YearSheetRow(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(BudgetTheme.radii.md),
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = BudgetTheme.spacing.lg, vertical = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
