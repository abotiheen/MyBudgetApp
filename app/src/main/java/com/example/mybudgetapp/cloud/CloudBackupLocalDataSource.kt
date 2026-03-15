package com.example.mybudgetapp.cloud

import androidx.room.withTransaction
import com.example.mybudgetapp.BuildConfig
import com.example.mybudgetapp.database.BudgetDatabase
import com.example.mybudgetapp.database.BudgetTransaction
import com.example.mybudgetapp.database.TRANSACTION_TYPE_EXPENSE
import com.example.mybudgetapp.database.TRANSACTION_TYPE_INCOME
import java.time.Instant

class CloudBackupLocalDataSource(
    private val database: BudgetDatabase,
) {
    suspend fun exportSnapshot(): BackupSnapshot = BackupSnapshot(
        exportedAt = Instant.now().toString(),
        appVersion = BuildConfig.VERSION_CODE,
        transactions = database.transactionDao().getAllTransactions(),
    )

    suspend fun importSnapshot(snapshot: BackupSnapshot) {
        val transactionsToImport = when {
            snapshot.transactions.isNotEmpty() -> snapshot.transactions
            snapshot.items.isNotEmpty() && snapshot.purchases.isNotEmpty() -> snapshot.toTransactions()
            else -> emptyList()
        }

        database.withTransaction {
            database.clearAllTables()
            database.transactionDao().insertTransactions(transactionsToImport)
        }
    }
}

private fun BackupSnapshot.toTransactions(): List<BudgetTransaction> {
    val itemLookup = items.associateBy { it.itemId }
    return purchases.mapNotNull { purchase ->
        val item = itemLookup[purchase.itemId] ?: return@mapNotNull null
        BudgetTransaction(
            transactionId = purchase.purchaseId,
            title = item.name,
            amount = purchase.cost,
            category = item.category,
            type = if (item.category == "income") TRANSACTION_TYPE_INCOME else TRANSACTION_TYPE_EXPENSE,
            transactionDate = purchase.purchaseDate,
            picturePath = item.picturePath,
        )
    }
}
