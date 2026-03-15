package com.example.mybudgetapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

const val TRANSACTION_TYPE_EXPENSE = "expense"
const val TRANSACTION_TYPE_INCOME = "income"

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

fun BudgetTransaction.displayTitle(): String {
    return title?.trim().takeUnless { it.isNullOrEmpty() } ?: defaultTransactionTitle(category, type)
}

fun defaultTransactionTitle(category: String, type: String): String = when {
    type == TRANSACTION_TYPE_INCOME -> "Income"
    category == "food" -> "Food"
    category == "transportation" -> "Transit"
    category == "others" -> "Other expense"
    else -> "Expense"
}
