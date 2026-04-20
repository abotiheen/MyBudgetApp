package com.example.mybudgetapp.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.cloud.LocalJsonBackupRepository
import com.example.mybudgetapp.cloud.LocalSpreadsheetExportRepository
import com.example.mybudgetapp.ui.theme.AppThemeMode
import com.example.mybudgetapp.ui.theme.ThemePreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CloudBackupViewModel(
    private val spreadsheetExportRepository: LocalSpreadsheetExportRepository,
    private val jsonBackupRepository: LocalJsonBackupRepository,
    private val themePreferenceRepository: ThemePreferenceRepository,
) : ViewModel() {
    private val isBusy = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CloudBackupUiState> = combine(
        isBusy,
        statusMessage,
        themePreferenceRepository.themeMode,
    ) { busy: Boolean, message: String?, themeMode: AppThemeMode ->
        CloudBackupUiState(
            isBusy = busy,
            statusMessage = message,
            themeMode = themeMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = CloudBackupUiState(),
    )

    fun exportSpreadsheet() {
        runBackupTask {
            spreadsheetExportRepository.exportSpreadsheet()
        }
    }

    fun exportJsonBackup() {
        runBackupTask {
            jsonBackupRepository.exportJsonBackup()
        }
    }

    fun restoreJsonBackup(uri: Uri) {
        runBackupTask {
            jsonBackupRepository.restoreJsonBackup(uri)
        }
    }

    fun selectDarkMode(isDark: Boolean) {
        themePreferenceRepository.setThemeMode(
            if (isDark) AppThemeMode.Dark else AppThemeMode.Light
        )
    }

    fun refreshStatus() = Unit

    private fun runBackupTask(
        task: suspend () -> Result<String>,
    ) {
        viewModelScope.launch {
            isBusy.value = true
            statusMessage.value = null
            task()
                .onSuccess { message ->
                    statusMessage.value = message
                }
                .onFailure { error ->
                    statusMessage.value = error.message
                }
            isBusy.value = false
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class CloudBackupUiState(
    val isBusy: Boolean = false,
    val statusMessage: String? = null,
    val themeMode: AppThemeMode = AppThemeMode.System,
)
