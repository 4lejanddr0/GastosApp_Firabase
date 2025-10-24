package com.tuempresa.gastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuempresa.gastos.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val isLogged: Boolean = false
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState(isLogged = repo.isLoggedIn()))
    val state: StateFlow<AuthUiState> = _state

    // ---- Email Sign-In (callback) ----
    fun signInEmail(email: String, pass: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        repo.signInWithEmail(email.trim(), pass) { r ->
            _state.value = r.fold(
                onSuccess = { _state.value.copy(loading = false, isLogged = true) },
                onFailure = {
                    _state.value.copy(
                        loading = false,
                        error = it.localizedMessage ?: "Error al iniciar sesión"
                    )
                }
            )
        }
    }

    // ---- Email Sign-Up (callback) — sin nombre visible (compatibilidad) ----
    fun signUpEmail(email: String, pass: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        // Usa la SOBRECARGA con callback que recibe displayName:
        repo.registerWithEmail(email.trim(), pass, displayName = "") { r ->
            _state.value = r.fold(
                onSuccess = { _state.value.copy(loading = false, isLogged = true) },
                onFailure = {
                    _state.value.copy(
                        loading = false,
                        error = it.localizedMessage ?: "Error al registrarte"
                    )
                }
            )
        }
    }

    // ---- Email Sign-Up con nombre (usa la versión suspend) ----
    fun signUpWithName(email: String, pass: String, displayName: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                repo.registerWithEmail(email.trim(), pass, displayName.trim())
            }.onSuccess {
                _state.value = _state.value.copy(loading = false, isLogged = true)
            }.onFailure {
                _state.value = _state.value.copy(
                    loading = false,
                    error = it.localizedMessage ?: "Error al registrarte"
                )
            }
        }
    }

    // ---- Google Sign-In (callback) ----
    fun signInWithGoogleIdToken(idToken: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        repo.firebaseAuthWithGoogle(idToken) { r ->
            _state.value = r.fold(
                onSuccess = { _state.value.copy(loading = false, isLogged = true) },
                onFailure = {
                    _state.value.copy(
                        loading = false,
                        error = it.localizedMessage ?: "Error con Google"
                    )
                }
            )
        }
    }

    fun signOut() {
        repo.signOut()
        _state.value = AuthUiState()
    }
}
