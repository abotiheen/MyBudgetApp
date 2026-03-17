package com.example.mybudgetapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BudgetTransaction::class, BudgetCategory::class], version = 5, exportSchema = false)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `categoryKey` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `iconKey` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `isDefault` INTEGER NOT NULL,
                        `isArchived` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        PRIMARY KEY(`categoryKey`)
                    )
                    """.trimIndent()
                )
                database.execSQL(DEFAULT_CATEGORIES_SQL)
            }
        }

        private val DEFAULT_CATEGORIES_SQL =
            """
            INSERT OR REPLACE INTO `categories`
            (`categoryKey`, `name`, `type`, `iconKey`, `colorHex`, `isDefault`, `isArchived`, `sortOrder`)
            VALUES
            ('$CATEGORY_KEY_FOOD', 'Food', '$TRANSACTION_TYPE_EXPENSE', 'fastfood', '#5EBB4A', 1, 0, 0),
            ('$CATEGORY_KEY_TRANSPORTATION', 'Transportation', '$TRANSACTION_TYPE_EXPENSE', 'directions_transit', '#2D9CDB', 1, 0, 1),
            ('$CATEGORY_KEY_OTHERS', 'Others', '$TRANSACTION_TYPE_EXPENSE', 'cookie', '#9AAF47', 1, 0, 2),
            ('$CATEGORY_KEY_INCOME', 'Income', '$TRANSACTION_TYPE_INCOME', 'attach_money', '#4FAF33', 1, 0, 3)
            """.trimIndent()

        private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(DEFAULT_CATEGORIES_SQL)
            }
        }

        fun getDatabase(context: Context): BudgetDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, BudgetDatabase::class.java, "test_database")
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .addCallback(databaseCallback)
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
