package com.example.mybudgetapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.SpendingCategoryDisplayObject
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.ComparisonDirection
import com.example.mybudgetapp.ui.viewmodels.HomeTransactionPreview
import com.example.mybudgetapp.ui.viewmodels.ThisYearScreenUiState
import com.example.mybudgetapp.ui.viewmodels.ThisYearScreenViewModel
import com.example.mybudgetapp.ui.widgets.BottomNavigationBar
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.DropDownItem
import com.example.mybudgetapp.ui.widgets.SectionHeading
import com.example.mybudgetapp.ui.widgets.TrendChartCard

object ThisYearDestination : NavigationDestination {
    override val route = "ThisYearScreen"
    override val titleRes = R.string.this_month_screen
}

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
                modifier = Modifier,
                uiState = uiState.value,
                onPreviousYear = viewModel::selectPreviousYear,
                onNextYear = viewModel::selectNextYear,
                onOpenYearPicker = { isYearPickerVisible = true },
                navigateToSpendingOnCategoryForYear = navigateToSpendingOnCategoryForYear,
                navigateToTotalIncomeForYear = navigateToTotalIncomeForYear,
                navigateToInsights = navigateToInsights,
                navigateToThisMonthScreen = navigateToThisMonthScreen,
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
    navigateToThisMonthScreen: () -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val netBalance = formatCurrencyIraqiDinar(uiState.totalIncomeAmountForYear - uiState.totalSpendingAmountForYear)
    val totalSpending = uiState.totalSpendingAmountForYear.takeIf { it > 0 } ?: 1.0
    val topCategory = uiState.insights.getOrNull(0)
    val averageMonthly = uiState.insights.getOrNull(3)

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
            YearlyHomeHeader(
                yearLabel = uiState.currentYear,
                onOpenMonth = navigateToThisMonthScreen,
            )
        }
        item {
            YearNavigatorCard(
                label = uiState.currentYear,
                canNavigatePrevious = uiState.canNavigatePrevious,
                canNavigateNext = uiState.canNavigateNext,
                onPrevious = onPreviousYear,
                onNext = onNextYear,
                onOpenPicker = onOpenYearPicker,
            )
        }
        item {
            YearlyPulseHeroCard(
                yearLabel = uiState.currentYear,
                isCurrentYear = uiState.isCurrentYear,
                netBalance = netBalance,
                income = uiState.totalIncomeForYear,
                spending = uiState.totalSpendingForYear,
                onOpenInsights = { navigateToInsights(uiState.selectedYear) },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                YearOverviewMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Income",
                    value = uiState.totalIncomeForYear,
                    subtitle = "Cash in this year",
                    accent = BudgetTheme.extendedColors.income,
                    icon = R.drawable.baseline_attach_money_24,
                    onClick = {
                        navigateToTotalIncomeForYear(uiState.selectedYear, true)
                    },
                )
                YearOverviewMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Spent",
                    value = uiState.totalSpendingForYear,
                    subtitle = "Cash out this year",
                    accent = BudgetTheme.extendedColors.danger,
                    icon = R.drawable.baseline_money_off_24,
                    onClick = {
                        navigateToTotalIncomeForYear(uiState.selectedYear, false)
                    },
                )
            }
        }
        item {
            YearlySignalCard(
                comparisonTitle = uiState.comparison.title,
                comparisonSummary = uiState.comparison.summary,
                comparisonDirection = uiState.comparison.direction,
                topCategory = topCategory?.value ?: "None",
                topCategoryCaption = topCategory?.subtitle ?: "No category leader yet",
                averageMonthly = averageMonthly?.value ?: formatCurrencyIraqiDinar(0.0),
            )
        }
        item {
            TrendChartCard(
                title = "Monthly spending trend",
                subtitle = "A year-level view to catch drift, spikes, and recovery months.",
                points = uiState.spendingTrend,
            )
        }
        item {
            SectionHeading(
                title = "Category spend",
                subtitle = "Annual category weight across food, transit, and everything else.",
            )
        }
        item {
            YearCategoryBreakdownCard(
                label = stringResource(id = SpendingCategoryDisplayObject.items[0].title),
                amount = uiState.totalSpendingOnFoodForYear,
                progress = (uiState.totalFoodAmountForYear / totalSpending).toFloat().coerceIn(0f, 1f),
                accent = BudgetTheme.extendedColors.food,
                icon = SpendingCategoryDisplayObject.items[0].spendingIcon,
                onClick = {
                    navigateToSpendingOnCategoryForYear("food", uiState.selectedYear)
                },
            )
        }
        item {
            YearCategoryBreakdownCard(
                label = stringResource(id = SpendingCategoryDisplayObject.items[1].title),
                amount = uiState.totalSpendingOnTransportationForYear,
                progress = (uiState.totalTransportationAmountForYear / totalSpending).toFloat().coerceIn(0f, 1f),
                accent = BudgetTheme.extendedColors.transit,
                icon = SpendingCategoryDisplayObject.items[1].spendingIcon,
                onClick = {
                    navigateToSpendingOnCategoryForYear("transportation", uiState.selectedYear)
                },
            )
        }
        item {
            YearCategoryBreakdownCard(
                label = stringResource(id = SpendingCategoryDisplayObject.items[2].title),
                amount = uiState.totalSpendingOnOthersForYear,
                progress = (uiState.totalOthersAmountForYear / totalSpending).toFloat().coerceIn(0f, 1f),
                accent = BudgetTheme.extendedColors.others,
                icon = SpendingCategoryDisplayObject.items[2].spendingIcon,
                onClick = {
                    navigateToSpendingOnCategoryForYear("others", uiState.selectedYear)
                },
            )
        }
        if (uiState.recentTransactions.isNotEmpty()) {
            item {
                YearRecentTransactionsCard(
                    items = uiState.recentTransactions,
                    onViewAll = {
                        navigateToTotalIncomeForYear(uiState.selectedYear, false)
                    },
                )
            }
        }
    }
}

@Composable
private fun YearlyHomeHeader(
    yearLabel: String,
    onOpenMonth: () -> Unit,
) {
    val spacing = BudgetTheme.spacing
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Text(
                text = "Year view",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Your annual money picture for $yearLabel.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shape = RoundedCornerShape(BudgetTheme.radii.pill),
            shadowElevation = BudgetTheme.elevations.level2,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                YearModeChip(
                    label = "Year",
                    selected = true,
                    modifier = Modifier.weight(1f),
                    onClick = {},
                )
                YearModeChip(
                    label = "Month",
                    selected = false,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenMonth,
                )
            }
        }
    }
}

@Composable
private fun YearModeChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun YearNavigatorCard(
    label: String,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenPicker: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        shadowElevation = BudgetTheme.elevations.level2,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = onPrevious, enabled = canNavigatePrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = null,
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onOpenPicker),
            ) {
                Text(
                    text = "Selected year",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onNext, enabled = canNavigateNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun YearlyPulseHeroCard(
    yearLabel: String,
    isCurrentYear: Boolean,
    netBalance: String,
    income: String,
    spending: String,
    onOpenInsights: () -> Unit,
) {
    val extendedColors = BudgetTheme.extendedColors
    val spacing = BudgetTheme.spacing

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level3),
        onClick = onOpenInsights,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            extendedColors.heroStart,
                            extendedColors.heroEnd,
                        )
                    )
                )
                .padding(spacing.xl)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(112.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.06f),
                        shape = CircleShape,
                    )
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Text(
                            text = yearLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.74f),
                        )
                        Text(
                            text = "Yearly balance",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(BudgetTheme.radii.pill),
                    ) {
                        Text(
                            text = if (isCurrentYear) "Live year" else "Archived year",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text(
                        text = netBalance,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = "Tap for trend and yearly habit insights",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    YearHeroAmountPill(
                        modifier = Modifier.weight(1f),
                        label = "Income",
                        value = income,
                    )
                    YearHeroAmountPill(
                        modifier = Modifier.weight(1f),
                        label = "Spent",
                        value = spending,
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                        shape = CircleShape,
                    ) {
                        Box(
                            modifier = Modifier.size(52.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearHeroAmountPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(BudgetTheme.radii.md),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun YearOverviewMetricCard(
    label: String,
    value: String,
    subtitle: String,
    accent: Color,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Surface(
                color = accent.copy(alpha = 0.12f),
                shape = CircleShape,
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
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
}

@Composable
private fun YearlySignalCard(
    comparisonTitle: String,
    comparisonSummary: String,
    comparisonDirection: ComparisonDirection,
    topCategory: String,
    topCategoryCaption: String,
    averageMonthly: String,
) {
    val statusColor = when (comparisonDirection) {
        ComparisonDirection.Up -> BudgetTheme.extendedColors.danger
        ComparisonDirection.Down -> BudgetTheme.extendedColors.success
        ComparisonDirection.Flat -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Text(
                text = comparisonTitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = comparisonSummary,
                style = MaterialTheme.typography.headlineSmall,
                color = statusColor,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            ) {
                YearSignalMetric(
                    modifier = Modifier.weight(1f),
                    title = "Top category",
                    value = topCategory,
                    subtitle = topCategoryCaption,
                )
                YearSignalMetric(
                    modifier = Modifier.weight(1f),
                    title = "Avg monthly",
                    value = averageMonthly,
                    subtitle = "Smoothed across 12 months",
                )
            }
        }
    }
}

@Composable
private fun YearSignalMetric(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(BudgetTheme.radii.md),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
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
private fun YearCategoryBreakdownCard(
    label: String,
    amount: String,
    progress: Float,
    accent: Color,
    icon: Int,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = accent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(BudgetTheme.radii.md),
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                                tint = accent,
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = amount,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = accent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun YearRecentTransactionsCard(
    items: List<HomeTransactionPreview>,
    onViewAll: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeading(
                    title = "Recent expenses",
                    subtitle = "The latest annual feed preview before you open the full transaction center.",
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "View all",
                    modifier = Modifier.clickable(onClick = onViewAll),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items.forEachIndexed { index, item ->
                YearRecentTransactionRow(item = item)
                if (index != items.lastIndex) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun YearRecentTransactionRow(
    item: HomeTransactionPreview,
) {
    val accent = when (item.categoryKey) {
        "food" -> BudgetTheme.extendedColors.food
        "transportation" -> BudgetTheme.extendedColors.transit
        "others" -> BudgetTheme.extendedColors.others
        else -> BudgetTheme.extendedColors.income
    }

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
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(accent, CircleShape)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${item.category} • ${item.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = item.amount,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
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
                    subtitle = "Move across annual views without stepping one year at a time.",
                )
            }
            item {
                YearSheetRow(
                    label = "This year",
                    onClick = onJumpToCurrent,
                )
            }
            items(years) { year ->
                YearSheetRow(
                    label = year.title,
                    onClick = { onSelectYear(year) },
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
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.md),
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
