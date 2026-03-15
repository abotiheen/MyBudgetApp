package com.example.mybudgetapp.cloud

import androidx.room.withTransaction
import com.example.mybudgetapp.BuildConfig
import com.example.mybudgetapp.database.BudgetDatabase
import java.time.Instant

class CloudBackupLocalDataSource(
    private val database: BudgetDatabase,
) {
    suspend fun exportSnapshot(): BackupSnapshot = BackupSnapshot(
        exportedAt = Instant.now().toString(),
        appVersion = BuildConfig.VERSION_CODE,
        items = database.itemDao().getAllItems(),
        purchases = database.purchaseDetailsDao().getAllPurchaseDetails(),
    )

    suspend fun importSnapshot(snapshot: BackupSnapshot) {
        database.withTransaction {
            database.clearAllTables()
            database.itemDao().insertItems(snapshot.items)
            database.purchaseDetailsDao().insertItems(snapshot.purchases)
        }
    }
}
