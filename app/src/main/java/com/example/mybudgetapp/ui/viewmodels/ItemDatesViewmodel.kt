package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_INCOME
import com.example.mybudgetapp.database.displayTitle
import com.example.mybudgetapp.ui.screens.ItemDatesScreenNavigationDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ItemDatesViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id: Long = checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.id.toString()])

    val uiState: StateFlow<ItemDatesUiState> = itemRepository.getTransaction(id)
        .map { transaction ->
            ItemDatesUiState(
                itemDatesList = listOf(transaction.toItemWithDates()),
                category = transaction.category,
                categoryLabel = categoryLabel(transaction.category),
                name = transaction.displayTitle(),
                date = transaction.transactionDate,
                amount = formatCurrencyIraqiDinar(transaction.amount),
                typeLabel = if (transaction.type == TRANSACTION_TYPE_INCOME) "Income" else "Expense",
                picturePath = transaction.picturePath,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = ItemDatesUiState()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ItemDatesUiState(
    val itemDatesList: List<ItemWIthDates> = listOf(),
    val category: String = "",
    val categoryLabel: String = "",
    val picturePath: String? = null,
    val date: String = "",
    val amount: String = "",
    val typeLabel: String = "",
    val name: String = "",
)

data class ItemWIthDates(
    val cost: String = "",
    val date: String = "",
)

fun BudgetTransaction.toItemWithDates(): ItemWIthDates =
    ItemWIthDates(
        cost = formatCurrencyIraqiDinar(amount),
        date = transactionDate
    )
