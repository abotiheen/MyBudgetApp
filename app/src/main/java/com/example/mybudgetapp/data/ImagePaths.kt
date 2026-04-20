package com.example.mybudgetapp.data

import android.net.Uri
import java.io.File

fun usableImagePath(path: String?): String? {
    val value = path?.trim().orEmpty()
    if (value.isBlank()) {
        return null
    }
    return when {
        value.startsWith("content://") -> value
        value.startsWith("file://") -> {
            runCatching { File(requireNotNull(Uri.parse(value).path)).takeIf { it.exists() }?.path }.getOrNull()
        }
        else -> File(value).takeIf { it.exists() }?.path
    }
}
