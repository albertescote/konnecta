package com.konnecta.app.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.onesignal.OneSignal

class AuthService {
    private val client = SupabaseClient.client
    private val auth = client.auth

    init {
        // Link user to OneSignal whenever session changes
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

    suspend fun deleteAccount() {
        auth.signOut()
    }
}

