package com.example.mybudgetapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: BudgetTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<BudgetTransaction>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTransaction(transaction: BudgetTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: BudgetTransaction)

    @Query("delete from transactions where transactionId = :id")
    suspend fun deleteTransactionWithId(id: Long)

    @Query("select * from transactions where transactionId = :id")
    fun getTransaction(id: Long): Flow<BudgetTransaction>

    @Query(
        """
        select * from transactions
        where lower(ifnull(nullif(trim(title), ''), 'item')) = lower(:title)
          and category = :category
          and type = :type
          and cast(strftime('%m', transactionDate) as integer) = :month
          and cast(strftime('%Y', transactionDate) as integer) = :year
        order by transactionDate desc, transactionId desc
        """
    )
    fun getTransactionsForItemInMonth(
        title: String,
        category: String,
        type: String,
        year: Int,
        month: Int,
    ): Flow<List<BudgetTransaction>>

    @Query(
        """
        select * from transactions
        where lower(ifnull(nullif(trim(title), ''), 'item')) = lower(:title)
          and category = :category
          and type = :type
          and cast(strftime('%Y', transactionDate) as integer) = :year
        order by transactionDate desc, transactionId desc
        """
    )
    fun getTransactionsForItemInYear(
        title: String,
        category: String,
        type: String,
        year: Int,
    ): Flow<List<BudgetTransaction>>

    @Query("select * from transactions order by transactionDate desc, transactionId desc")
    suspend fun getAllTransactions(): List<BudgetTransaction>

    @Query(
        """
        select title, category, amount, type
        from transactions
        order by transactionDate desc, transactionId desc
        limit :limit
        """
    )
    fun getRecentEntryTemplates(limit: Int): Flow<List<RecentEntryTemplate>>

    @Query(
        """
        select distinct cast(strftime('%m', transactionDate) as integer)
        from transactions
        where cast(strftime('%Y', transactionDate) as integer) = :year
        order by cast(strftime('%m', transactionDate) as integer) asc
        """
    )
    fun getAllMonths(year: Int): Flow<List<Int>>

    @Query(
        """
        select distinct cast(strftime('%Y', transactionDate) as integer) as year,
               cast(strftime('%m', transactionDate) as integer) as month
        from transactions
        order by year asc, month asc
        """
    )
    fun getAvailableMonthPeriods(): Flow<List<MonthPeriod>>

    @Query(
        """
        select distinct cast(strftime('%Y', transactionDate) as integer)
        from transactions
        order by cast(strftime('%Y', transactionDate) as integer) asc
        """
    )
    fun getAllYears(): Flow<List<Int>>

    @Query(
        """
        select * from transactions
        where type = :type
          and cast(strftime('%m', transactionDate) as integer) = :month
          and cast(strftime('%Y', transactionDate) as integer) = :year
        order by transactionDate desc, transactionId desc
        """
    )
    fun getTransactionsByType(month: Int, year: Int, type: String): Flow<List<BudgetTransaction>>

    @Query(
        """
        select * from transactions
        where type = :type
          and cast(strftime('%Y', transactionDate) as integer) = :year
        order by transactionDate desc, transactionId desc
        """
    )
    fun getTransactionsByTypeForYear(year: Int, type: String): Flow<List<BudgetTransaction>>

    @Query(
        """
        select * from transactions
        where category = :category
          and cast(strftime('%m', transactionDate) as integer) = :month
          and cast(strftime('%Y', transactionDate) as integer) = :year
        order by transactionDate desc, transactionId desc
        """
    )
    fun getTransactionsByCategory(month: Int, year: Int, category: String): Flow<List<BudgetTransaction>>

    @Query(
        """
        select * from transactions
        where category = :category
          and cast(strftime('%Y', transactionDate) as integer) = :year
        order by transactionDate desc, transactionId desc
        """
    )
    fun getTransactionsByCategoryForYear(year: Int, category: String): Flow<List<BudgetTransaction>>

    @Query(
        """
        select cast(strftime('%d', transactionDate) as integer) as day,
               ifnull(sum(amount), 0) as total
        from transactions
        where type = 'expense'
          and cast(strftime('%m', transactionDate) as integer) = :month
          and cast(strftime('%Y', transactionDate) as integer) = :year
        group by day
        order by day asc
        """
    )
    fun getDailySpendingTotals(month: Int, year: Int): Flow<List<DailySpendingTotal>>

    @Query(
        """
        select cast(strftime('%m', transactionDate) as integer) as month,
               ifnull(sum(amount), 0) as total
        from transactions
        where type = 'expense'
          and cast(strftime('%Y', transactionDate) as integer) = :year
        group by month
        order by month asc
        """
    )
    fun getMonthlySpendingTotals(year: Int): Flow<List<MonthlySpendingTotal>>

    @Query(
        """
        select ifnull(sum(amount), 0)
        from transactions
        where category = :category
          and cast(strftime('%Y', transactionDate) as integer) = :year
          and cast(strftime('%m', transactionDate) as integer) = :month
        """
    )
    fun getTotalSpendingOnCategory(category: String, year: Int, month: Int): Flow<Double>

    @Query(
        """
        select ifnull(sum(amount), 0)
        from transactions
        where type = 'expense'
          and cast(strftime('%Y', transactionDate) as integer) = :year
          and cast(strftime('%m', transactionDate) as integer) = :month
        """
    )
    fun getTotalSpendingOverall(year: Int, month: Int): Flow<Double>

    @Query(
        """
        select ifnull(sum(amount), 0)
        from transactions
        where type = 'income'
          and cast(strftime('%Y', transactionDate) as integer) = :year
          and cast(strftime('%m', transactionDate) as integer) = :month
        """
    )
    fun getTotalIncomeOverall(year: Int, month: Int): Flow<Double>

    @Query(
        """
        select ifnull(sum(amount), 0)
        from transactions
        where category = :category
          and cast(strftime('%Y', transactionDate) as integer) = :year
        """
    )
    fun getTotalSpendingOnCategoryForYear(category: String, year: Int): Flow<Double>

    @Query(
        """
        select ifnull(sum(amount), 0)
        from transactions
        where type = 'expense'
          and cast(strftime('%Y', transactionDate) as integer) = :year
        """
    )
    fun getTotalSpendingOverallForYear(year: Int): Flow<Double>

    @Query(
        """
        select ifnull(sum(amount), 0)
        from transactions
        where type = 'income'
          and cast(strftime('%Y', transactionDate) as integer) = :year
        """
    )
    fun getTotalIncomeOverallForYear(year: Int): Flow<Double>
}
