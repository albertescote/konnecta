package com.konnecta.app.data.remote

import io.github.jan_tennert.supabase.gotrue.auth
import io.github.jan_tennert.supabase.gotrue.providers.Google
import io.github.jan_tennert.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.onesignal.OneSignal

class AuthService {
    private val client = SupabaseClient.client
    private val auth = client.auth

    init {
        // Link user to OneSignal whenever session changes
        auth.sessionStatus.map { status ->
            if (status is io.github.jan_tennert.supabase.gotrue.SessionStatus.Authenticated) {
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
        auth.signInWith(Google) {
            this.redirectTo = redirectTo
        }
    }

    suspend fun signInWithMagicLink(email: String, redirectTo: String) {
        auth.signInWith(io.github.jan_tennert.supabase.gotrue.providers.builtin.OTP) {
            this.email = email
            this.redirectTo = redirectTo
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun deleteAccount() {
        // In Supabase, deleting a user is typically restricted to the Admin client or a database trigger.
        // For standard client SDKs, we sign out and the user must request deletion 
        // or we call a specific Supabase RPC/Function if configured.
        // For store compliance, provide a clear 'Delete' action that at least clears session 
        // and ideally calls a function to mark for deletion.
        auth.signOut()
    }
}
