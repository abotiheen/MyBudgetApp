package com.example.mybudgetapp.cloud

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CloudAuthRepository {
    val sessionFlow: StateFlow<UserSession?>

    suspend fun signUp(email: String, password: String): Result<String>
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signOut(): Result<Unit>
    suspend fun deleteAccount(): Result<String>
    suspend fun getCurrentSession(forceRefresh: Boolean = false): Result<UserSession>
}

class SupabaseAuthRepository(
    private val sessionStore: CloudSessionStore,
    private val httpClient: HttpClient = SupabaseHttpClient.client,
) : CloudAuthRepository {
    private val mutableSessionFlow = MutableStateFlow(sessionStore.loadSession())
    override val sessionFlow: StateFlow<UserSession?> = mutableSessionFlow.asStateFlow()

    override suspend fun signUp(email: String, password: String): Result<String> {
        if (!CloudBackupConfig.isConfigured) {
            return Result.failure(IllegalStateException("Supabase keys are missing from local.properties."))
        }
        val response = httpClient.post("${CloudBackupConfig.baseUrl}/auth/v1/signup") {
            header("apikey", CloudBackupConfig.anonKey)
            contentType(ContentType.Application.Json)
            setBody(AuthCredentialsRequest(email = email, password = password))
        }
        if (!response.status.isSuccess()) {
            return Result.failure(response.toCloudException())
        }
        val body = response.body<AuthResponseDto>()
        val session = body.toUserSessionOrNull()
        if (session != null) {
            persistSession(session)
            return Result.success("Account created and signed in.")
        }
        return Result.success(
            "Account created. If email confirmation is enabled in Supabase, confirm the email before signing in."
        )
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        if (!CloudBackupConfig.isConfigured) {
            return Result.failure(IllegalStateException("Supabase keys are missing from local.properties."))
        }
        val response = httpClient.post("${CloudBackupConfig.baseUrl}/auth/v1/token?grant_type=password") {
            header("apikey", CloudBackupConfig.anonKey)
            contentType(ContentType.Application.Json)
            setBody(AuthCredentialsRequest(email = email, password = password))
        }
        if (!response.status.isSuccess()) {
            return Result.failure(response.toCloudException())
        }
        val body = response.body<AuthResponseDto>()
        val session = body.toUserSessionOrNull()
            ?: return Result.failure(IllegalStateException("Supabase did not return a valid session."))
        persistSession(session)
        return Result.success("Signed in successfully.")
    }

    override suspend fun signOut(): Result<Unit> {
        val session = mutableSessionFlow.value
        if (session != null && CloudBackupConfig.isConfigured) {
            httpClient.post("${CloudBackupConfig.baseUrl}/auth/v1/logout") {
                header("apikey", CloudBackupConfig.anonKey)
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                contentType(ContentType.Application.Json)
            }
        }
        clearSession()
        return Result.success(Unit)
    }

    override suspend fun deleteAccount(): Result<String> {
        val session = mutableSessionFlow.value
            ?: return Result.failure(IllegalStateException("Sign in to delete the account."))
        if (!CloudBackupConfig.canDeleteAccount) {
            return Result.failure(
                IllegalStateException(
                    "Account deletion requires a secure server endpoint. Set SUPABASE_DELETE_ACCOUNT_URL after deploying the provided Supabase Edge Function."
                )
            )
        }
        val response = httpClient.post(CloudBackupConfig.deleteAccountUrl) {
            header("apikey", CloudBackupConfig.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
            contentType(ContentType.Application.Json)
        }
        if (!response.status.isSuccess()) {
            return Result.failure(response.toCloudException())
        }
        clearSession()
        return Result.success("Cloud account deleted.")
    }

    override suspend fun getCurrentSession(forceRefresh: Boolean): Result<UserSession> {
        val currentSession = mutableSessionFlow.value
            ?: return Result.failure(IllegalStateException("Sign in to use cloud backup."))
        if (!forceRefresh) {
            return Result.success(currentSession)
        }
        if (!CloudBackupConfig.isConfigured) {
            return Result.failure(IllegalStateException("Supabase keys are missing from local.properties."))
        }
        val response = httpClient.post("${CloudBackupConfig.baseUrl}/auth/v1/token?grant_type=refresh_token") {
            header("apikey", CloudBackupConfig.anonKey)
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(refreshToken = currentSession.refreshToken))
        }
        if (!response.status.isSuccess()) {
            clearSession()
            return Result.failure(response.toCloudException())
        }
        val body = response.body<AuthResponseDto>()
        val refreshedSession = body.toUserSessionOrNull(currentSession.email, currentSession.userId)
            ?: return Result.failure(IllegalStateException("Supabase did not return a refreshed session."))
        persistSession(refreshedSession)
        return Result.success(refreshedSession)
    }

    private fun persistSession(session: UserSession) {
        sessionStore.saveSession(session)
        mutableSessionFlow.value = session
    }

    private fun clearSession() {
        sessionStore.clearSession()
        mutableSessionFlow.value = null
    }
}

private fun AuthResponseDto.toUserSessionOrNull(
    fallbackEmail: String? = null,
    fallbackUserId: String? = null,
): UserSession? {
    val token = accessToken ?: return null
    val refresh = refreshToken ?: return null
    val userId = user?.id ?: fallbackUserId ?: return null
    val email = user?.email ?: fallbackEmail ?: return null
    return UserSession(
        accessToken = token,
        refreshToken = refresh,
        userId = userId,
        email = email,
    )
}
