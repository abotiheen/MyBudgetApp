package com.example.mybudgetapp.database

import android.content.Context
import com.example.mybudgetapp.cloud.CloudAuthRepository
import com.example.mybudgetapp.cloud.CloudBackupLocalDataSource
import com.example.mybudgetapp.cloud.CloudBackupRepository
import com.example.mybudgetapp.cloud.CloudSessionStore
import com.example.mybudgetapp.cloud.DeviceJsonBackupRepository
import com.example.mybudgetapp.cloud.ExcelCompatibleSpreadsheetExportRepository
import com.example.mybudgetapp.cloud.LocalJsonBackupRepository
import com.example.mybudgetapp.cloud.LocalSpreadsheetExportRepository
import com.example.mybudgetapp.cloud.SupabaseAuthRepository
import com.example.mybudgetapp.cloud.SupabaseCloudBackupRepository
import com.example.mybudgetapp.ui.theme.SharedPreferencesThemePreferenceRepository
import com.example.mybudgetapp.ui.theme.ThemePreferenceRepository

interface AppContainer {
    val itemRepository: ItemRepository
    val cloudAuthRepository: CloudAuthRepository
    val cloudBackupRepository: CloudBackupRepository
    val localSpreadsheetExportRepository: LocalSpreadsheetExportRepository
    val localJsonBackupRepository: LocalJsonBackupRepository
    val themePreferenceRepository: ThemePreferenceRepository
}

class AppDataContainer(context: Context) : AppContainer {
    private val database: BudgetDatabase by lazy {
        BudgetDatabase.getDatabase(context)
    }

    private val sessionStore: CloudSessionStore by lazy {
        CloudSessionStore(context)
    }

    private val cloudBackupLocalDataSource: CloudBackupLocalDataSource by lazy {
        CloudBackupLocalDataSource(database)
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
            localDataSource = cloudBackupLocalDataSource,
            sessionStore = sessionStore,
        )
    }

    override val localSpreadsheetExportRepository: LocalSpreadsheetExportRepository by lazy {
        ExcelCompatibleSpreadsheetExportRepository(
            context = context,
            localDataSource = cloudBackupLocalDataSource,
        )
    }

    override val localJsonBackupRepository: LocalJsonBackupRepository by lazy {
        DeviceJsonBackupRepository(
            context = context,
            localDataSource = cloudBackupLocalDataSource,
        )
    }

    override val themePreferenceRepository: ThemePreferenceRepository by lazy {
        SharedPreferencesThemePreferenceRepository(context)
    }
}
