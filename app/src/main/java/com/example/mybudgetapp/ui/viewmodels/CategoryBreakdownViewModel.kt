package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.database.TRANSACTION_TYPE_INCOME
import com.example.mybudgetapp.ui.screens.CategoryBreakdownDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Month

class CategoryBreakdownViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val scope: String = checkNotNull(savedStateHandle[CategoryBreakdownDestination.scope])
    private val year: Int = checkNotNull(savedStateHandle[CategoryBreakdownDestination.year])
    private val month: Int = checkNotNull(savedStateHandle[CategoryBreakdownDestination.month])
    private val selectedType = MutableStateFlow(TRANSACTION_TYPE_EXPENSE)

    val uiState: StateFlow<CategoryBreakdownUiState> = if (scope == InsightScope.Year.routeValue) {
        combine(
            selectedType,
            itemRepository.getTotalSpendingOverallForYear(year),
            itemRepository.getTotalIncomeOverallForYear(year),
            itemRepository.getCategoryTotalsByTypeForYear(
                type = TRANSACTION_TYPE_EXPENSE,
                year = year,
                includeArchived = true,
            ),
            itemRepository.getCategoryTotalsByTypeForYear(
                type = TRANSACTION_TYPE_INCOME,
                year = year,
                includeArchived = true,
            ),
        ) { currentType, totalSpending, totalIncome, expenseCategories, incomeCategories ->
            buildUiState(
                currentType = currentType,
                periodLabel = year.toString(),
                scope = InsightScope.Year,
                expenseTotal = totalSpending,
                incomeTotal = totalIncome,
                expenseCategories = expenseCategories.toCategorySummaryUi(),
                incomeCategories = incomeCategories.toCategorySummaryUi(),
            )
        }
    } else {
        val selectedMonth = month.coerceIn(1, 12)
        combine(
            selectedType,
            itemRepository.getTotalSpendingOverall(year, selectedMonth),
            itemRepository.getTotalIncomeOverall(year, selectedMonth),
            itemRepository.getCategoryTotalsByType(
                type = TRANSACTION_TYPE_EXPENSE,
                year = year,
                month = selectedMonth,
                includeArchived = true,
            ),
            itemRepository.getCategoryTotalsByType(
                type = TRANSACTION_TYPE_INCOME,
                year = year,
                month = selectedMonth,
                includeArchived = true,
            ),
        ) { currentType, totalSpending, totalIncome, expenseCategories, incomeCategories ->
            buildUiState(
                currentType = currentType,
                periodLabel = "${Month.of(selectedMonth).name.capitalized()} $year",
                scope = InsightScope.Month,
                expenseTotal = totalSpending,
                incomeTotal = totalIncome,
                expenseCategories = expenseCategories.toCategorySummaryUi(),
                incomeCategories = incomeCategories.toCategorySummaryUi(),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = CategoryBreakdownUiState(),
    )

    fun selectType(isIncome: Boolean) {
        selectedType.value = if (isIncome) TRANSACTION_TYPE_INCOME else TRANSACTION_TYPE_EXPENSE
    }

    private fun buildUiState(
        currentType: String,
        periodLabel: String,
        scope: InsightScope,
        expenseTotal: Double,
        incomeTotal: Double,
        expenseCategories: List<CategorySummaryUi>,
        incomeCategories: List<CategorySummaryUi>,
    ): CategoryBreakdownUiState {
        val isIncome = currentType == TRANSACTION_TYPE_INCOME
        val selectedCategories = if (isIncome) incomeCategories else expenseCategories
        val selectedTotal = if (isIncome) incomeTotal else expenseTotal
        return CategoryBreakdownUiState(
            periodLabel = periodLabel,
            scope = scope,
            isIncome = isIncome,
            totalAmount = selectedTotal,
            totalLabel = formatCurrencyIraqiDinar(selectedTotal),
            categories = selectedCategories.map { category ->
                CategoryBreakdownCategoryUi(
                    categoryKey = category.categoryKey,
                    label = category.categoryLabel,
                    totalAmount = category.total,
                    totalLabel = formatCompactCurrencyIraqiDinar(category.total),
                    ratio = if (selectedTotal == 0.0) 0f else (category.total / selectedTotal).toFloat().coerceIn(0f, 1f),
                    iconKey = category.iconKey,
                    colorHex = category.colorHex,
                    isArchived = category.isArchived,
                    canOpenDetails = !isIncome && category.total > 0.0,
                )
            },
        )
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class CategoryBreakdownUiState(
    val periodLabel: String = "",
    val scope: InsightScope = InsightScope.Month,
    val isIncome: Boolean = false,
    val totalAmount: Double = 0.0,
    val totalLabel: String = "",
    val categories: List<CategoryBreakdownCategoryUi> = emptyList(),
)

data class CategoryBreakdownCategoryUi(
    val categoryKey: String,
    val label: String,
    val totalAmount: Double,
    val totalLabel: String,
    val ratio: Float,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean,
    val canOpenDetails: Boolean,
)
