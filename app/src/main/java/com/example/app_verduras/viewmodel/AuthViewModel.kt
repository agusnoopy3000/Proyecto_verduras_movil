package com.example.app_verduras.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.auth.FirebaseMFAManager
import com.example.app_verduras.auth.HybridAuthRepository
import com.example.app_verduras.dal.UserDao
import com.example.app_verduras.firebase.FirestoreService
import com.example.app_verduras.Model.User
import com.example.app_verduras.util.AnalyticsManager
import com.example.app_verduras.util.CrashlyticsManager
import com.example.app_verduras.util.PerformanceManager
import com.example.app_verduras.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MFAMode {
    ENROLLMENT,
    VERIFICATION
}

class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    val mfaState: StateFlow<FirebaseMFAManager.MFAState> = FirebaseMFAManager.mfaState

    private var activity: Activity? = null
    private val useFirebaseAuth = true

    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    fun onUserMessageShown() {
        _state.update { it.copy(userMessage = null) }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, userMessage = null) }
            PerformanceManager.startLoginTrace()

            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()

            if (trimmedEmail.isBlank()) {
                PerformanceManager.stopLoginTrace(false)
                _state.update { it.copy(isLoading = false, userMessage = "El email no puede estar vacío.") }
                return@launch
            }
            if (trimmedPassword.isBlank()) {
                PerformanceManager.stopLoginTrace(false)
                _state.update { it.copy(isLoading = false, userMessage = "La contraseña no puede estar vacía.") }
                return@launch
            }

            Log.d("AuthViewModel", "Iniciando login híbrido para: \$trimmedEmail")

            val result = HybridAuthRepository.login(
                email = trimmedEmail,
                password = trimmedPassword,
                useFirebase = useFirebaseAuth
            )

            when (result) {
                is HybridAuthRepository.AuthResult.Success -> {
                    val authResponse = result.authResponse
                    SessionManager.loginWithAuthResponse(authResponse)
                    
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
                    
                    userDao.insert(user)
                    
                    try {
                        FirestoreService.saveUser(user)
                    } catch (e: Exception) {
                        Log.w("AuthViewModel", "Error sincronizando usuario con Firestore", e)
                    }
                    
                    _state.update {
                        it.copy(isLoading = false, isAuthenticated = true, user = user)
                    }
                    
                    AnalyticsManager.logLogin(if (useFirebaseAuth) "firebase_hybrid" else "backend_direct")
                    AnalyticsManager.setUserId(user.email)
                    AnalyticsManager.setUserRole(user.rol)
                    CrashlyticsManager.setUserId(user.email)
                    CrashlyticsManager.setUserInfo(user.email, user.rol)
                    PerformanceManager.stopLoginTrace(true)
                }
                is HybridAuthRepository.AuthResult.Error -> {
                    CrashlyticsManager.log("Error de login: \${result.message}")
                    PerformanceManager.stopLoginTrace(false)
                    _state.update { it.copy(isLoading = false, userMessage = result.message) }
                }
                is HybridAuthRepository.AuthResult.MFARequired -> {
                    PerformanceManager.stopLoginTrace(false)
                    _state.update { it.copy(isLoading = false, mfaRequired = true) }
                }
            }
        }
    }

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
            if (password.length < 8) {
                _state.update { it.copy(isLoading = false, userMessage = "La contraseña debe tener al menos 8 caracteres.") }
                return@launch
            }

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
                    SessionManager.loginWithAuthResponse(authResponse)
                    
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
                    
                    userDao.insert(user)
                    
                    try {
                        FirestoreService.saveUser(user)
                    } catch (e: Exception) {
                        Log.w("AuthViewModel", "Error sincronizando usuario con Firestore", e)
                    }
                    
                    _state.update {
                        it.copy(isLoading = false, isAuthenticated = true, user = user)
                    }
                    
                    AnalyticsManager.logSignUp(if (useFirebaseAuth) "firebase_hybrid" else "backend_direct")
                    AnalyticsManager.setUserId(user.email)
                    AnalyticsManager.setUserRole(user.rol)
                    CrashlyticsManager.setUserId(user.email)
                    CrashlyticsManager.setUserInfo(user.email, user.rol)
                }
                is HybridAuthRepository.AuthResult.Error -> {
                    CrashlyticsManager.log("Error de registro: \${result.message}")
                    _state.update { it.copy(isLoading = false, userMessage = result.message) }
                }
                is HybridAuthRepository.AuthResult.MFARequired -> {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

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
                    AnalyticsManager.logPasswordReset()
                    _state.update {
                        it.copy(isLoading = false, userMessage = "Si el email está registrado, recibirás un correo para restablecer tu contraseña.")
                    }
                }
                is HybridAuthRepository.AuthResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false, userMessage = "Si el email está registrado, recibirás un correo para restablecer tu contraseña.")
                    }
                }
                is HybridAuthRepository.AuthResult.MFARequired -> {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun resetAuthState() {
        _state.value = AuthState()
    }

    fun isUserEnrolledInMFA(): Boolean {
        return FirebaseMFAManager.isUserEnrolledInMFA()
    }

    fun getEnrolledMFAFactors(): List<String> {
        return FirebaseMFAManager.getEnrolledFactors()
    }

    fun startMFAEnrollment(phoneNumber: String) {
        val currentActivity = activity
        if (currentActivity == null) {
            _state.update { it.copy(userMessage = "Error: Activity no disponible") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            FirebaseMFAManager.startMFAEnrollment(phoneNumber, currentActivity).collect { result ->
                when (result) {
                    is FirebaseMFAManager.MFAResult.CodeSent -> {
                        _state.update { it.copy(isLoading = false, userMessage = "Código enviado a \$phoneNumber") }
                    }
                    is FirebaseMFAManager.MFAResult.Error -> {
                        _state.update { it.copy(isLoading = false, userMessage = result.message) }
                    }
                    is FirebaseMFAManager.MFAResult.Success -> {
                        _state.update { it.copy(isLoading = false, userMessage = "MFA habilitado exitosamente") }
                    }
                    is FirebaseMFAManager.MFAResult.MFARequired -> {
                        _state.update { it.copy(isLoading = false, mfaRequired = true) }
                    }
                }
            }
        }
    }

    fun verifyMFACode(code: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = FirebaseMFAManager.completeMFAEnrollment(code)
            when (result) {
                is FirebaseMFAManager.MFAResult.Success -> {
                    _state.update { it.copy(isLoading = false, mfaRequired = false, userMessage = "Verificación exitosa") }
                }
                is FirebaseMFAManager.MFAResult.Error -> {
                    _state.update { it.copy(isLoading = false, userMessage = result.message) }
                }
                else -> {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun resendMFACode() {
        val currentActivity = activity ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            FirebaseMFAManager.sendVerificationCode(0, currentActivity).collect { result ->
                when (result) {
                    is FirebaseMFAManager.MFAResult.CodeSent -> {
                        _state.update { it.copy(isLoading = false, userMessage = "Código reenviado") }
                    }
                    is FirebaseMFAManager.MFAResult.Error -> {
                        _state.update { it.copy(isLoading = false, userMessage = result.message) }
                    }
                    else -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    fun resetMFAState() {
        FirebaseMFAManager.resetState()
        _state.update { it.copy(mfaRequired = false) }
    }

    fun disableMFA() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val factors = user?.multiFactor?.enrolledFactors ?: emptyList()
                
                if (factors.isNotEmpty()) {
                    user?.multiFactor?.unenroll(factors.first())?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _state.update { it.copy(isLoading = false, userMessage = "Verificación de dos pasos deshabilitada") }
                        } else {
                            _state.update { it.copy(isLoading = false, userMessage = "Error al deshabilitar MFA") }
                        }
                    }
                } else {
                    _state.update { it.copy(isLoading = false, userMessage = "No hay factores MFA para deshabilitar") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, userMessage = "Error: \${e.message}") }
            }
        }
    }

    fun logout() {
        AnalyticsManager.logLogout()
        AnalyticsManager.setUserId(null)
        CrashlyticsManager.clearUserInfo()
        HybridAuthRepository.signOut()
        SessionManager.logout()
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
    val userMessage: String? = null,
    val mfaRequired: Boolean = false
)
