package com.konnecta.app.data.remote

import com.konnecta.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.HttpTimeout
import kotlinx.serialization.json.Json

object SupabaseClient {
    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY

    @OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000L
                connectTimeoutMillis = 10_000L
                socketTimeoutMillis = 15_000L
            }
        }
        install(Auth) {
            scheme = "konnecta"
            host = "login-callback"
        }
        install(ComposeAuth) {
            googleNativeLogin(serverClientId = BuildConfig.GOOGLE_CLIENT_ID)
        }
        install(Postgrest) {
            serializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(Storage)

        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }
}
