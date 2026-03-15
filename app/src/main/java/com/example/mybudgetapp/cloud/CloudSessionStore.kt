package com.example.mybudgetapp.cloud

import android.content.Context

class CloudSessionStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSession(): UserSession? {
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        val userId = prefs.getString(KEY_USER_ID, null)
        val email = prefs.getString(KEY_EMAIL, null)
        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() || userId.isNullOrBlank() || email.isNullOrBlank()) {
            return null
        }
        return UserSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            email = email,
        )
    }

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .apply()
    }

    fun loadBackupStatus(): CloudBackupStatus = CloudBackupStatus(
        lastUploadedAt = prefs.getString(KEY_LAST_UPLOADED_AT, null),
        lastRestoredAt = prefs.getString(KEY_LAST_RESTORED_AT, null),
    )

    fun saveLastUploadedAt(timestamp: String) {
        prefs.edit().putString(KEY_LAST_UPLOADED_AT, timestamp).apply()
    }

    fun saveLastRestoredAt(timestamp: String) {
        prefs.edit().putString(KEY_LAST_RESTORED_AT, timestamp).apply()
    }

    fun clearBackupStatus() {
        prefs.edit()
            .remove(KEY_LAST_UPLOADED_AT)
            .remove(KEY_LAST_RESTORED_AT)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "cloud_backup_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_LAST_UPLOADED_AT = "last_uploaded_at"
        private const val KEY_LAST_RESTORED_AT = "last_restored_at"
    }
}
