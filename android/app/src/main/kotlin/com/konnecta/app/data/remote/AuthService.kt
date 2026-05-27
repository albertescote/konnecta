package com.konnecta.app.data.remote

import com.onesignal.OneSignal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor() {
    private val client = SupabaseClient.client
    private val auth = client.auth

    init {
        auth.sessionStatus.map { status ->
            if (status is SessionStatus.Authenticated) {
                OneSignal.login(status.session.user?.id ?: "")
            } else {
                OneSignal.logout()
            }
        }
    }

    val currentUser: UserInfo?
        get() = auth.currentUserOrNull()

    val sessionStatus = auth.sessionStatus

    suspend fun signInWithGoogle(redirectTo: String) {
        auth.signInWith(Google)
    }

    suspend fun signInWithMagicLink(email: String, redirectTo: String) {
        auth.signInWith(OTP) {
            this.email = email
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun updateProfile(userId: String, fullName: String, avatarUrl: String?): Boolean {
        return try {
            client.postgrest["profiles"].update(
                mapOf(
                    "full_name" to fullName,
                    "avatar_url" to avatarUrl,
                    "updated_at" to Instant.now().toString()
                )
            ) {
                filter {
                    eq("id", userId)
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "AuthService: Error updating profile")
            false
        }
    }

    suspend fun uploadAvatar(userId: String, bytes: ByteArray, fileName: String): String? {
        return try {
            val bucket = client.storage["avatars"]
            val fileExt = fileName.split(".").last()
            val filePath = "$userId/${UUID.randomUUID()}.$fileExt"
            bucket.upload(filePath, bytes) {
                upsert = true
            }
            bucket.publicUrl(filePath)
        } catch (e: Exception) {
            Timber.e(e, "AuthService: Error uploading avatar")
            null
        }
    }

    suspend fun deleteAccount() {
        auth.signOut()
    }
}
