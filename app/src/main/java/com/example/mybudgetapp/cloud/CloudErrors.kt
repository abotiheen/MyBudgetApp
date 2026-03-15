package com.example.mybudgetapp.cloud

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

suspend fun HttpResponse.toCloudException(): IllegalStateException {
    return try {
        val body = body<AuthErrorDto>()
        IllegalStateException(body.message ?: body.errorDescription ?: body.msg ?: "Cloud request failed with HTTP ${status.value}.")
    } catch (_: Exception) {
        IllegalStateException("Cloud request failed with HTTP ${status.value}.")
    }
}
