package com.example.app_verduras.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.auth.HybridAuthRepository
import com.example.app_verduras.dal.UserDao
import com.example.app_verduras.Model.User
import com.example.app_verduras.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para autenticación híbrida Firebase + Spring Boot.
 * 
 * FLUJO DE AUTENTICACIÓN:
 * 1. Usuario ingresa credenciales
 * 2. Se autentica con Firebase (si está habilitado)
 * 3. Se sincroniza con el backend de Spring Boot
 * 4. El JWT del backend se guarda para futuras llamadas
 * 
 * La app usa el JWT del BACKEND para todas las llamadas a la API,
 * Firebase solo se usa para validar la identidad inicial.
 */
class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    // Flag para habilitar/deshabilitar Firebase
    // Poner en false si el backend no tiene el endpoint firebase-sync implementado
    private val useFirebaseAuth = true

    fun onUserMessageShown() {
        _state.update { it.copy(userMessage = null) }
    }

    /**
     * Login usando autenticación híbrida (Firebase + Backend)
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, userMessage = null) }

            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()

            // Validaciones básicas
            if (trimmedEmail.isBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "El email no puede estar vacío.") }
                return@launch
            }
            if (trimmedPassword.isBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "La contraseña no puede estar vacía.") }
                return@launch
            }

            Log.d("AuthViewModel", "Iniciando login híbrido para: $trimmedEmail")

            val result = HybridAuthRepository.login(
                email = trimmedEmail, 
                password = trimmedPassword,
                useFirebase = useFirebaseAuth
            )

            when (result) {
                is HybridAuthRepository.AuthResult.Success -> {
                    val authResponse = result.authResponse
                    
                    // Guardar sesión con el token JWT del BACKEND
                    SessionManager.loginWithAuthResponse(authResponse)
                    
                    // Crear usuario local para compatibilidad
                    val user = User(
                        email = authResponse.user.email,
                        nombre = authResponse.user.nombre,
                        apellido = authResponse.user.apellido,
                        run = authResponse.user.run,
                        direccion = authResponse.user.direccion,
                        telefono = authResponse.user.telefono,
                        rol = authResponse.user.rol,
                        createdAt = authResponse.user.createdAt
                    )
                    
                    // Guardar en base de datos local
                    userDao.insert(user)
                    
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            isAuthenticated = true, 
                            user = user
                        ) 
                    }
                    
                    Log.d("AuthViewModel", "Login exitoso para: ${user.email}")
                }
                is HybridAuthRepository.AuthResult.Error -> {
                    Log.e("AuthViewModel", "Error en login: ${result.message}")
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            userMessage = result.message
                        ) 
                    }
                }
            }
        }
    }

    /**
     * Registro de usuario usando autenticación híbrida
     * IMPORTANTE: El campo apellidos es PLURAL en el backend
     */
    fun register(
        run: String,
        nombre: String,
        apellidos: String,
        email: String,
        password: String,
        direccion: String?,
        telefono: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, userMessage = null) }

            // Validaciones
            val trimmedRun = run.trim()
            val trimmedNombre = nombre.trim()
            val trimmedApellidos = apellidos.trim()
            val trimmedEmail = email.trim()
            val trimmedDireccion = direccion?.trim()
            val trimmedTelefono = telefono?.trim()

            if (trimmedRun.isBlank()) {
                _state.update { it.copy(isLoading = false, userMessage = "El RUT no puede estar vacío.") }
                return@launch
            }
            if (trimmedNombre.length < 2 || trimmedNombre.length > 50) {
                _state.update { it.copy(isLoading = false, userMessage = "El nombre debe tener entre 2 y 50 caracteres.") }
                return@launch
            }
            if (trimmedApellidos.length < 2 || trimmedApellidos.length > 50) {
                _state.update { it.copy(isLoading = false, userMessage = "Los apellidos deben tener entre 2 y 50 caracteres.") }
                return@launch
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                _state.update { it.copy(isLoading = false, userMessage = "El formato del email no es válido.") }
                return@launch
            }
            // Validación de contraseña para el backend (más estricta que Firebase)
            if (password.length < 8) {
                _state.update { it.copy(isLoading = false, userMessage = "La contraseña debe tener al menos 8 caracteres.") }
                return@launch
            }
            if (!password.any { it in "!@#\$%^&*()_+-=[]{}|;':,.<>?/~" }) {
                _state.update { it.copy(isLoading = false, userMessage = "La contraseña debe contener al menos un carácter especial.") }
                return@launch
            }

            Log.d("AuthViewModel", "Iniciando registro híbrido para: $trimmedEmail")

            val result = HybridAuthRepository.register(
                email = trimmedEmail,
                password = password,
                run = trimmedRun,
                nombre = trimmedNombre,
                apellidos = trimmedApellidos,
                direccion = trimmedDireccion,
                telefono = trimmedTelefono,
                useFirebase = useFirebaseAuth
            )

            when (result) {
                is HybridAuthRepository.AuthResult.Success -> {
                    val authResponse = result.authResponse
                    
                    // Guardar sesión con el token JWT del BACKEND
                    SessionManager.loginWithAuthResponse(authResponse)
                    
                    // Crear usuario local
                    val user = User(
                        email = authResponse.user.email,
                        nombre = authResponse.user.nombre,
                        apellido = authResponse.user.apellido,
                        run = authResponse.user.run,
                        direccion = authResponse.user.direccion,
                        telefono = authResponse.user.telefono,
                        rol = authResponse.user.rol,
                        createdAt = authResponse.user.createdAt
                    )
                    
                    // Guardar en base de datos local
                    userDao.insert(user)
                    
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            isAuthenticated = true, 
                            user = user
                        ) 
                    }
                    
                    Log.d("AuthViewModel", "Registro exitoso para: ${user.email}")
                }
                is HybridAuthRepository.AuthResult.Error -> {
                    Log.e("AuthViewModel", "Error en registro: ${result.message}")
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            userMessage = result.message
                        ) 
                    }
                }
            }
        }
    }

    /**
     * Registro legacy para compatibilidad con código existente
     */
    fun register(user: User) {
        register(
            run = user.run ?: "",
            nombre = user.nombre,
            apellidos = user.apellido,
            email = user.email,
            password = user.password,
            direccion = user.direccion,
            telefono = user.telefono
        )
    }

    /**
     * Recuperación de contraseña usando Firebase
     */
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, userMessage = null) }
            val trimmedEmail = email.trim()
            
            if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                _state.update { it.copy(isLoading = false, userMessage = "El formato del email no es válido.") }
                return@launch
            }

            val result = HybridAuthRepository.sendPasswordResetEmail(trimmedEmail)
            
            when (result) {
                is HybridAuthRepository.AuthResult.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            userMessage = "Si el email está registrado, recibirás un correo para restablecer tu contraseña."
                        ) 
                    }
                }
                is HybridAuthRepository.AuthResult.Error -> {
                    // No revelar si el email existe o no por seguridad
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            userMessage = "Si el email está registrado, recibirás un correo para restablecer tu contraseña."
                        ) 
                    }
                }
            }
        }
    }

    fun resetAuthState() {
        _state.value = AuthState()
    }

    /**
     * Cierra sesión en Firebase y el backend
     */
    fun logout() {
        // Cerrar sesión en Firebase
        HybridAuthRepository.signOut()
        // Limpiar sesión local y tokens
        SessionManager.logout()
        // Resetear estado
        _state.value = AuthState()
        Log.d("AuthViewModel", "Sesión cerrada")
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
