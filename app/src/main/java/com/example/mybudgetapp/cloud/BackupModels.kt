package com.example.mybudgetapp.cloud

import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.database.BudgetTransaction
import kotlinx.serialization.Serializable

// Keep these field names stable so existing JSON backups remain restorable across app updates.
@Serializable
data class BackupSnapshot(
    val exportedAt: String,
    val appVersion: Int,
    val categories: List<BudgetCategory> = emptyList(),
    val transactions: List<BudgetTransaction> = emptyList(),
    val items: List<LegacyBackupItem> = emptyList(),
    val purchases: List<LegacyBackupPurchase> = emptyList(),
)

@Serializable
data class LegacyBackupItem(
    val itemId: Long = 0,
    val name: String,
    val date: String,
    val category: String,
    val picturePath: String? = null,
)

@Serializable
data class LegacyBackupPurchase(
    val purchaseId: Long = 0,
    val itemId: Long,
    val cost: Double,
    val purchaseDate: String,
    val month: Int,
    val year: Int,
)
