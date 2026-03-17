package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.ui.screens.TotalIncomeDestinationForYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TotalSpendingScreenForYearViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val date: LocalDate = LocalDate.now()
    private val currentYear: Int = checkNotNull(savedStateHandle[TotalIncomeDestinationForYear.year.toString()])
    private val initialIsIncome: Boolean = checkNotNull(savedStateHandle[TotalIncomeDestinationForYear.isIncome.toString()])
    private var isThisYearCurrent = true
    private var isDeleteDialogVisible = MutableStateFlow(false)
    private val selectedTransactionType = MutableStateFlow(initialIsIncome)
    private val screenMeta = combine(
        selectedTransactionType,
        isDeleteDialogVisible,
    ) { isIncome, isDeleteDialogVisible ->
        isIncome to isDeleteDialogVisible
    }

    init {
        isThisYearCurrent = (date.year == currentYear)
    }

    val uiState: StateFlow<TotalSpendingUiState> = combine(
        itemRepository.getTransactionsForYear(year = currentYear),
        itemRepository.getTotalSpendingOverallForYear(year = currentYear),
        itemRepository.getTotalIncomeOverallForYear(year = currentYear),
        itemRepository.getIncomeTransactionsForYear(year = currentYear),
        screenMeta,
    ) { spendingItems, totalSpending, totalIncome, incomeItems, screenMeta ->
        val (isIncome, isDeleteDialogVisible) = screenMeta
        TotalSpendingUiState(
            totalSpending = formatCompactCurrencyIraqiDinar(totalSpending),
            spendingItemList = spendingItems.toGroupedSpendingItems(
                year = currentYear,
                month = 0,
            ),
            month = currentYear.toString(),
            isIncome = isIncome,
            totalIncome = formatCompactCurrencyIraqiDinar(totalIncome),
            incomeItemList = incomeItems.toGroupedSpendingItems(
                year = currentYear,
                month = 0,
            ),
            isThisMonthCurrent = isThisYearCurrent,
            isDeleteDialogVisible = isDeleteDialogVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = TotalSpendingUiState()
    )

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            itemRepository.deleteTransactionWithId(itemId)
        }
    }

    fun displayConfirmDelete(isIt: Boolean) {
        isDeleteDialogVisible.value = isIt
    }

    fun selectTransactionType(isIncome: Boolean) {
        selectedTransactionType.value = isIncome
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
