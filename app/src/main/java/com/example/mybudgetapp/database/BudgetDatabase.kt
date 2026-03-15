package com.example.mybudgetapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BudgetTransaction::class], version = 4, exportSchema = false)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var instance: BudgetDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `transactions` (
                        `transactionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT,
                        `amount` REAL NOT NULL,
                        `category` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `transactionDate` TEXT NOT NULL,
                        `picturePath` TEXT
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO `transactions` (`transactionId`, `title`, `amount`, `category`, `type`, `transactionDate`, `picturePath`)
                    SELECT
                        p.`purchaseId`,
                        NULLIF(TRIM(i.`name`), ''),
                        p.`cost`,
                        i.`category`,
                        CASE WHEN i.`category` = 'income' THEN 'income' ELSE 'expense' END,
                        p.`purchaseDate`,
                        i.`picturePath`
                    FROM `purchase_details` AS p
                    INNER JOIN `budget_item` AS i ON i.`itemId` = p.`itemId`
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE IF EXISTS `purchase_details`")
                database.execSQL("DROP TABLE IF EXISTS `budget_item`")
            }
        }

        fun getDatabase(context: Context): BudgetDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, BudgetDatabase::class.java, "test_database")
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
