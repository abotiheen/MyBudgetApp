package com.example.mybudgetapp.database

import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    suspend fun insertTransaction(transaction: BudgetTransaction): Long
    suspend fun insertTransactions(transactions: List<BudgetTransaction>)
    suspend fun updateTransaction(transaction: BudgetTransaction)
    suspend fun deleteTransaction(transaction: BudgetTransaction)
    suspend fun deleteTransactionWithId(id: Long)

    fun getTransaction(id: Long): Flow<BudgetTransaction>
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
