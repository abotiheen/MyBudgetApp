package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.capitalized
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.formatCurrencyIraqiDinar
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.resolvedTransactionTitle
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
    private val initialIsIncome: Boolean = checkNotNull(savedStateHandle[TotalIncomeDestination.isIncome])
    private val date: LocalDate = LocalDate.now()
    private val isDeleteDialogVisible = MutableStateFlow(false)
    private val selectedTransactionType = MutableStateFlow(initialIsIncome)
    private val screenMeta = combine(
        selectedTransactionType,
        isDeleteDialogVisible,
    ) { isIncome, isDeleteDialogVisible ->
        isIncome to isDeleteDialogVisible
    }

    val uiState: StateFlow<TotalSpendingUiState> = combine(
        itemRepository.getTransactions(month = currentMonthValue, year = currentYear),
        itemRepository.getTotalSpendingOverall(year = currentYear, month = currentMonthValue),
        itemRepository.getTotalIncomeOverall(year = currentYear, month = currentMonthValue),
        itemRepository.getIncomeTransactions(year = currentYear, month = currentMonthValue),
        screenMeta,
    ) { spendingItems, totalSpending, totalIncome, incomeItems, screenMeta ->
        val (isIncome, isDeleteDialogVisible) = screenMeta
        TotalSpendingUiState(
            totalSpending = formatCompactCurrencyIraqiDinar(totalSpending),
            spendingItemList = spendingItems.toGroupedSpendingItems(
                year = currentYear,
                month = currentMonthValue,
            ),
            month = "${Month.of(currentMonthValue).toString().capitalized()} $currentYear",
            isIncome = isIncome,
            totalIncome = formatCompactCurrencyIraqiDinar(totalIncome),
            incomeItemList = incomeItems.toGroupedSpendingItems(
                year = currentYear,
                month = currentMonthValue,
            ),
            isThisMonthCurrent = currentYear == date.year && currentMonthValue == date.monthValue,
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
    val amountValue: Double = 0.0,
    val type: String = "",
    val category: String = "",
    val itemId: Long = 0,
    val year: Int = 0,
    val month: Int = 0,
)

private data class SpendingItemGroupKey(
    val title: String,
    val category: String,
    val type: String,
)

fun List<BudgetTransaction>.toGroupedSpendingItems(
    year: Int,
    month: Int,
): List<SpendingItem> = this
    .groupBy { transaction ->
        SpendingItemGroupKey(
            title = resolvedTransactionTitle(transaction.title, transaction.category, transaction.type),
            category = transaction.category,
            type = transaction.type,
        )
    }
    .values
    .map { transactions ->
        val latestTransaction = transactions.maxWithOrNull(
            compareBy<BudgetTransaction> { it.transactionDate }.thenBy { it.transactionId }
        ) ?: transactions.first()
        SpendingItem(
            imagePath = latestTransaction.picturePath ?: transactions.firstNotNullOfOrNull { it.picturePath },
            name = resolvedTransactionTitle(latestTransaction.title, latestTransaction.category, latestTransaction.type),
            date = latestTransaction.transactionDate,
            totalCost = formatCompactCurrencyIraqiDinar(transactions.sumOf { it.amount }),
            amountValue = transactions.sumOf { it.amount },
            type = latestTransaction.type,
            category = latestTransaction.category,
            itemId = latestTransaction.transactionId,
            year = year,
            month = month,
        )
    }
    .sortedWith(compareByDescending<SpendingItem> { it.date }.thenByDescending { it.itemId })
