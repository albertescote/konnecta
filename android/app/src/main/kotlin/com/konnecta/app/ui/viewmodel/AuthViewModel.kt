package com.konnecta.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konnecta.app.data.remote.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val sessionStatus: SessionStatus? = null,
    val user: UserInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        viewModelScope.launch {
            authService.sessionStatus.collect { status ->
                _state.value = _state.value.copy(
                    sessionStatus = status,
                    user = if (status is SessionStatus.Authenticated) status.session.user else null
                )
            }
        }
    }

    fun signInWithGoogle(redirectTo: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                authService.signInWithGoogle(redirectTo)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun signInWithMagicLink(email: String, redirectTo: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                authService.signInWithMagicLink(email, redirectTo)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
        }
    }

    fun updateProfile(fullName: String, avatarUrl: String?, onResult: (Boolean) -> Unit = {}) {
        val user = _state.value.user ?: return
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val success = authService.updateProfile(user.id, fullName, avatarUrl)
                onResult(success)
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                onResult(false)
            }
        }
    }

    fun uploadAvatar(bytes: ByteArray, fileName: String, onResult: (String?) -> Unit) {
        val user = _state.value.user ?: return
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val url = authService.uploadAvatar(user.id, bytes, fileName)
                onResult(url)
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                onResult(null)
            }
        }
    }
}
