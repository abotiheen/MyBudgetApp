package com.example.mybudgetapp.database

import android.content.Context
import com.example.mybudgetapp.cloud.CloudAuthRepository
import com.example.mybudgetapp.cloud.CloudBackupLocalDataSource
import com.example.mybudgetapp.cloud.CloudBackupRepository
import com.example.mybudgetapp.cloud.CloudSessionStore
import com.example.mybudgetapp.cloud.SupabaseAuthRepository
import com.example.mybudgetapp.cloud.SupabaseCloudBackupRepository

interface AppContainer {
    val itemRepository: ItemRepository
    val cloudAuthRepository: CloudAuthRepository
    val cloudBackupRepository: CloudBackupRepository
}

class AppDataContainer(context: Context) : AppContainer {
    private val database: BudgetDatabase by lazy {
        BudgetDatabase.getDatabase(context)
    }

    private val sessionStore: CloudSessionStore by lazy {
        CloudSessionStore(context)
    }

    override val itemRepository: ItemRepository by lazy {
        OfflineRepository(
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
        )
    }

    override val cloudAuthRepository: CloudAuthRepository by lazy {
        SupabaseAuthRepository(sessionStore = sessionStore)
    }

    override val cloudBackupRepository: CloudBackupRepository by lazy {
        SupabaseCloudBackupRepository(
            authRepository = cloudAuthRepository,
            localDataSource = CloudBackupLocalDataSource(database),
            sessionStore = sessionStore,
        )
    }
}
