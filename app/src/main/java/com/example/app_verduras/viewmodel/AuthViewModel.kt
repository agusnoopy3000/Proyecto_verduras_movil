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

    // 4. SE SIMPLIFICA LA FUNCIÓN DE REGISTRO
    fun register(nombre: String, apellido: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading

            // Validaciones básicas
            if (nombre.isBlank() || apellido.isBlank() || email.isBlank() || password.isBlank()) {
                _uiState.value = AuthState.Error("Todos los campos son obligatorios.")
                return@launch
            }
            val existingUser = withContext(Dispatchers.IO) { userDao.getUserByEmail(email) }
            if (existingUser != null) {
                _uiState.value = AuthState.Error("El email ya está registrado.")
                return@launch
            }

            val newUser = User(
                email = email,
                nombre = nombre,
                apellido = apellido,
                password = password
            )
            userDao.insert(newUser)
            SessionManager.login(newUser)
            _uiState.value = AuthState.Authenticated
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
