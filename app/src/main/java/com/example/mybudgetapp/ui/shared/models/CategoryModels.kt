package com.example.mybudgetapp.ui.shared.models

import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.normalizedTransactionTitleKey
import com.example.mybudgetapp.database.resolvedTransactionTitle
import com.example.mybudgetapp.data.usableImagePath
import com.example.mybudgetapp.data.formatCompactCurrencyIraqiDinar

data class SpendingOnCategoryUiState(
    val isDeleteDialogVisible: Boolean = false,
    val isThisMonthCurrent: Boolean = true,
    val category: String = "",
    val sentCategory: String = "",
    val periodLabel: String = "",
    val totalSpending: String = "",
    val totalCategory: String = "",
    val spendingRatio: Float = 0f,
    val transactionCount: Int = 0,
    val averageTransaction: String = "",
    val biggestTransaction: String = "",
    val itemList: List<SpendingOnCategoryItem> = listOf(),
    val categoryIconKey: String = "",
    val categoryColorHex: String = "",
)

data class SpendingOnCategoryItem(
    val imagePath: String? = null,
    val name: String = "",
    val date: String = "",
    val totalCost: String = "",
    val amountValue: Double = 0.0,
    val itemId: Long = 0,
    val category: String = "",
    val type: String = "",
    val year: Int = 0,
    val month: Int = 0,
)

fun List<BudgetTransaction>.toGroupedSpendingOnCategoryItems(
    year: Int,
    month: Int,
): List<SpendingOnCategoryItem> = this
    .groupBy { transaction ->
        normalizedTransactionTitleKey(transaction.title, transaction.category, transaction.type)
    }
    .values
    .map { transactions ->
        val latestTransaction = transactions.maxWithOrNull(
            compareBy<BudgetTransaction> { it.transactionDate }.thenBy { it.transactionId }
        ) ?: transactions.first()
        SpendingOnCategoryItem(
            itemId = latestTransaction.transactionId,
            imagePath = usableImagePath(
                latestTransaction.picturePath ?: transactions.firstNotNullOfOrNull { it.picturePath }
            ),
            name = resolvedTransactionTitle(latestTransaction.title, latestTransaction.category, latestTransaction.type),
            date = latestTransaction.transactionDate,
            totalCost = formatCompactCurrencyIraqiDinar(transactions.sumOf { it.amount }),
            amountValue = transactions.sumOf { it.amount },
            category = latestTransaction.category,
            type = latestTransaction.type,
            year = year,
            month = month,
        )
    }
    .sortedWith(compareByDescending<SpendingOnCategoryItem> { it.date }.thenByDescending { it.itemId })
