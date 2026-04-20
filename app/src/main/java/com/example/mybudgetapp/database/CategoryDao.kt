package com.example.mybudgetapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: BudgetCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<BudgetCategory>)

    @Update
    suspend fun updateCategory(category: BudgetCategory)

    @Query(
        """
        select * from categories
        where (:includeArchived = 1 or isArchived = 0)
        order by isArchived asc, sortOrder asc, name asc
        """
    )
    fun getAllCategories(includeArchived: Boolean): Flow<List<BudgetCategory>>

    @Query(
        """
        select * from categories
        where type = :type
          and (:includeArchived = 1 or isArchived = 0)
        order by isArchived asc, sortOrder asc, name asc
        """
    )
    fun getCategoriesByType(type: String, includeArchived: Boolean): Flow<List<BudgetCategory>>

    @Query("select * from categories where categoryKey = :categoryKey limit 1")
    fun getCategory(categoryKey: String): Flow<BudgetCategory?>

    @Query(
        """
        select
            c.categoryKey as categoryKey,
            c.name as categoryName,
            c.type as type,
            c.iconKey as iconKey,
            c.colorHex as colorHex,
            c.isArchived as isArchived,
            c.sortOrder as sortOrder,
            ifnull(sum(t.amount), 0) as total
        from categories c
        left join transactions t
            on t.category = c.categoryKey
           and t.type = c.type
           and cast(strftime('%Y', t.transactionDate) as integer) = :year
           and cast(strftime('%m', t.transactionDate) as integer) = :month
        where c.type = :type
          and (:includeArchived = 1 or c.isArchived = 0)
        group by c.categoryKey, c.name, c.type, c.iconKey, c.colorHex, c.isArchived, c.sortOrder
        order by total desc, c.sortOrder asc, c.name asc
        """
    )
    fun getCategoryTotalsByType(
        type: String,
        year: Int,
        month: Int,
        includeArchived: Boolean,
    ): Flow<List<CategorySpendingTotal>>

    @Query(
        """
        select
            c.categoryKey as categoryKey,
            c.name as categoryName,
            c.type as type,
            c.iconKey as iconKey,
            c.colorHex as colorHex,
            c.isArchived as isArchived,
            c.sortOrder as sortOrder,
            ifnull(sum(t.amount), 0) as total
        from categories c
        left join transactions t
            on t.category = c.categoryKey
           and t.type = c.type
           and cast(strftime('%Y', t.transactionDate) as integer) = :year
        where c.type = :type
          and (:includeArchived = 1 or c.isArchived = 0)
        group by c.categoryKey, c.name, c.type, c.iconKey, c.colorHex, c.isArchived, c.sortOrder
        order by total desc, c.sortOrder asc, c.name asc
        """
    )
    fun getCategoryTotalsByTypeForYear(
        type: String,
        year: Int,
        includeArchived: Boolean,
    ): Flow<List<CategorySpendingTotal>>

    @Query(
        """
        update categories
        set isArchived = 1
        where categoryKey = :categoryKey
        """
    )
    suspend fun archiveCategory(categoryKey: String)

    @Query(
        """
        select * from categories
        order by isArchived asc, sortOrder asc, name asc
        """
    )
    suspend fun getAllCategoriesList(): List<BudgetCategory>
}
