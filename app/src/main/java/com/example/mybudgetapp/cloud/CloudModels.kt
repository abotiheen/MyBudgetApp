package com.example.mybudgetapp.cloud

import com.example.mybudgetapp.BuildConfig
import com.example.mybudgetapp.database.Item
import com.example.mybudgetapp.database.PurchaseDetails
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object CloudBackupConfig {
    val baseUrl: String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()
    val deleteAccountUrl: String = BuildConfig.SUPABASE_DELETE_ACCOUNT_URL.trim()
    val isConfigured: Boolean = baseUrl.isNotBlank() && anonKey.isNotBlank()
    val canDeleteAccount: Boolean = deleteAccountUrl.isNotBlank()
}

@Serializable
data class AuthUserDto(
    val id: String,
    val email: String? = null,
)

@Serializable
data class AuthResponseDto(
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    val user: AuthUserDto? = null,
)

@Serializable
data class AuthCredentialsRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String,
)

@Serializable
data class AuthErrorDto(
    val message: String? = null,
    val msg: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null,
)

@Serializable
data class BackupSnapshot(
    val exportedAt: String,
    val appVersion: Int,
    val items: List<Item>,
    val purchases: List<PurchaseDetails>,
)

@Serializable
data class BackupRecordDto(
    @SerialName("user_id")
    val userId: String,
    val data: BackupSnapshot,
    @SerialName("updated_at")
    val updatedAt: String,
)

data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
)

data class CloudBackupStatus(
    val remoteBackupAt: String? = null,
    val lastUploadedAt: String? = null,
    val lastRestoredAt: String? = null,
)

class UnauthorizedCloudException : IllegalStateException("The saved Supabase session is no longer valid.")
