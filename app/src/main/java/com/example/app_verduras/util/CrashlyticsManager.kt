package com.example.app_verduras.util

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

/**
 * Manager centralizado para Firebase Crashlytics.
 * 
 * Permite registrar errores no fatales, información de usuario
 * y claves personalizadas para facilitar el debugging de crashes.
 */
object CrashlyticsManager {
    
    private const val TAG = "CrashlyticsManager"
    private var isInitialized = false
    
    /**
     * Inicializa Crashlytics.
     * Debe llamarse temprano en el ciclo de vida de la app.
     */
    fun initialize() {
        if (isInitialized) return
        
        try {
            // Crashlytics se inicializa automáticamente, pero podemos
            // configurar opciones adicionales aquí
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
            isInitialized = true
            Log.d(TAG, "Firebase Crashlytics inicializado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar Crashlytics: ${e.message}")
        }
    }
    
    /**
     * Establece el ID del usuario para asociar crashes.
     */
    fun setUserId(userId: String?) {
        try {
            Firebase.crashlytics.setUserId(userId ?: "")
            Log.d(TAG, "User ID establecido en Crashlytics: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer userId: ${e.message}")
        }
    }
    
    /**
     * Establece una clave personalizada para contexto adicional.
     */
    fun setCustomKey(key: String, value: String) {
        try {
            Firebase.crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer custom key: ${e.message}")
        }
    }
    
    /**
     * Establece una clave personalizada (Int)
     */
    fun setCustomKey(key: String, value: Int) {
        try {
            Firebase.crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer custom key: ${e.message}")
        }
    }
    
    /**
     * Establece una clave personalizada (Boolean)
     */
    fun setCustomKey(key: String, value: Boolean) {
        try {
            Firebase.crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer custom key: ${e.message}")
        }
    }
    
    /**
     * Registra información del usuario logueado.
     */
    fun setUserInfo(email: String?, role: String?) {
        try {
            email?.let { setCustomKey("user_email", it) }
            role?.let { setCustomKey("user_role", it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer user info: ${e.message}")
        }
    }
    
    /**
     * Registra un mensaje de log para contexto en crashes.
     */
    fun log(message: String) {
        try {
            Firebase.crashlytics.log(message)
            Log.d(TAG, "Log registrado: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar log: ${e.message}")
        }
    }
    
    /**
     * Registra una excepción no fatal.
     * Útil para errores que se manejan pero queremos trackear.
     */
    fun recordException(throwable: Throwable, message: String? = null) {
        try {
            message?.let { log(it) }
            Firebase.crashlytics.recordException(throwable)
            Log.d(TAG, "Excepción registrada: ${throwable.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar excepción: ${e.message}")
        }
    }
    
    /**
     * Registra un error de red.
     */
    fun recordNetworkError(
        endpoint: String,
        errorCode: Int,
        errorMessage: String?
    ) {
        try {
            setCustomKey("last_network_endpoint", endpoint)
            setCustomKey("last_network_error_code", errorCode)
            log("Network Error: $endpoint - Code: $errorCode - $errorMessage")
            
            val exception = NetworkException(
                "API Error: $endpoint returned $errorCode: $errorMessage"
            )
            Firebase.crashlytics.recordException(exception)
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar network error: ${e.message}")
        }
    }
    
    /**
     * Registra información de la pantalla actual.
     */
    fun setCurrentScreen(screenName: String) {
        try {
            setCustomKey("current_screen", screenName)
            log("Navegando a: $screenName")
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer current screen: ${e.message}")
        }
    }
    
    /**
     * Registra información del carrito para contexto en crashes.
     */
    fun setCartInfo(itemCount: Int, totalValue: Double) {
        try {
            setCustomKey("cart_item_count", itemCount)
            setCustomKey("cart_total", totalValue.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer cart info: ${e.message}")
        }
    }
    
    /**
     * Registra el inicio del proceso de checkout.
     */
    fun logCheckoutStarted(orderId: String? = null) {
        try {
            log("Checkout iniciado${orderId?.let { " - Order: $it" } ?: ""}")
            setCustomKey("in_checkout", true)
            orderId?.let { setCustomKey("current_order_id", it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar checkout: ${e.message}")
        }
    }
    
    /**
     * Registra la finalización del checkout.
     */
    fun logCheckoutCompleted(orderId: String) {
        try {
            log("Checkout completado - Order: $orderId")
            setCustomKey("in_checkout", false)
            setCustomKey("last_completed_order", orderId)
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar checkout completado: ${e.message}")
        }
    }
    
    /**
     * Limpia la información del usuario (para logout).
     */
    fun clearUserInfo() {
        try {
            setUserId(null)
            setCustomKey("user_email", "")
            setCustomKey("user_role", "")
            log("Usuario deslogueado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar user info: ${e.message}")
        }
    }
    
    /**
     * Fuerza un crash de prueba (solo para desarrollo).
     * NO USAR EN PRODUCCIÓN.
     */
    fun testCrash() {
        throw RuntimeException("Test Crash - Crashlytics está funcionando!")
    }
}

/**
 * Excepción personalizada para errores de red.
 */
class NetworkException(message: String) : Exception(message)
