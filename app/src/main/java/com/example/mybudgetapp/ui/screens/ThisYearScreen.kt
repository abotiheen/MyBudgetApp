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
import com.example.mybudgetapp.ui.viewmodels.ThisYearScreenUiState
import com.example.mybudgetapp.ui.viewmodels.ThisYearScreenViewModel
import com.example.mybudgetapp.ui.widgets.BottomNavigationBar
import com.example.mybudgetapp.ui.widgets.CategoryCard
import com.example.mybudgetapp.ui.widgets.DropDownItem
import com.example.mybudgetapp.ui.widgets.TotalIncomeSpending

object ThisYearDestination : NavigationDestination {
    override val route = "ThisYearScreen"
    override val titleRes = R.string.this_month_screen
}

@Composable
fun ThisYearScreen(
    navigateToSpendingOnCategoryForYear: (String, Int) -> Unit,
    navigateToTotalIncomeForYear: (Int, Boolean) -> Unit,
    navigateToCloudBackup: () -> Unit,
    navigateToThisMonthScreen: () -> Unit,
    navigateToThisYearScreen: () -> Unit
) {
    val viewModel: ThisYearScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    var isQuickAddVisible by rememberSaveable { mutableStateOf(false) }
    var isYearPickerVisible by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.value.isCurrentYear) {
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
                selectedItemIndex = 0
            )
        }
    ) { innerPadding ->
        ThisYearScreenBody(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState.value,
            onPreviousYear = viewModel::selectPreviousYear,
            onNextYear = viewModel::selectNextYear,
            onOpenYearPicker = { isYearPickerVisible = true },
            navigateToSpendingOnCategoryForYear = navigateToSpendingOnCategoryForYear,
            navigateToTotalIncomeForYear = navigateToTotalIncomeForYear
        )

        if (isQuickAddVisible) {
            QuickAddBottomSheet(
                viewModelKey = "homeQuickAddYear",
                onDismissRequest = { isQuickAddVisible = false }
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
                }
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
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        YearHeader(
            label = uiState.currentYear,
            canNavigatePrevious = uiState.canNavigatePrevious,
            canNavigateNext = uiState.canNavigateNext,
            onPrevious = onPreviousYear,
            onNext = onNextYear,
            onOpenPicker = onOpenYearPicker,
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
                total = uiState.totalIncomeForYear,
                modifier = Modifier.weight(1f),
                navigateToTotalIncome = {
                    navigateToTotalIncomeForYear(uiState.selectedYear, true)
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            TotalIncomeSpending(
                icon = R.drawable.baseline_money_off_24,
                incomeOrSpending = R.string.spending,
                total = uiState.totalSpendingForYear,
                modifier = Modifier.weight(1f),
                navigateToTotalIncome = {
                    navigateToTotalIncomeForYear(uiState.selectedYear, false)
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
                totalSpending = uiState.totalSpendingOnFoodForYear,
                modifier = Modifier.padding(bottom = 16.dp),
                navigateToSpendingOnCategory = { navigateToSpendingOnCategoryForYear("food", uiState.selectedYear) }
            )
            CategoryCard(
                item = SpendingCategoryDisplayObject.items[1],
                totalSpending = uiState.totalSpendingOnTransportationForYear,
                modifier = Modifier.padding(bottom = 16.dp),
                navigateToSpendingOnCategory = { navigateToSpendingOnCategoryForYear("transportation", uiState.selectedYear) }
            )
            CategoryCard(
                item = SpendingCategoryDisplayObject.items[2],
                totalSpending = uiState.totalSpendingOnOthersForYear,
                modifier = Modifier.padding(bottom = 16.dp),
                navigateToSpendingOnCategory = { navigateToSpendingOnCategoryForYear("others", uiState.selectedYear) }
            )
        }
    }
}

@Composable
private fun YearHeader(
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
private fun YearPickerBottomSheet(
    years: List<DropDownItem>,
    onDismissRequest: () -> Unit,
    onSelectYear: (DropDownItem) -> Unit,
    onJumpToCurrent: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Jump to year",
                    fontFamily = dmSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
            item {
                Text(
                    text = "This year",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onJumpToCurrent)
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                )
            }
            items(years) { year ->
                Text(
                    text = year.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectYear(year) }
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                )
            }
        }
    }
}
