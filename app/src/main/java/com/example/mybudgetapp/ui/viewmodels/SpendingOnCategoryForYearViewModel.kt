package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.ui.screens.SpendingOnCategoryForYearDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class SpendingOnCategoryForYearScreenViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val date: LocalDate = LocalDate.now()
    private val currentYear: Int = checkNotNull(savedStateHandle[SpendingOnCategoryForYearDestination.year.toString()])
    private val category: String = checkNotNull(savedStateHandle[SpendingOnCategoryForYearDestination.category])
    private var isThisYearCurrent: Boolean = true
    private var isDeleteDialogVisible = MutableStateFlow(false)

    init {
        isThisYearCurrent = (date.year == currentYear)
    }

    val uiState: StateFlow<SpendingOnCategoryUiState> = combine(
        itemRepository.getTransactionsByCategoryForYear(year = currentYear, category = category),
        itemRepository.getTotalSpendingOnCategoryForYear(
            year = currentYear,
            category = category
        ),
        itemRepository.getTotalSpendingOverallForYear(
            year = currentYear,
        ),
        itemRepository.getCategory(category),
    ) { itemList, totalCategory, totalSpending, categoryDetails ->
        val mappedItems = itemList.toGroupedSpendingOnCategoryItems(
            year = currentYear,
            month = 0,
        )
        val averageAmount = if (mappedItems.isEmpty()) 0.0 else totalCategory / mappedItems.size
        val biggestAmount = itemList.maxOfOrNull { it.amount } ?: 0.0
        SpendingOnCategoryUiState(
            totalSpending = formatCompactCurrencyIraqiDinar(totalSpending),
            totalCategory = formatCompactCurrencyIraqiDinar(totalCategory),
            spendingRatio = if (totalSpending == 0.0) 0f else totalCategory.toFloat() / totalSpending.toFloat(),
            itemList = mappedItems,
            isThisMonthCurrent = isThisYearCurrent,
            category = categoryDetails?.name ?: category.capitalized(),
            sentCategory = category,
            periodLabel = currentYear.toString(),
            transactionCount = mappedItems.size,
            averageTransaction = formatCompactCurrencyIraqiDinar(averageAmount),
            biggestTransaction = formatCompactCurrencyIraqiDinar(biggestAmount),
            isDeleteDialogVisible = isDeleteDialogVisible.value,
            categoryIconKey = categoryDetails?.iconKey.orEmpty(),
            categoryColorHex = categoryDetails?.colorHex.orEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = SpendingOnCategoryUiState()
    )

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            itemRepository.deleteTransactionWithId(itemId)
        }
    }

    fun displayConfirmDelete(isIt: Boolean) {
        isDeleteDialogVisible.value = isIt
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
