package com.example.app_verduras.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Gestor de autenticación con Firebase.
 * 
 * Esta clase maneja la autenticación inicial con Firebase (login/registro).
 * Después de autenticar con Firebase, se debe sincronizar con el backend
 * de Spring Boot usando el endpoint /auth/firebase-sync.
 * 
 * FLUJO DE AUTENTICACIÓN HÍBRIDA:
 * 1. Usuario ingresa credenciales
 * 2. FirebaseAuthManager autentica con Firebase
 * 3. Se obtiene el ID Token de Firebase
 * 4. Se envía el token al backend para sincronización
 * 5. El backend valida el token y devuelve su propio JWT
 * 6. La app usa el JWT del backend para todas las demás llamadas
 * 
 * NOTA: Si Firebase no está configurado correctamente, las operaciones
 * devolverán Error y el HybridAuthRepository usará el fallback al backend directo.
 */
object FirebaseAuthManager {
    
    private const val TAG = "FirebaseAuthManager"
    
    // Flag para verificar si Firebase está disponible
    private var firebaseAvailable: Boolean = true
    
    private val firebaseAuth: FirebaseAuth? by lazy { 
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth no está disponible: ${e.message}")
            firebaseAvailable = false
            null
        }
    }
    
    /**
     * Verifica si Firebase está disponible y configurado
     */
    fun isFirebaseAvailable(): Boolean {
        return firebaseAvailable && firebaseAuth != null
    }
    
    /**
     * Resultado de operaciones de Firebase
     */
    sealed class FirebaseResult<out T> {
        data class Success<T>(val data: T) : FirebaseResult<T>()
        data class Error(val exception: Exception, val message: String) : FirebaseResult<Nothing>()
    }
    
    /**
     * Datos del usuario autenticado en Firebase
     */
    data class FirebaseUserData(
        val uid: String,
        val email: String,
        val idToken: String,
        val isNewUser: Boolean = false
    )
    
    /**
     * Obtiene el usuario actual de Firebase
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth?.currentUser
    
    /**
     * Verifica si hay un usuario autenticado en Firebase
     */
    fun isAuthenticated(): Boolean = firebaseAuth?.currentUser != null
    
    /**
     * Login con email y contraseña en Firebase.
     * 
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return FirebaseResult con los datos del usuario y su ID Token
     */
    suspend fun signIn(email: String, password: String): FirebaseResult<FirebaseUserData> {
        if (!isFirebaseAvailable()) {
            return FirebaseResult.Error(
                Exception("Firebase no disponible"),
                "Firebase no está configurado"
            )
        }
        
        return try {
            Log.d(TAG, "Iniciando login con Firebase para: $email")
            
            val result = firebaseAuth!!.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            
            if (user != null) {
                // Obtener el ID Token de Firebase (esto es crucial para el backend)
                val idToken = user.getIdToken(true).await().token
                
                if (idToken != null) {
                    Log.d(TAG, "Login exitoso en Firebase. UID: ${user.uid}")
                    FirebaseResult.Success(
                        FirebaseUserData(
                            uid = user.uid,
                            email = user.email ?: email,
                            idToken = idToken,
                            isNewUser = false
                        )
                    )
                } else {
                    Log.e(TAG, "No se pudo obtener el ID Token de Firebase")
                    FirebaseResult.Error(
                        Exception("Token nulo"),
                        "No se pudo obtener el token de autenticación"
                    )
                }
            } else {
                Log.e(TAG, "Usuario nulo después del login")
                FirebaseResult.Error(
                    Exception("Usuario nulo"),
                    "Error al obtener los datos del usuario"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login de Firebase: ${e.message}", e)
            val errorMessage = mapFirebaseError(e)
            FirebaseResult.Error(e, errorMessage)
        }
    }
    
    /**
     * Registro de nuevo usuario en Firebase.
     * 
     * @param email Email del nuevo usuario
     * @param password Contraseña (mín. 6 caracteres para Firebase)
     * @return FirebaseResult con los datos del usuario y su ID Token
     */
    suspend fun signUp(email: String, password: String): FirebaseResult<FirebaseUserData> {
        if (!isFirebaseAvailable()) {
            return FirebaseResult.Error(
                Exception("Firebase no disponible"),
                "Firebase no está configurado"
            )
        }
        
        return try {
            Log.d(TAG, "Iniciando registro en Firebase para: $email")
            
            val result = firebaseAuth!!.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            
            if (user != null) {
                // Obtener el ID Token de Firebase
                val idToken = user.getIdToken(true).await().token
                
                if (idToken != null) {
                    Log.d(TAG, "Registro exitoso en Firebase. UID: ${user.uid}")
                    FirebaseResult.Success(
                        FirebaseUserData(
                            uid = user.uid,
                            email = user.email ?: email,
                            idToken = idToken,
                            isNewUser = true
                        )
                    )
                } else {
                    Log.e(TAG, "No se pudo obtener el ID Token de Firebase")
                    FirebaseResult.Error(
                        Exception("Token nulo"),
                        "No se pudo obtener el token de autenticación"
                    )
                }
            } else {
                Log.e(TAG, "Usuario nulo después del registro")
                FirebaseResult.Error(
                    Exception("Usuario nulo"),
                    "Error al crear el usuario"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro de Firebase: ${e.message}", e)
            val errorMessage = mapFirebaseError(e)
            FirebaseResult.Error(e, errorMessage)
        }
    }
    
    /**
     * Obtiene el ID Token actual del usuario autenticado.
     * Útil para refrescar el token si ha expirado.
     * 
     * @param forceRefresh Si es true, fuerza la obtención de un nuevo token
     * @return El ID Token o null si no hay usuario autenticado
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        if (!isFirebaseAvailable()) return null
        
        return try {
            val user = firebaseAuth?.currentUser
            user?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo ID Token: ${e.message}", e)
            null
        }
    }
    
    /**
     * Envía un email de recuperación de contraseña.
     * 
     * @param email Email del usuario
     * @return FirebaseResult indicando éxito o error
     */
    suspend fun sendPasswordResetEmail(email: String): FirebaseResult<Unit> {
        if (!isFirebaseAvailable()) {
            return FirebaseResult.Error(
                Exception("Firebase no disponible"),
                "Firebase no está configurado. Contacta al soporte."
            )
        }
        
        return try {
            Log.d(TAG, "Enviando email de recuperación a: $email")
            firebaseAuth!!.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Email de recuperación enviado")
            FirebaseResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando email de recuperación: ${e.message}", e)
            val errorMessage = mapFirebaseError(e)
            FirebaseResult.Error(e, errorMessage)
        }
    }
    
    /**
     * Cierra la sesión en Firebase.
     * IMPORTANTE: Esto NO cierra la sesión del backend, solo de Firebase.
     */
    fun signOut() {
        Log.d(TAG, "Cerrando sesión de Firebase")
        firebaseAuth?.signOut()
    }
    
    /**
     * Elimina la cuenta del usuario en Firebase.
     * IMPORTANTE: Esto solo elimina de Firebase, no del backend.
     */
    suspend fun deleteAccount(): FirebaseResult<Unit> {
        if (!isFirebaseAvailable()) {
            return FirebaseResult.Error(
                Exception("Firebase no disponible"),
                "Firebase no está configurado"
            )
        }
        
        return try {
            val user = firebaseAuth?.currentUser
            if (user != null) {
                user.delete().await()
                Log.d(TAG, "Cuenta eliminada de Firebase")
                FirebaseResult.Success(Unit)
            } else {
                FirebaseResult.Error(
                    Exception("No hay usuario"),
                    "No hay un usuario autenticado"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando cuenta: ${e.message}", e)
            FirebaseResult.Error(e, mapFirebaseError(e))
        }
    }
    
    /**
     * Mapea los errores de Firebase a mensajes amigables en español
     */
    private fun mapFirebaseError(exception: Exception): String {
        val message = exception.message ?: ""
        return when {
            message.contains("INVALID_EMAIL", ignoreCase = true) ||
            message.contains("invalid email", ignoreCase = true) -> 
                "El formato del email no es válido"
            
            message.contains("WRONG_PASSWORD", ignoreCase = true) ||
            message.contains("wrong password", ignoreCase = true) ||
            message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            message.contains("invalid credential", ignoreCase = true) ->
                "Email o contraseña incorrectos"
            
            message.contains("USER_NOT_FOUND", ignoreCase = true) ||
            message.contains("user not found", ignoreCase = true) ->
                "No existe una cuenta con este email"
            
            message.contains("EMAIL_EXISTS", ignoreCase = true) ||
            message.contains("email already in use", ignoreCase = true) ->
                "Ya existe una cuenta con este email"
            
            message.contains("WEAK_PASSWORD", ignoreCase = true) ||
            message.contains("weak password", ignoreCase = true) ->
                "La contraseña es muy débil. Usa al menos 6 caracteres"
            
            message.contains("TOO_MANY_ATTEMPTS", ignoreCase = true) ||
            message.contains("too many", ignoreCase = true) ->
                "Demasiados intentos fallidos. Intenta más tarde"
            
            message.contains("NETWORK_ERROR", ignoreCase = true) ||
            message.contains("network", ignoreCase = true) ->
                "Error de conexión. Verifica tu internet"
            
            message.contains("USER_DISABLED", ignoreCase = true) ->
                "Esta cuenta ha sido deshabilitada"
            
            message.contains("REQUIRES_RECENT_LOGIN", ignoreCase = true) ->
                "Por seguridad, vuelve a iniciar sesión"
            
            else -> "Error de autenticación: ${exception.localizedMessage ?: "Error desconocido"}"
        }
    }
}
