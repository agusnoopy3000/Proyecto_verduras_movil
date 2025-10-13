package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado para la autenticación
data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            // TODO: Reemplazar con lógica de autenticación real (ej. API, Firebase, etc.)
            // Simulación de una llamada de red
            kotlinx.coroutines.delay(1000)

            if (username.isNotEmpty() && password.isNotEmpty()) {
                _authState.value = AuthState(isAuthenticated = true)
            } else {
                _authState.value = AuthState(error = "Usuario o contraseña inválidos")
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            // TODO: Reemplazar con lógica de registro real
            kotlinx.coroutines.delay(1000)

            if (username.isNotEmpty() && password.isNotEmpty()) {
                _authState.value = AuthState(isAuthenticated = true) // Auto-login after register
            } else {
                _authState.value = AuthState(error = "Datos de registro inválidos")
            }
        }
    }

    fun logout() {
        _authState.value = AuthState()
    }
}
