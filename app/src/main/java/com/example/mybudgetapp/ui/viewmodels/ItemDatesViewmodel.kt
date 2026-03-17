package com.example.mybudgetapp.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_INCOME
import com.example.mybudgetapp.database.resolvedTransactionTitle
import com.example.mybudgetapp.ui.screens.ItemDatesScreenNavigationDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ItemDatesViewModel(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val title: String = Uri.decode(checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.title]))
    private val category: String = checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.category])
    private val type: String = checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.type])
    private val year: Int = checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.year])
    private val month: Int = checkNotNull(savedStateHandle[ItemDatesScreenNavigationDestination.month])

    val uiState: StateFlow<ItemDatesUiState> = if (month == 0) {
        itemRepository.getTransactionsForItemInYear(
            title = title,
            category = category,
            type = type,
            year = year,
        )
    } else {
        itemRepository.getTransactionsForItemInMonth(
            title = title,
            category = category,
            type = type,
            year = year,
            month = month,
        )
    }.map { transactions ->
        val latestTransaction = transactions.firstOrNull()
        val totalAmount = transactions.sumOf { it.amount }
        val displayTitle = latestTransaction?.let {
            resolvedTransactionTitle(it.title, it.category, it.type)
        } ?: title

        val history = transactions.map { it.toItemWithDates() }
        val latestDate = latestTransaction?.transactionDate.orEmpty()

            ItemDatesUiState(
                itemDatesList = history,
                category = latestTransaction?.category ?: category,
                categoryLabel = categoryLabel(latestTransaction?.category ?: category),
                name = displayTitle,
                date = latestDate,
                amount = formatCurrencyIraqiDinar(totalAmount),
                typeLabel = if ((latestTransaction?.type ?: type) == TRANSACTION_TYPE_INCOME) "Income" else "Expense",
                picturePath = latestTransaction?.picturePath ?: transactions.firstNotNullOfOrNull { it.picturePath },
                historyCount = history.size,
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
    val historyCount: Int = 0,
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
