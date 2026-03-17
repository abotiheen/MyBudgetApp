package com.example.mybudgetapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

const val TRANSACTION_TYPE_EXPENSE = "expense"
const val TRANSACTION_TYPE_INCOME = "income"
const val DEFAULT_TRANSACTION_TITLE = "item"
const val CATEGORY_KEY_FOOD = "food"
const val CATEGORY_KEY_TRANSPORTATION = "transportation"
const val CATEGORY_KEY_OTHERS = "others"
const val CATEGORY_KEY_INCOME = "income"

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
@Entity(tableName = "categories")
data class BudgetCategory(
    @PrimaryKey
    val categoryKey: String,
    val name: String,
    val type: String,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
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

data class CategorySpendingTotal(
    val categoryKey: String,
    val categoryName: String,
    val type: String,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean,
    val sortOrder: Int,
    val total: Double,
)

fun BudgetTransaction.displayTitle(): String {
    return resolvedTransactionTitle(title, category, type)
}

fun resolvedTransactionTitle(title: String?, category: String, type: String): String =
    title?.trim().takeUnless { it.isNullOrEmpty() } ?: defaultTransactionTitle(category, type)

fun defaultTransactionTitle(category: String, type: String): String = DEFAULT_TRANSACTION_TITLE

fun defaultBudgetCategories(): List<BudgetCategory> = listOf(
    BudgetCategory(
        categoryKey = CATEGORY_KEY_FOOD,
        name = "Food",
        type = TRANSACTION_TYPE_EXPENSE,
        iconKey = "fastfood",
        colorHex = "#5EBB4A",
        isDefault = true,
        sortOrder = 0,
    ),
    BudgetCategory(
        categoryKey = CATEGORY_KEY_TRANSPORTATION,
        name = "Transportation",
        type = TRANSACTION_TYPE_EXPENSE,
        iconKey = "directions_transit",
        colorHex = "#2D9CDB",
        isDefault = true,
        sortOrder = 1,
    ),
    BudgetCategory(
        categoryKey = CATEGORY_KEY_OTHERS,
        name = "Others",
        type = TRANSACTION_TYPE_EXPENSE,
        iconKey = "cookie",
        colorHex = "#9AAF47",
        isDefault = true,
        sortOrder = 2,
    ),
    BudgetCategory(
        categoryKey = CATEGORY_KEY_INCOME,
        name = "Income",
        type = TRANSACTION_TYPE_INCOME,
        iconKey = "attach_money",
        colorHex = "#4FAF33",
        isDefault = true,
        sortOrder = 3,
    ),
)
