package com.tuempresa.gastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.tuempresa.gastos.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    fun signInEmail(email: String, pass: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        repo.signInWithEmail(email.trim(), pass) { r: Result<Unit> ->
            _state.value = r.fold(
                onSuccess = { _state.value.copy(loading = false, isLogged = true) },
                onFailure = { _state.value.copy(loading = false, error = it.localizedMessage ?: "Error al iniciar sesión") }
            )
        }
    }

    fun signUpEmail(email: String, pass: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        repo.registerWithEmail(email.trim(), pass) { r: Result<Unit> ->
            _state.value = r.fold(
                onSuccess = { _state.value.copy(loading = false, isLogged = true) },
                onFailure = { _state.value.copy(loading = false, error = it.localizedMessage ?: "Error al registrarte") }
            )
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        repo.firebaseAuthWithGoogle(idToken) { r: Result<Unit> ->
            _state.value = r.fold(
                onSuccess = { _state.value.copy(loading = false, isLogged = true) },
                onFailure = { _state.value.copy(loading = false, error = it.localizedMessage ?: "Error con Google") }
            )
        }
    }

    fun signOut() {
        repo.signOut()
        _state.value = AuthUiState()
    }
}
