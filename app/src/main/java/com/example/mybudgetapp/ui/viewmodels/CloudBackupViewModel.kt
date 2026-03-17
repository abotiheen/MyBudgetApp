package com.example.mybudgetapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetapp.cloud.CloudAuthRepository
import com.example.mybudgetapp.cloud.CloudBackupConfig
import com.example.mybudgetapp.cloud.CloudBackupRepository
import com.example.mybudgetapp.cloud.LocalJsonBackupRepository
import com.example.mybudgetapp.cloud.LocalSpreadsheetExportRepository
import com.example.mybudgetapp.cloud.UserSession
import android.net.Uri
import com.example.mybudgetapp.ui.theme.AppThemeMode
import com.example.mybudgetapp.ui.theme.ThemePreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CloudBackupViewModel(
    private val authRepository: CloudAuthRepository,
    private val backupRepository: CloudBackupRepository,
    private val spreadsheetExportRepository: LocalSpreadsheetExportRepository,
    private val jsonBackupRepository: LocalJsonBackupRepository,
    private val themePreferenceRepository: ThemePreferenceRepository,
) : ViewModel() {
    private val isBusy = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val remoteBackupAt = MutableStateFlow<String?>(null)
    private val lastUploadedAt = MutableStateFlow<String?>(null)
    private val lastRestoredAt = MutableStateFlow<String?>(null)

    private val statusFlow = combine(
        remoteBackupAt,
        lastUploadedAt,
        lastRestoredAt,
    ) { remoteAt: String?, uploadedAt: String?, restoredAt: String? ->
        Triple(remoteAt, uploadedAt, restoredAt)
    }

    val uiState: StateFlow<CloudBackupUiState> = combine(
        authRepository.sessionFlow,
        isBusy,
        statusMessage,
        statusFlow,
        themePreferenceRepository.themeMode,
    ) { session: UserSession?, busy: Boolean, message: String?, status: Triple<String?, String?, String?>, themeMode: AppThemeMode ->
        CloudBackupUiState(
            isConfigured = CloudBackupConfig.isConfigured,
            canDeleteAccount = CloudBackupConfig.canDeleteAccount,
            isSignedIn = session != null,
            email = session?.email.orEmpty(),
            isBusy = busy,
            statusMessage = message,
            remoteBackupAt = status.first,
            lastUploadedAt = status.second,
            lastRestoredAt = status.third,
            themeMode = themeMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = CloudBackupUiState(isConfigured = CloudBackupConfig.isConfigured),
    )

    init {
        refreshStatus()
    }

    fun signIn(email: String, password: String) {
        runCloudTask(task = {
            authRepository.signIn(email.trim(), password)
        })
    }

    fun signUp(email: String, password: String) {
        runCloudTask(task = {
            authRepository.signUp(email.trim(), password)
        })
    }

    fun signOut() {
        runCloudTask(
            task = {
                authRepository.signOut().map { "Signed out." }
            },
            onSuccess = {
                remoteBackupAt.value = null
            }
        )
    }

    fun deleteBackup() {
        runCloudTask(
            task = {
                backupRepository.deleteBackup()
            },
            onSuccess = {
                remoteBackupAt.value = null
                lastUploadedAt.value = null
            }
        )
    }

    fun deleteAccount() {
        runCloudTask(
            task = {
                authRepository.deleteAccount()
            },
            onSuccess = {
                remoteBackupAt.value = null
                lastUploadedAt.value = null
                lastRestoredAt.value = null
            }
        )
    }

    fun uploadBackup() {
        runCloudTask(task = {
            backupRepository.uploadBackup()
        })
    }

    fun restoreBackup() {
        runCloudTask(task = {
            backupRepository.restoreBackup()
        })
    }

    fun exportSpreadsheet() {
        viewModelScope.launch {
            isBusy.value = true
            statusMessage.value = null
            spreadsheetExportRepository.exportSpreadsheet()
                .onSuccess { message ->
                    statusMessage.value = message
                }
                .onFailure { error ->
                    statusMessage.value = error.message
                }
            isBusy.value = false
        }
    }

    fun exportJsonBackup() {
        viewModelScope.launch {
            isBusy.value = true
            statusMessage.value = null
            jsonBackupRepository.exportJsonBackup()
                .onSuccess { message ->
                    statusMessage.value = message
                }
                .onFailure { error ->
                    statusMessage.value = error.message
                }
            isBusy.value = false
        }
    }

    fun restoreJsonBackup(uri: Uri) {
        viewModelScope.launch {
            isBusy.value = true
            statusMessage.value = null
            jsonBackupRepository.restoreJsonBackup(uri)
                .onSuccess { message ->
                    statusMessage.value = message
                }
                .onFailure { error ->
                    statusMessage.value = error.message
                }
            isBusy.value = false
        }
    }

    fun selectDarkMode(isDark: Boolean) {
        themePreferenceRepository.setThemeMode(
            if (isDark) AppThemeMode.Dark else AppThemeMode.Light
        )
    }

    fun refreshStatus() {
        if (!CloudBackupConfig.isConfigured) {
            return
        }
        viewModelScope.launch {
            backupRepository.getBackupStatus()
                .onSuccess { status ->
                    remoteBackupAt.value = status.remoteBackupAt
                    lastUploadedAt.value = status.lastUploadedAt
                    lastRestoredAt.value = status.lastRestoredAt
                }
                .onFailure { error ->
                    statusMessage.value = error.message
                }
        }
    }

    private fun runCloudTask(
        task: suspend () -> Result<String>,
        onSuccess: (() -> Unit)? = null,
    ) {
        viewModelScope.launch {
            isBusy.value = true
            statusMessage.value = null
            task()
                .onSuccess { message ->
                    statusMessage.value = message
                    onSuccess?.invoke()
                    refreshStatus()
                }
                .onFailure { error ->
                    statusMessage.value = error.message
                    refreshStatus()
                }
            isBusy.value = false
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class CloudBackupUiState(
    val isConfigured: Boolean = false,
    val canDeleteAccount: Boolean = false,
    val isSignedIn: Boolean = false,
    val email: String = "",
    val isBusy: Boolean = false,
    val statusMessage: String? = null,
    val remoteBackupAt: String? = null,
    val lastUploadedAt: String? = null,
    val lastRestoredAt: String? = null,
    val themeMode: AppThemeMode = AppThemeMode.System,
)
