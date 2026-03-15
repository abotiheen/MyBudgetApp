package com.example.mybudgetapp.database

import kotlinx.coroutines.flow.Flow

class OfflineRepository(
    private val transactionDao: TransactionDao,
) : ItemRepository {

    override suspend fun insertTransaction(transaction: BudgetTransaction): Long =
        transactionDao.insertTransaction(transaction)

    override suspend fun insertTransactions(transactions: List<BudgetTransaction>) =
        transactionDao.insertTransactions(transactions)

    override suspend fun updateTransaction(transaction: BudgetTransaction) =
        transactionDao.updateTransaction(transaction)

    override suspend fun deleteTransaction(transaction: BudgetTransaction) =
        transactionDao.deleteTransaction(transaction)

    override suspend fun deleteTransactionWithId(id: Long) =
        transactionDao.deleteTransactionWithId(id)

    override fun getTransaction(id: Long): Flow<BudgetTransaction> =
        transactionDao.getTransaction(id)

    override fun getRecentEntryTemplates(limit: Int): Flow<List<RecentEntryTemplate>> =
        transactionDao.getRecentEntryTemplates(limit)

    override fun getAllMonths(year: Int): Flow<List<Int>> = transactionDao.getAllMonths(year)

    override fun getAllYears(): Flow<List<Int>> = transactionDao.getAllYears()

    override fun getTransactions(month: Int, year: Int): Flow<List<BudgetTransaction>> =
        transactionDao.getTransactionsByType(month, year, TRANSACTION_TYPE_EXPENSE)

    override fun getTransactionsForYear(year: Int): Flow<List<BudgetTransaction>> =
        transactionDao.getTransactionsByTypeForYear(year, TRANSACTION_TYPE_EXPENSE)

    override fun getTransactionsByCategory(month: Int, year: Int, category: String): Flow<List<BudgetTransaction>> =
        transactionDao.getTransactionsByCategory(month, year, category)

    override fun getTransactionsByCategoryForYear(year: Int, category: String): Flow<List<BudgetTransaction>> =
        transactionDao.getTransactionsByCategoryForYear(year, category)

    override fun getIncomeTransactions(month: Int, year: Int): Flow<List<BudgetTransaction>> =
        transactionDao.getTransactionsByType(month, year, TRANSACTION_TYPE_INCOME)

    override fun getIncomeTransactionsForYear(year: Int): Flow<List<BudgetTransaction>> =
        transactionDao.getTransactionsByTypeForYear(year, TRANSACTION_TYPE_INCOME)

    override fun getTotalSpendingOnCategory(category: String, year: Int, month: Int): Flow<Double> =
        transactionDao.getTotalSpendingOnCategory(category, year, month)

    override fun getTotalSpendingOverall(year: Int, month: Int): Flow<Double> =
        transactionDao.getTotalSpendingOverall(year, month)

    override fun getTotalIncomeOverall(year: Int, month: Int): Flow<Double> =
        transactionDao.getTotalIncomeOverall(year, month)

    override fun getTotalSpendingOnCategoryForYear(category: String, year: Int): Flow<Double> =
        transactionDao.getTotalSpendingOnCategoryForYear(category, year)

    override fun getTotalSpendingOverallForYear(year: Int): Flow<Double> =
        transactionDao.getTotalSpendingOverallForYear(year)

    override fun getTotalIncomeOverallForYear(year: Int): Flow<Double> =
        transactionDao.getTotalIncomeOverallForYear(year)
}
