package com.example.mybudgetapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

const val TRANSACTION_TYPE_EXPENSE = "expense"
const val TRANSACTION_TYPE_INCOME = "income"
const val DEFAULT_TRANSACTION_TITLE = "item"

@Serializable
@Entity(tableName = "transactions")
data class BudgetTransaction(
    @PrimaryKey(autoGenerate = true)
    val transactionId: Long = 0,
    val title: String? = null,
    val amount: Double,
    val category: String,
    val type: String,
    val transactionDate: String,
    val picturePath: String? = null,
)

@Serializable
data class RecentEntryTemplate(
    val title: String? = null,
    val category: String,
    val amount: Double,
    val type: String,
)

data class MonthPeriod(
    val year: Int,
    val month: Int,
)

data class DailySpendingTotal(
    val day: Int,
    val total: Double,
)

data class MonthlySpendingTotal(
    val month: Int,
    val total: Double,
)

fun BudgetTransaction.displayTitle(): String {
    return resolvedTransactionTitle(title, category, type)
}

fun resolvedTransactionTitle(title: String?, category: String, type: String): String =
    title?.trim().takeUnless { it.isNullOrEmpty() } ?: defaultTransactionTitle(category, type)

fun defaultTransactionTitle(category: String, type: String): String = DEFAULT_TRANSACTION_TITLE
