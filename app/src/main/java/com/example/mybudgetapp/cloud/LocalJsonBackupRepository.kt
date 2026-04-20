package com.example.mybudgetapp.cloud

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

interface LocalJsonBackupRepository {
    suspend fun exportJsonBackup(): Result<String>
    suspend fun restoreJsonBackup(uri: Uri): Result<String>
}

class DeviceJsonBackupRepository(
    private val context: Context,
    private val localDataSource: CloudBackupLocalDataSource,
) : LocalJsonBackupRepository {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override suspend fun exportJsonBackup(): Result<String> = runCatching {
        val snapshot = localDataSource.exportSnapshot()
        val fileName = buildFileName(snapshot.exportedAt)
        val payload = json.encodeToString(BackupSnapshot.serializer(), snapshot)
        saveToDownloads(fileName, payload.toByteArray(Charsets.UTF_8))
        "Offline backup saved to Downloads as $fileName."
    }

    override suspend fun restoreJsonBackup(uri: Uri): Result<String> = runCatching {
        val contents = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Unable to open the selected backup file.")
        val snapshot = json.decodeFromString(BackupSnapshot.serializer(), contents)
        localDataSource.importSnapshot(snapshot)
        "Offline backup restored from JSON."
    }

    private fun buildFileName(exportedAt: String): String {
        val formatted = runCatching {
            val instant = Instant.parse(exportedAt)
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US)
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }.getOrElse {
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US)
                .withZone(ZoneId.systemDefault())
                .format(Instant.now())
        }
        return "mybudget-backup-$formatted.json"
    }

    private fun saveToDownloads(fileName: String, bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create backup file in Downloads.")
            resolver.openOutputStream(uri)?.use { output ->
                output.write(bytes)
            } ?: error("Unable to open backup output stream.")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return
        }

        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDirectory.exists()) {
            downloadsDirectory.mkdirs()
        }
        val file = File(downloadsDirectory, fileName)
        file.writeBytes(bytes)
    }
}
