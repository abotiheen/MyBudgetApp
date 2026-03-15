package com.example.mybudgetapp.cloud

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object SupabaseHttpClient {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
    }

    val client: HttpClient = HttpClient(Android) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
    }
}
