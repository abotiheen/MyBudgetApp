package com.example.mybudgetapp.database

import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    suspend fun insertTransaction(transaction: BudgetTransaction): Long
    suspend fun insertTransactions(transactions: List<BudgetTransaction>)
    suspend fun updateTransaction(transaction: BudgetTransaction)
    suspend fun updateTransactionCategoryForItemInMonth(
        title: String,
        oldCategory: String,
        newCategory: String,
        type: String,
        year: Int,
        month: Int,
    )
    suspend fun updateTransactionCategoryForItemInYear(
        title: String,
        oldCategory: String,
        newCategory: String,
        type: String,
        year: Int,
    )
    suspend fun deleteTransaction(transaction: BudgetTransaction)
    suspend fun deleteTransactionWithId(id: Long)
    suspend fun insertCategory(category: BudgetCategory)
    suspend fun insertCategories(categories: List<BudgetCategory>)
    suspend fun updateCategory(category: BudgetCategory)
    suspend fun archiveCategory(categoryKey: String)
    suspend fun unarchiveCategory(categoryKey: String)

    fun getTransaction(id: Long): Flow<BudgetTransaction>
    fun getAllCategories(includeArchived: Boolean = true): Flow<List<BudgetCategory>>
    fun getCategoriesByType(type: String, includeArchived: Boolean = false): Flow<List<BudgetCategory>>
    fun getCategory(categoryKey: String): Flow<BudgetCategory?>
    fun getCategoryTotalsByType(type: String, year: Int, month: Int, includeArchived: Boolean = false): Flow<List<CategorySpendingTotal>>
    fun getCategoryTotalsByTypeForYear(type: String, year: Int, includeArchived: Boolean = false): Flow<List<CategorySpendingTotal>>
    fun getTransactionsForItemInMonth(title: String, category: String, type: String, year: Int, month: Int): Flow<List<BudgetTransaction>>
    fun getTransactionsForItemInYear(title: String, category: String, type: String, year: Int): Flow<List<BudgetTransaction>>
    fun getRecentEntryTemplates(limit: Int): Flow<List<RecentEntryTemplate>>
    fun getAllMonths(year: Int): Flow<List<Int>>
    fun getAvailableMonthPeriods(): Flow<List<MonthPeriod>>
    fun getAllYears(): Flow<List<Int>>

    fun getTransactions(month: Int, year: Int): Flow<List<BudgetTransaction>>
    fun getTransactionsForYear(year: Int): Flow<List<BudgetTransaction>>
    fun getTransactionsByCategory(month: Int, year: Int, category: String): Flow<List<BudgetTransaction>>
    fun getTransactionsByCategoryForYear(year: Int, category: String): Flow<List<BudgetTransaction>>
    fun getIncomeTransactions(month: Int, year: Int): Flow<List<BudgetTransaction>>
    fun getIncomeTransactionsForYear(year: Int): Flow<List<BudgetTransaction>>
    fun getDailySpendingTotals(month: Int, year: Int): Flow<List<DailySpendingTotal>>
    fun getMonthlySpendingTotals(year: Int): Flow<List<MonthlySpendingTotal>>

    fun getTotalSpendingOnCategory(category: String, year: Int, month: Int): Flow<Double>
    fun getTotalSpendingOverall(year: Int, month: Int): Flow<Double>
    fun getTotalIncomeOverall(year: Int, month: Int): Flow<Double>
    fun getTotalSpendingOnCategoryForYear(category: String, year: Int): Flow<Double>
    fun getTotalSpendingOverallForYear(year: Int): Flow<Double>
    fun getTotalIncomeOverallForYear(year: Int): Flow<Double>
}
