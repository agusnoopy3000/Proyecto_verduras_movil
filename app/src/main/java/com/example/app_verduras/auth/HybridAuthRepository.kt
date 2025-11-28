package com.example.app_verduras.auth

import android.util.Log
import com.example.app_verduras.api.ApiService
import com.example.app_verduras.api.RetrofitClient
import com.example.app_verduras.api.models.AuthResponse
import com.example.app_verduras.api.models.FirebaseSyncRequest
import com.example.app_verduras.api.models.LoginRequest
import com.example.app_verduras.api.models.RegisterRequest

/**
 * Repositorio que implementa la autenticación híbrida Firebase + Spring Boot.
 * 
 * FLUJO DE AUTENTICACIÓN:
 * 
 * LOGIN:
 * 1. Usuario ingresa email/password
 * 2. Se autentica con Firebase -> obtiene firebaseIdToken
 * 3. Se envía firebaseIdToken al backend (/auth/firebase-sync)
 * 4. Backend valida token, crea/busca usuario, genera JWT propio
 * 5. App guarda JWT del backend para futuras llamadas
 * 
 * REGISTRO:
 * 1. Usuario ingresa datos (email, password, nombre, etc.)
 * 2. Se registra en Firebase -> obtiene firebaseIdToken  
 * 3. Se envía firebaseIdToken + datos al backend (/auth/firebase-sync)
 * 4. Backend valida token, crea usuario con datos adicionales, genera JWT
 * 5. App guarda JWT del backend
 * 
 * FALLBACK:
 * Si Firebase falla, se intenta autenticación directa con el backend
 * para mantener compatibilidad con usuarios existentes que no usan Firebase.
 */
object HybridAuthRepository {
    
    private const val TAG = "HybridAuthRepository"
    
    private val apiService: ApiService by lazy { RetrofitClient.apiService }
    
    /**
     * Resultado de las operaciones de autenticación
     */
    sealed class AuthResult {
        data class Success(val authResponse: AuthResponse) : AuthResult()
        data class Error(val message: String, val isNetworkError: Boolean = false) : AuthResult()
        object MFARequired : AuthResult()
    }
    
    /**
     * Login híbrido: Firebase primero, luego sincronización con backend.
     * 
     * @param email Email del usuario
     * @param password Contraseña
     * @param useFirebase Si es true, usa Firebase primero. Si es false, va directo al backend.
     * @return AuthResult con la respuesta del backend
     */
    suspend fun login(
        email: String, 
        password: String,
        useFirebase: Boolean = true
    ): AuthResult {
        Log.d(TAG, "Iniciando login para: $email (useFirebase: $useFirebase)")
        
        // Verificar si Firebase está disponible cuando se solicita
        val shouldUseFirebase = useFirebase && FirebaseAuthManager.isFirebaseAvailable()
        
        return if (shouldUseFirebase) {
            loginWithFirebase(email, password)
        } else {
            if (useFirebase && !FirebaseAuthManager.isFirebaseAvailable()) {
                Log.w(TAG, "Firebase solicitado pero no disponible, usando backend directo")
            }
            loginDirectToBackend(email, password)
        }
    }
    
    /**
     * Login usando Firebase como proveedor de identidad
     */
    private suspend fun loginWithFirebase(email: String, password: String): AuthResult {
        // Paso 1: Autenticar con Firebase
        Log.d(TAG, "Paso 1: Autenticando con Firebase...")
        val firebaseResult = FirebaseAuthManager.signIn(email, password)
        
        return when (firebaseResult) {
            is FirebaseAuthManager.FirebaseResult.Success -> {
                // Paso 2: Sincronizar con el backend
                Log.d(TAG, "Paso 2: Sincronizando con backend...")
                syncWithBackend(firebaseResult.data.idToken)
            }
            is FirebaseAuthManager.FirebaseResult.Error -> {
                Log.w(TAG, "Firebase falló, intentando backend directo: ${firebaseResult.message}")
                // Fallback: intentar login directo al backend
                loginDirectToBackend(email, password)
            }
        }
    }
    
    /**
     * Login directo al backend (sin Firebase)
     */
    private suspend fun loginDirectToBackend(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Login directo al backend para: $email")
            val response = apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Login directo exitoso")
                AuthResult.Success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Email o contraseña incorrectos"
                    400 -> "Datos de login inválidos"
                    404 -> "Usuario no encontrado"
                    else -> "Error al iniciar sesión: ${response.message()}"
                }
                Log.e(TAG, "Error en login directo: ${response.code()} - ${response.message()}")
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en login directo: ${e.message}", e)
            AuthResult.Error(
                "Error de conexión. Verifica tu internet.",
                isNetworkError = true
            )
        }
    }
    
    /**
     * Registro híbrido: Firebase primero, luego sincronización con backend.
     * 
     * @param email Email del nuevo usuario
     * @param password Contraseña (mín. 8 chars + 1 especial para backend, mín. 6 para Firebase)
     * @param run RUT del usuario
     * @param nombre Nombre del usuario
     * @param apellidos Apellidos del usuario (PLURAL para el backend)
     * @param direccion Dirección (opcional)
     * @param telefono Teléfono (opcional)
     * @param useFirebase Si es true, registra en Firebase primero
     * @return AuthResult con la respuesta del backend
     */
    suspend fun register(
        email: String,
        password: String,
        run: String,
        nombre: String,
        apellidos: String,
        direccion: String? = null,
        telefono: String? = null,
        useFirebase: Boolean = true
    ): AuthResult {
        Log.d(TAG, "Iniciando registro para: $email (useFirebase: $useFirebase)")
        
        // Verificar si Firebase está disponible cuando se solicita
        val shouldUseFirebase = useFirebase && FirebaseAuthManager.isFirebaseAvailable()
        
        return if (shouldUseFirebase) {
            registerWithFirebase(email, password, run, nombre, apellidos, direccion, telefono)
        } else {
            if (useFirebase && !FirebaseAuthManager.isFirebaseAvailable()) {
                Log.w(TAG, "Firebase solicitado pero no disponible, usando backend directo")
            }
            registerDirectToBackend(email, password, run, nombre, apellidos, direccion, telefono)
        }
    }
    
    /**
     * Registro usando Firebase como proveedor de identidad
     */
    private suspend fun registerWithFirebase(
        email: String,
        password: String,
        run: String,
        nombre: String,
        apellidos: String,
        direccion: String?,
        telefono: String?
    ): AuthResult {
        // Paso 1: Registrar en Firebase
        Log.d(TAG, "Paso 1: Registrando en Firebase...")
        val firebaseResult = FirebaseAuthManager.signUp(email, password)
        
        return when (firebaseResult) {
            is FirebaseAuthManager.FirebaseResult.Success -> {
                // Paso 2: Sincronizar con el backend (incluir datos adicionales)
                Log.d(TAG, "Paso 2: Sincronizando con backend (nuevo usuario)...")
                syncWithBackend(
                    firebaseIdToken = firebaseResult.data.idToken,
                    run = run,
                    nombre = nombre,
                    apellidos = apellidos,
                    direccion = direccion,
                    telefono = telefono
                )
            }
            is FirebaseAuthManager.FirebaseResult.Error -> {
                Log.w(TAG, "Firebase registro falló: ${firebaseResult.message}")
                // Para registro, NO hacemos fallback automático
                // ya que queremos que los nuevos usuarios estén en Firebase
                // Pero si el error es "email ya existe", intentamos login
                if (firebaseResult.message.contains("existe", ignoreCase = true)) {
                    Log.d(TAG, "Email ya existe en Firebase, intentando sincronizar...")
                    // El usuario ya existe en Firebase, intentar login
                    loginWithFirebase(email, password)
                } else {
                    // Intentar registro directo al backend como fallback
                    registerDirectToBackend(email, password, run, nombre, apellidos, direccion, telefono)
                }
            }
        }
    }
    
    /**
     * Registro directo al backend (sin Firebase)
     */
    private suspend fun registerDirectToBackend(
        email: String,
        password: String,
        run: String,
        nombre: String,
        apellidos: String,
        direccion: String?,
        telefono: String?
    ): AuthResult {
        return try {
            Log.d(TAG, "Registro directo al backend para: $email")
            
            val request = RegisterRequest(
                run = run,
                nombre = nombre,
                apellidos = apellidos, // ¡PLURAL!
                email = email,
                password = password,
                direccion = direccion,
                telefono = telefono
            )
            
            val response = apiService.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Registro directo exitoso")
                AuthResult.Success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "El email ya está registrado o datos inválidos"
                    409 -> "Ya existe un usuario con este email"
                    else -> "Error al registrar: ${response.message()}"
                }
                Log.e(TAG, "Error en registro directo: ${response.code()} - ${response.message()}")
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en registro directo: ${e.message}", e)
            AuthResult.Error(
                "Error de conexión. Verifica tu internet.",
                isNetworkError = true
            )
        }
    }
    
    /**
     * Sincroniza el usuario de Firebase con el backend de Spring Boot.
     * Este es el paso clave del flujo híbrido: "Token Swap".
     * 
     * @param firebaseIdToken Token de ID de Firebase
     * @param run RUT (solo para registro de nuevos usuarios)
     * @param nombre Nombre (solo para registro)
     * @param apellidos Apellidos (solo para registro)
     * @param direccion Dirección (opcional)
     * @param telefono Teléfono (opcional)
     * @return AuthResult con el JWT del backend
     */
    private suspend fun syncWithBackend(
        firebaseIdToken: String,
        run: String? = null,
        nombre: String? = null,
        apellidos: String? = null,
        direccion: String? = null,
        telefono: String? = null
    ): AuthResult {
        return try {
            Log.d(TAG, "Sincronizando con backend (firebase-sync)...")
            
            val request = FirebaseSyncRequest(
                firebaseIdToken = firebaseIdToken,
                run = run,
                nombre = nombre,
                apellidos = apellidos,
                direccion = direccion,
                telefono = telefono
            )
            
            val response = apiService.syncWithFirebase(request)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Sincronización exitosa con backend")
                AuthResult.Success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Token de Firebase inválido o expirado"
                    400 -> "Datos de sincronización inválidos"
                    404 -> "Endpoint de sincronización no disponible"
                    else -> "Error en sincronización: ${response.message()}"
                }
                Log.e(TAG, "Error en sincronización: ${response.code()} - ${response.message()}")
                
                // Si el endpoint de firebase-sync no existe (404), 
                // el backend aún no lo tiene implementado
                if (response.code() == 404) {
                    Log.w(TAG, "Endpoint firebase-sync no disponible, backend necesita actualización")
                }
                
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en sincronización: ${e.message}", e)
            AuthResult.Error(
                "Error de conexión con el servidor.",
                isNetworkError = true
            )
        }
    }
    
    /**
     * Recuperación de contraseña.
     * Usa Firebase para enviar el email de recuperación.
     * 
     * @param email Email del usuario
     * @return Mensaje indicando el resultado
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        val result = FirebaseAuthManager.sendPasswordResetEmail(email)
        return when (result) {
            is FirebaseAuthManager.FirebaseResult.Success -> {
                AuthResult.Success(
                    AuthResponse(
                        token = "",
                        user = com.example.app_verduras.api.models.UserResponse(
                            email = email,
                            nombre = "",
                            apellido = "",
                            run = null,
                            direccion = null,
                            telefono = null,
                            rol = "",
                            createdAt = null
                        )
                    )
                )
            }
            is FirebaseAuthManager.FirebaseResult.Error -> {
                AuthResult.Error(result.message)
            }
        }
    }
    
    /**
     * Cierra sesión en Firebase y limpia tokens locales.
     */
    fun signOut() {
        Log.d(TAG, "Cerrando sesión (Firebase + local)")
        FirebaseAuthManager.signOut()
    }
    
    /**
     * Verifica si hay sesión activa en Firebase.
     */
    fun isFirebaseAuthenticated(): Boolean = FirebaseAuthManager.isAuthenticated()
    
    /**
     * Obtiene un nuevo token de Firebase (útil para refrescar).
     */
    suspend fun refreshFirebaseToken(): String? = FirebaseAuthManager.getIdToken(forceRefresh = true)
}
