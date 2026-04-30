package com.example.mybudgetapp.ui.shared.models

import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar
import com.example.mybudgetapp.data.usableImagePath
import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.normalizedTransactionTitleKey
import com.example.mybudgetapp.database.resolvedTransactionTitle
import com.example.mybudgetapp.data.capitalized

data class SpendingItem(
    val imagePath: String? = null,
    val name: String = "",
    val date: String = "",
    val totalCost: String = "",
    val amountValue: Double = 0.0,
    val type: String = "",
    val category: String = "",
    val categoryLabel: String = "",
    val categoryIconKey: String = "",
    val categoryColorHex: String = "",
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
    categoryLookup: Map<String, BudgetCategory>,
): List<SpendingItem> = this
    .groupBy { transaction ->
        SpendingItemGroupKey(
            title = normalizedTransactionTitleKey(transaction.title, transaction.category, transaction.type),
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
            imagePath = usableImagePath(
                latestTransaction.picturePath ?: transactions.firstNotNullOfOrNull { it.picturePath }
            ),
            name = resolvedTransactionTitle(latestTransaction.title, latestTransaction.category, latestTransaction.type),
            date = latestTransaction.transactionDate,
            totalCost = formatCompactCurrencyIraqiDinar(transactions.sumOf { it.amount }),
            amountValue = transactions.sumOf { it.amount },
            type = latestTransaction.type,
            category = latestTransaction.category,
            categoryLabel = categoryLookup[latestTransaction.category]?.name ?: latestTransaction.category.capitalized(),
            categoryIconKey = categoryLookup[latestTransaction.category]?.iconKey.orEmpty(),
            categoryColorHex = categoryLookup[latestTransaction.category]?.colorHex.orEmpty(),
            itemId = latestTransaction.transactionId,
            year = year,
            month = month,
        )
    }
    .sortedWith(compareByDescending<SpendingItem> { it.date }.thenByDescending { it.itemId })

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

