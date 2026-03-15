package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.displayTitle
import com.example.mybudgetapp.ui.screens.TotalIncomeDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month

class TotalSpendingScreenViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentMonthValue: Int = checkNotNull(savedStateHandle[TotalIncomeDestination.month])
    private val currentYear: Int = checkNotNull(savedStateHandle[TotalIncomeDestination.year])
    private val isIncome: Boolean = checkNotNull(savedStateHandle[TotalIncomeDestination.isIncome])
    private val date: LocalDate = LocalDate.now()
    private val isDeleteDialogVisible = MutableStateFlow(false)

    val uiState: StateFlow<TotalSpendingUiState> = combine(
        itemRepository.getTransactions(month = currentMonthValue, year = currentYear),
        itemRepository.getTotalSpendingOverall(year = currentYear, month = currentMonthValue),
        itemRepository.getTotalIncomeOverall(year = currentYear, month = currentMonthValue),
        itemRepository.getIncomeTransactions(year = currentYear, month = currentMonthValue)
    ) { spendingItems, totalSpending, totalIncome, incomeItems ->
        TotalSpendingUiState(
            totalSpending = formatCurrencyIraqiDinar(totalSpending),
            spendingItemList = spendingItems.map { it.toSpendingItem() },
            month = "${Month.of(currentMonthValue).toString().capitalized()} $currentYear",
            isIncome = isIncome,
            totalIncome = formatCurrencyIraqiDinar(totalIncome),
            incomeItemList = incomeItems.map { it.toSpendingItem() },
            isThisMonthCurrent = currentYear == date.year && currentMonthValue == date.monthValue,
            isDeleteDialogVisible = isDeleteDialogVisible.value
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

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class TotalSpendingUiState(
    val isDeleteDialogVisible: Boolean = false,
    val isThisMonthCurrent: Boolean = true,
    val totalSpending: String = "",
    val month: String = "",
    val isIncome: Boolean = true,
    val totalIncome: String = "",
    val spendingItemList: List<SpendingItem> = listOf(),
    val incomeItemList: List<SpendingItem> = listOf()
)

data class SpendingItem(
    val imagePath: String? = null,
    val name: String = "",
    val date: String = "",
    val totalCost: String = "",
    val category: String = "",
    val itemId: Long = 0
)

fun BudgetTransaction.toSpendingItem(): SpendingItem =
    SpendingItem(
        imagePath = picturePath,
        name = displayTitle(),
        date = transactionDate,
        totalCost = formatCurrencyIraqiDinar(amount),
        category = category,
        itemId = transactionId
    )
