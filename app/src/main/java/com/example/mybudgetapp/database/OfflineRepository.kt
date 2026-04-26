package com.example.mybudgetapp.database

import kotlinx.coroutines.flow.Flow

class OfflineRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) : ItemRepository {

    override suspend fun insertTransaction(transaction: BudgetTransaction): Long =
        transactionDao.insertTransaction(transaction)

    override suspend fun insertTransactions(transactions: List<BudgetTransaction>) =
        transactionDao.insertTransactions(transactions)

    override suspend fun updateTransaction(transaction: BudgetTransaction) =
        transactionDao.updateTransaction(transaction)

    override suspend fun updateTransactionCategoryForItemInMonth(
        title: String,
        oldCategory: String,
        newCategory: String,
        type: String,
        year: Int,
        month: Int,
    ) = transactionDao.updateTransactionCategoryForItemInMonth(
        title = title,
        oldCategory = oldCategory,
        newCategory = newCategory,
        type = type,
        year = year,
        month = month,
    )

    override suspend fun updateTransactionCategoryForItemInYear(
        title: String,
        oldCategory: String,
        newCategory: String,
        type: String,
        year: Int,
    ) = transactionDao.updateTransactionCategoryForItemInYear(
        title = title,
        oldCategory = oldCategory,
        newCategory = newCategory,
        type = type,
        year = year,
    )

    override suspend fun deleteTransaction(transaction: BudgetTransaction) =
        transactionDao.deleteTransaction(transaction)

    override suspend fun deleteTransactionWithId(id: Long) =
        transactionDao.deleteTransactionWithId(id)

    override suspend fun insertCategory(category: BudgetCategory) =
        categoryDao.insertCategory(category)

    override suspend fun insertCategories(categories: List<BudgetCategory>) =
        categoryDao.insertCategories(categories)

    override suspend fun updateCategory(category: BudgetCategory) =
        categoryDao.updateCategory(category)

    override suspend fun archiveCategory(categoryKey: String) =
        categoryDao.archiveCategory(categoryKey)

    override suspend fun unarchiveCategory(categoryKey: String) =
        categoryDao.unarchiveCategory(categoryKey)

    override fun getTransaction(id: Long): Flow<BudgetTransaction> =
        transactionDao.getTransaction(id)

    override fun getAllCategories(includeArchived: Boolean): Flow<List<BudgetCategory>> =
        categoryDao.getAllCategories(includeArchived)

    override fun getCategoriesByType(type: String, includeArchived: Boolean): Flow<List<BudgetCategory>> =
        categoryDao.getCategoriesByType(type, includeArchived)

    override fun getCategory(categoryKey: String): Flow<BudgetCategory?> =
        categoryDao.getCategory(categoryKey)

    override fun getCategoryTotalsByType(
        type: String,
        year: Int,
        month: Int,
        includeArchived: Boolean,
    ): Flow<List<CategorySpendingTotal>> =
        categoryDao.getCategoryTotalsByType(
            type = type,
            year = year,
            month = month,
            includeArchived = includeArchived,
        )

    override fun getCategoryTotalsByTypeForYear(
        type: String,
        year: Int,
        includeArchived: Boolean,
    ): Flow<List<CategorySpendingTotal>> =
        categoryDao.getCategoryTotalsByTypeForYear(
            type = type,
            year = year,
            includeArchived = includeArchived,
        )

    override fun getTransactionsForItemInMonth(
        title: String,
        category: String,
        type: String,
        year: Int,
        month: Int,
    ): Flow<List<BudgetTransaction>> =
        transactionDao.getTransactionsForItemInMonth(
            title = title,
            category = category,
            type = type,
            year = year,
            month = month,
        )

    override fun getTransactionsForItemInYear(
        title: String,
        category: String,
        type: String,
        year: Int,
    ): Flow<List<BudgetTransaction>> =
        transactionDao.getTransactionsForItemInYear(
            title = title,
            category = category,
            type = type,
            year = year,
        )

    override fun getRecentEntryTemplates(limit: Int): Flow<List<RecentEntryTemplate>> =
        transactionDao.getRecentEntryTemplates(limit)

    override fun getAllMonths(year: Int): Flow<List<Int>> = transactionDao.getAllMonths(year)

    override fun getAvailableMonthPeriods(): Flow<List<MonthPeriod>> =
        transactionDao.getAvailableMonthPeriods()

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

    override fun getDailySpendingTotals(month: Int, year: Int): Flow<List<DailySpendingTotal>> =
        transactionDao.getDailySpendingTotals(month, year)

    override fun getMonthlySpendingTotals(year: Int): Flow<List<MonthlySpendingTotal>> =
        transactionDao.getMonthlySpendingTotals(year)

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
