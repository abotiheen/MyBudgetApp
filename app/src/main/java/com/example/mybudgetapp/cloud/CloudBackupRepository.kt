package com.example.mybudgetapp.cloud

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.time.Instant

interface CloudBackupRepository {
    suspend fun getBackupStatus(): Result<CloudBackupStatus>
    suspend fun uploadBackup(): Result<String>
    suspend fun restoreBackup(): Result<String>
    suspend fun deleteBackup(): Result<String>
}

class SupabaseCloudBackupRepository(
    private val authRepository: CloudAuthRepository,
    private val localDataSource: CloudBackupLocalDataSource,
    private val sessionStore: CloudSessionStore,
    private val httpClient: HttpClient = SupabaseHttpClient.client,
) : CloudBackupRepository {

    override suspend fun getBackupStatus(): Result<CloudBackupStatus> {
        if (!CloudBackupConfig.isConfigured) {
            return Result.success(sessionStore.loadBackupStatus())
        }
        authRepository.getCurrentSession().getOrElse {
            return Result.success(sessionStore.loadBackupStatus())
        }
        val remote = fetchBackupRecord().getOrElse { error ->
            return Result.failure(error)
        }
        return Result.success(
            sessionStore.loadBackupStatus().copy(
                remoteBackupAt = remote?.updatedAt,
            )
        )
    }

    override suspend fun uploadBackup(): Result<String> {
        if (!CloudBackupConfig.isConfigured) {
            return Result.failure(IllegalStateException("Supabase keys are missing from local.properties."))
        }
        val snapshot = localDataSource.exportSnapshot()
        val record = withAuthorizedSession { session ->
            val payload = listOf(
                BackupRecordDto(
                    userId = session.userId,
                    data = snapshot,
                    updatedAt = snapshot.exportedAt,
                )
            )
            val response = httpClient.post("${CloudBackupConfig.baseUrl}/rest/v1/user_backups") {
                header("apikey", CloudBackupConfig.anonKey)
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                header("Prefer", "resolution=merge-duplicates,return=representation")
                contentType(ContentType.Application.Json)
                parameter("on_conflict", "user_id")
                parameter("select", "user_id,updated_at,data")
                setBody(payload)
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                return@withAuthorizedSession Result.failure(UnauthorizedCloudException())
            }
            if (!response.status.isSuccess()) {
                return@withAuthorizedSession Result.failure(response.toCloudException())
            }
            val records = response.body<List<BackupRecordDto>>()
            Result.success(records.firstOrNull())
        }.getOrElse { error ->
            return Result.failure(error)
        }
        val uploadedAt = record?.updatedAt ?: snapshot.exportedAt
        sessionStore.saveLastUploadedAt(uploadedAt)
        return Result.success("Cloud backup uploaded successfully.")
    }

    override suspend fun restoreBackup(): Result<String> {
        if (!CloudBackupConfig.isConfigured) {
            return Result.failure(IllegalStateException("Supabase keys are missing from local.properties."))
        }
        val record = fetchBackupRecord().getOrElse { error ->
            return Result.failure(error)
        } ?: return Result.failure(IllegalStateException("No cloud backup exists for this account yet."))

        localDataSource.importSnapshot(record.data)
        sessionStore.saveLastRestoredAt(Instant.now().toString())
        return Result.success("Cloud backup restored to this device.")
    }

    override suspend fun deleteBackup(): Result<String> {
        if (!CloudBackupConfig.isConfigured) {
            return Result.failure(IllegalStateException("Supabase keys are missing from local.properties."))
        }
        withAuthorizedSession { session ->
            val response = httpClient.delete("${CloudBackupConfig.baseUrl}/rest/v1/user_backups") {
                header("apikey", CloudBackupConfig.anonKey)
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                parameter("user_id", "eq.${session.userId}")
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                return@withAuthorizedSession Result.failure(UnauthorizedCloudException())
            }
            if (!response.status.isSuccess()) {
                return@withAuthorizedSession Result.failure(response.toCloudException())
            }
            Result.success(Unit)
        }.getOrElse { error ->
            return Result.failure(error)
        }
        sessionStore.clearBackupStatus()
        return Result.success("Cloud backup deleted.")
    }

    private suspend fun fetchBackupRecord(): Result<BackupRecordDto?> {
        return withAuthorizedSession { session ->
            val response = httpClient.get("${CloudBackupConfig.baseUrl}/rest/v1/user_backups") {
                header("apikey", CloudBackupConfig.anonKey)
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                parameter("select", "user_id,updated_at,data")
                parameter("user_id", "eq.${session.userId}")
                parameter("limit", 1)
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                return@withAuthorizedSession Result.failure(UnauthorizedCloudException())
            }
            if (!response.status.isSuccess()) {
                return@withAuthorizedSession Result.failure(response.toCloudException())
            }
            val records = response.body<List<BackupRecordDto>>()
            Result.success(records.firstOrNull())
        }
    }

    private suspend fun <T> withAuthorizedSession(
        block: suspend (UserSession) -> Result<T>,
    ): Result<T> {
        val firstSession = authRepository.getCurrentSession().getOrElse { error ->
            return Result.failure(error)
        }
        val firstAttempt = block(firstSession)
        if (firstAttempt.exceptionOrNull() !is UnauthorizedCloudException) {
            return firstAttempt
        }
        val refreshedSession = authRepository.getCurrentSession(forceRefresh = true).getOrElse { error ->
            return Result.failure(error)
        }
        return block(refreshedSession)
    }
}
