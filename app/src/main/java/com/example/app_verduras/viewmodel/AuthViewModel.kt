package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.dal.UserDao
import com.example.app_verduras.Model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    // CORRECT WAY: Expose state using a StateFlow
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    fun onUserMessageShown() {
        _state.update { it.copy(userMessage = null) }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, userMessage = null) }

            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()

            if (trimmedEmail.isBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "El email no puede estar vacío.") }
                return@launch
            }
            if (trimmedPassword.isBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "La contraseña no puede estar vacía.") }
                return@launch
            }

            // This is a simplified example. In a real app, passwords should be hashed.
            if (trimmedEmail.equals("admin@test.com", ignoreCase = true)) {
                if (trimmedPassword == "admin123!") {
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            user = User(
                                email = "admin@test.com",
                                nombre = "Admin",
                                apellido = "User",
                                password = "admin123!", // Hashed in a real app
                                direccion = "N/A",
                                telefono = "N/A"
                            )
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, userMessage = "Contraseña de administrador incorrecta.") }
                }
            } else {
                val user = userDao.getUserByEmail(trimmedEmail)
                if (user != null && user.password == trimmedPassword) {
                    _state.update { it.copy(isLoading = false, isAuthenticated = true, user = user) }
                } else {
                    _state.update { it.copy(isLoading = false, userMessage = "Email o contraseña incorrectos.") }
                }
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, userMessage = null) }

            val trimmedEmail = user.email.trim()
            val trimmedPassword = user.password
            val trimmedNombre = user.nombre.trim()
            val trimmedApellido = user.apellido.trim()
            val trimmedDireccion = user.direccion?.trim()
            val trimmedTelefono = user.telefono?.trim()

            if (trimmedNombre.isBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "El nombre no puede estar vacío.") }
                return@launch
            }
            if (trimmedApellido.isBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "El apellido no puede estar vacío.") }
                return@launch
            }
            if (trimmedEmail.isBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "El email no puede estar vacío.") }
                return@launch
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                _state.update { it.copy(isLoading = false, userMessage = "El formato del email no es válido.") }
                return@launch
            }
            val allowedDomains = listOf("@duocuc.cl", "@gmail.com", "@profesorduoc.cl")
            if (allowedDomains.none { trimmedEmail.endsWith(it, ignoreCase = true) }) {
                _state.update { it.copy(isLoading = false, userMessage = "Solo se permiten dominios @duocuc.cl, @gmail.com o @profesorduoc.cl.") }
                return@launch
            }
            if (userDao.getUserByEmail(trimmedEmail) != null) {
                _state.update { it.copy(isLoading = false, userMessage = "El email ya está registrado.") }
                return@launch
            }
            if (trimmedPassword.length < 6) {
                _state.update { it.copy(isLoading = false, userMessage = "La contraseña debe tener al menos 6 caracteres.") }
                return@launch
            }
            if (!trimmedPassword.any { it.isDigit() }) {
                _state.update { it.copy(isLoading = false, userMessage = "La contraseña debe contener al menos un número.") }
                return@launch
            }
            if (!trimmedPassword.any { it in "!@#$%%^&*()_+-=[]{}|;':,.<>?/~" }) {
                _state.update { it.copy(isLoading = false, userMessage = "La contraseña debe contener al menos un carácter especial.") }
                return@launch
            }
            if (trimmedDireccion.isNullOrBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "La dirección no puede estar vacía.") }
                return@launch
            }
            if (trimmedTelefono.isNullOrBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "El teléfono no puede estar vacío.") }
                return@launch
            }

            val finalUser = user.copy(
                nombre = trimmedNombre,
                apellido = trimmedApellido,
                email = trimmedEmail,
                password = trimmedPassword, // Remember to hash passwords in a real app
                direccion = trimmedDireccion,
                telefono = trimmedTelefono
            )
            userDao.insert(finalUser)
            _state.update { it.copy(isLoading = false, isAuthenticated = true, user = finalUser) }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, userMessage = null) }
            val trimmedEmail = email.trim()
            if (trimmedEmail.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                // In a real app, you would trigger a password reset flow here (e.g., Firebase Auth, backend API)
                _state.update { it.copy(isLoading = false, userMessage = "Si el email está registrado, recibirás un correo.") }
            } else {
                _state.update { it.copy(isLoading = false, userMessage = "El formato del email no es válido.") }
            }
        }
    }

    fun resetAuthState() {
        _state.value = AuthState()
    }

    fun logout() {
        _state.value = AuthState()
    }

    class Factory(private val userDao: UserDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(userDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val userMessage: String? = null
)
