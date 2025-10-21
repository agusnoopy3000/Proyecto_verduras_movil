package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.dal.UserDao
import com.example.app_verduras.Model.User
import com.example.app_verduras.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 1. SE DEFINE EL ESTADO DE LA UI
sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}

class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    // 2. SE CREA EL STATEFLOW PARA EXPONER EL ESTADO
    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState = _uiState.asStateFlow()

    // 3. SE SIMPLIFICA LA FUNCIÓN DE LOGIN
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            if (email.isBlank() || password.isBlank()) {
                _uiState.value = AuthState.Error("El email y la contraseña no pueden estar vacíos.")
                return@launch
            }

            val user = withContext(Dispatchers.IO) { userDao.getUserByEmail(email) }

            if (user == null || user.password != password) {
                _uiState.value = AuthState.Error("Email o contraseña incorrectos.")
            } else {
                SessionManager.login(user)
                _uiState.value = AuthState.Authenticated
            }
        }
    }

    // 4. SE MODIFICA LA FUNCIÓN DE REGISTRO PARA ACEPTAR MÁS CAMPOS
    fun register(nombre: String, apellido: String, email: String, password: String, direccion: String, telefono: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading

            // --- INICIO DE VALIDACIONES ---

            // Validación de campos obligatorios
            if (nombre.isBlank() || apellido.isBlank() || email.isBlank() || password.isBlank() || direccion.isBlank() || telefono.isBlank()) {
                _uiState.value = AuthState.Error("Todos los campos son obligatorios.")
                return@launch
            }

            // Validación de dominio de correo
            if (!email.endsWith("@gmail.com") && !email.endsWith("@duocuc.cl")) {
                _uiState.value = AuthState.Error("El correo debe ser @gmail.com o @duocuc.cl.")
                return@launch
            }

            // Validación de contraseña
            val specialCharRegex = Regex("[^A-Za-z0-9]")
            if (password.length < 6 || !specialCharRegex.containsMatchIn(password)) {
                _uiState.value = AuthState.Error("La contraseña debe tener mínimo 6 caracteres y un carácter especial.")
                return@launch
            }

            // Validación de teléfono
            if (!telefono.all { it.isDigit() }) {
                _uiState.value = AuthState.Error("El teléfono solo puede contener números.")
                return@launch
            }

            // Validación de email existente
            val existingUser = withContext(Dispatchers.IO) { userDao.getUserByEmail(email) }
            if (existingUser != null) {
                _uiState.value = AuthState.Error("El email ya está registrado.")
                return@launch
            }

            // --- FIN DE VALIDACIONES ---

            val newUser = User(
                email = email,
                nombre = nombre,
                apellido = apellido,
                password = password, // En una app real, la contraseña debería ser hasheada aquí
                direccion = direccion,
                telefono = telefono
            )
            userDao.insert(newUser)
            SessionManager.login(newUser)
            _uiState.value = AuthState.Authenticated
        }
    }

    fun logout() {
        viewModelScope.launch {
            SessionManager.logout()
            _uiState.value = AuthState.Idle
        }
    }

    // Función para resetear el estado si el usuario navega hacia atrás
    fun resetState() {
        _uiState.value = AuthState.Idle
    }


    class Factory(private val userDao: UserDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(userDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
