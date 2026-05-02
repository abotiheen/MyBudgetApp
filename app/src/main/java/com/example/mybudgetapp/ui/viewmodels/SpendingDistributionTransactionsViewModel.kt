package com.example.mybudgetapp.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.usableImagePath
import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.ItemRepository
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.database.displayTitle
import com.example.mybudgetapp.ui.screens.SpendingDistributionTransactionsDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class SpendingDistributionTransactionsViewModel(
    itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val startDate: String = checkNotNull(savedStateHandle[SpendingDistributionTransactionsDestination.startDate])
    private val endDate: String = checkNotNull(savedStateHandle[SpendingDistributionTransactionsDestination.endDate])
    private val categoryKeys: List<String> = Uri.decode(
        checkNotNull<String>(savedStateHandle[SpendingDistributionTransactionsDestination.categoryKeys])
    ).split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    private val transactionsFlow = if (categoryKeys.isEmpty()) {
        flowOf(emptyList())
    } else {
        itemRepository.getExpenseTransactionsByCategoriesInRange(
            categories = categoryKeys,
            startDate = startDate,
            endDate = endDate,
        )
    }

    val uiState = combine(
        transactionsFlow,
        itemRepository.getCategoriesByType(
            type = TRANSACTION_TYPE_EXPENSE,
            includeArchived = true,
        ),
    ) { transactions, categories ->
        val categoryLookup = categories.associateBy { it.categoryKey }
        val totalAmount = transactions.sumOf { it.amount }
        SpendingDistributionTransactionsUiState(
            title = buildTitle(categoryKeys, categoryLookup),
            periodLabel = "${LocalDate.parse(startDate).toShortLabel()} - ${LocalDate.parse(endDate).toShortLabel()}",
            totalAmount = totalAmount,
            totalLabel = formatCompactCurrencyIraqiDinar(totalAmount),
            transactions = transactions.map { it.toDistributionTransactionUi(categoryLookup) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = SpendingDistributionTransactionsUiState(),
    )

    private fun buildTitle(
        keys: List<String>,
        categoryLookup: Map<String, BudgetCategory>,
    ): String = when {
        keys.isEmpty() -> "Transactions"
        keys.size == 1 -> categoryLookup[keys.first()]?.name ?: keys.first()
        else -> "Grouped categories"
    }

    private fun BudgetTransaction.toDistributionTransactionUi(
        categoryLookup: Map<String, BudgetCategory>,
    ): DistributionTransactionUi {
        val parsedDate = LocalDate.parse(transactionDate)
        val category = categoryLookup[category]
        return DistributionTransactionUi(
            id = transactionId,
            title = displayTitle(),
            amount = formatCompactCurrencyIraqiDinar(amount),
            date = transactionDate,
            displayDate = parsedDate.toShortLabel(),
            categoryKey = this.category,
            categoryLabel = category?.name ?: this.category,
            categoryIconKey = category?.iconKey.orEmpty(),
            categoryColorHex = category?.colorHex.orEmpty(),
            imagePath = usableImagePath(picturePath),
            type = type,
            year = parsedDate.year,
            month = parsedDate.monthValue,
        )
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class SpendingDistributionTransactionsUiState(
    val title: String = "Transactions",
    val periodLabel: String = "",
    val totalAmount: Double = 0.0,
    val totalLabel: String = "",
    val transactions: List<DistributionTransactionUi> = emptyList(),
)

data class DistributionTransactionUi(
    val id: Long,
    val title: String,
    val amount: String,
    val date: String,
    val displayDate: String,
    val categoryKey: String,
    val categoryLabel: String,
    val categoryIconKey: String,
    val categoryColorHex: String,
    val imagePath: String?,
    val type: String,
    val year: Int,
    val month: Int,
)

private fun LocalDate.toShortLabel(): String =
    format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
