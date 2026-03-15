package com.example.mybudgetapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.SpendingCategoryDisplayObject
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.dmSans
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.MonthPeriodOption
import com.example.mybudgetapp.ui.viewmodels.ThisMonthScreenUiState
import com.example.mybudgetapp.ui.viewmodels.ThisMonthScreenViewModel
import com.example.mybudgetapp.ui.widgets.BottomNavigationBar
import com.example.mybudgetapp.ui.widgets.CategoryCard
import com.example.mybudgetapp.ui.widgets.TotalIncomeSpending

object ThisMonthDestination : NavigationDestination {
    override val route = "ThisMonthScreen"
    override val titleRes = R.string.this_month_screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThisMonthScreen(
    navigateToSpendingOnCategory: (String, Int, Int) -> Unit,
    navigateToTotalIncome: (Int, Int, Boolean) -> Unit,
    navigateToCloudBackup: () -> Unit,
    navigateToThisMonthScreen: () -> Unit,
    navigateToThisYearScreen: () -> Unit
) {
    val viewModel: ThisMonthScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    var isQuickAddVisible by rememberSaveable { mutableStateOf(false) }
    var isPeriodPickerVisible by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.value.isCurrentPeriod) {
                        isQuickAddVisible = true
                    } else {
                        Toast.makeText(context, R.string.you_cant_add_item_archived, Toast.LENGTH_SHORT).show()
                    }
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(20.dp),
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(
                navigateToThisMonthScreen = navigateToThisMonthScreen,
                navigateToThisYearScreen = navigateToThisYearScreen,
                navigateToCloudBackupScreen = navigateToCloudBackup,
                selectedItemIndex = 1
            )
        }
    ) { innerPadding ->
        ThisMonthScreenBody(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState.value,
            onPreviousPeriod = viewModel::selectPreviousPeriod,
            onNextPeriod = viewModel::selectNextPeriod,
            onOpenPeriodPicker = { isPeriodPickerVisible = true },
            navigateToSpendingOnCategory = navigateToSpendingOnCategory,
            navigateToTotalIncome = navigateToTotalIncome,
        )

        if (isQuickAddVisible) {
            QuickAddBottomSheet(
                viewModelKey = "homeQuickAddMonth",
                onDismissRequest = { isQuickAddVisible = false }
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
                }
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
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        MonthPeriodHeader(
            label = uiState.periodLabel,
            canNavigatePrevious = uiState.canNavigatePrevious,
            canNavigateNext = uiState.canNavigateNext,
            onPrevious = onPreviousPeriod,
            onNext = onNextPeriod,
            onOpenPicker = onOpenPeriodPicker,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            TotalIncomeSpending(
                icon = R.drawable.baseline_attach_money_24,
                incomeOrSpending = R.string.income,
                total = uiState.totalIncome,
                modifier = Modifier.weight(1f),
                navigateToTotalIncome = {
                    navigateToTotalIncome(uiState.selectedMonth, uiState.selectedYear, true)
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            TotalIncomeSpending(
                icon = R.drawable.baseline_money_off_24,
                incomeOrSpending = R.string.spending,
                total = uiState.totalSpending,
                modifier = Modifier.weight(1f),
                navigateToTotalIncome = {
                    navigateToTotalIncome(uiState.selectedMonth, uiState.selectedYear, false)
                }
            )
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .padding(vertical = 2.dp, horizontal = 12.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Your spending",
                    fontFamily = dmSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            CategoryCard(
                item = SpendingCategoryDisplayObject.items[0],
                totalSpending = uiState.totalSpendingOnFood,
                modifier = Modifier.padding(bottom = 16.dp),
                navigateToSpendingOnCategory = {
                    navigateToSpendingOnCategory("food", uiState.selectedMonth, uiState.selectedYear)
                }
            )
            CategoryCard(
                item = SpendingCategoryDisplayObject.items[1],
                totalSpending = uiState.totalSpendingOnTransportation,
                modifier = Modifier.padding(bottom = 16.dp),
                navigateToSpendingOnCategory = {
                    navigateToSpendingOnCategory("transportation", uiState.selectedMonth, uiState.selectedYear)
                }
            )
            CategoryCard(
                item = SpendingCategoryDisplayObject.items[2],
                totalSpending = uiState.totalSpendingOnOthers,
                modifier = Modifier.padding(bottom = 16.dp),
                navigateToSpendingOnCategory = {
                    navigateToSpendingOnCategory("others", uiState.selectedMonth, uiState.selectedYear)
                }
            )
        }
    }
}

@Composable
private fun MonthPeriodHeader(
    label: String,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenPicker: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 24.dp)
            .fillMaxWidth()
    ) {
        IconButton(onClick = onPrevious, enabled = canNavigatePrevious) {
            Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = null)
        }
        Text(
            text = label,
            fontFamily = dmSans,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            modifier = Modifier.clickable(onClick = onOpenPicker)
        )
        IconButton(onClick = onNext, enabled = canNavigateNext) {
            Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = null)
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
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Jump to month",
                    fontFamily = dmSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
            item {
                Text(
                    text = "This month",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onJumpToCurrent)
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                )
            }
            items(periods) { period ->
                Text(
                    text = period.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPeriod(period) }
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                )
            }
        }
    }
}
