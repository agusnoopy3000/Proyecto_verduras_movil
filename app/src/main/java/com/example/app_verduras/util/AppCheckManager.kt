package com.example.app_verduras.util

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Manager para Firebase App Check.
 * 
 * App Check protege tu app contra el uso no autorizado de los servicios de Firebase,
 * especialmente importante para Vertex AI que tiene costos asociados.
 * 
 * Configuración:
 * - En DEBUG: App Check está DESHABILITADO para permitir desarrollo sin configuración adicional
 * - En RELEASE: Usa PlayIntegrityAppCheckProviderFactory (para producción)
 * 
 * NOTA: Para habilitar App Check en debug, debes registrar el debug token en Firebase Console:
 * 1. Ejecuta la app en modo debug
 * 2. Busca en Logcat: "Enter this debug secret into the Firebase Console"
 * 3. Copia el token y regístralo en Firebase Console → App Check → Manage debug tokens
 */
object AppCheckManager {
    
    private const val TAG = "AppCheckManager"
    private var isInitialized = false
    
    // DESHABILITADO temporalmente para desarrollo
    // El Agente IA (Vertex AI) funcionará sin App Check en modo debug
    // Para producción, cambiar a true y registrar el debug token
    private const val ENABLE_APP_CHECK_IN_DEBUG = false
    
    /**
     * Inicializa Firebase App Check.
     * Debe llamarse antes de usar cualquier servicio de Firebase protegido.
     * 
     * @param context Contexto de la aplicación
     * @param isDebug True si la app está en modo debug
     */
    fun initialize(context: Context, isDebug: Boolean = false) {
        if (isInitialized) {
            Log.d(TAG, "App Check ya está inicializado")
            return
        }
        
        try {
            // En modo debug, no inicializar App Check para evitar errores de attestation
            // Esto permite que Vertex AI funcione sin configuración adicional
            if (isDebug && !ENABLE_APP_CHECK_IN_DEBUG) {
                Log.d(TAG, "===========================================")
                Log.d(TAG, "App Check DESHABILITADO en modo DEBUG")
                Log.d(TAG, "Vertex AI funcionará sin App Check")
                Log.d(TAG, "Para producción, App Check se habilitará automáticamente")
                Log.d(TAG, "===========================================")
                isInitialized = true
                return
            }
            
            val providerFactory: AppCheckProviderFactory = if (isDebug) {
                Log.d(TAG, "Inicializando App Check en modo DEBUG")
                Log.d(TAG, "IMPORTANTE: Debes registrar el debug token en Firebase Console")
                // En modo debug con App Check habilitado, se genera un token de debug
                // Este token debe registrarse en Firebase Console
                DebugAppCheckProviderFactory.getInstance()
            } else {
                Log.d(TAG, "Inicializando App Check con Play Integrity")
                // En producción, usa Play Integrity
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
            
            Firebase.appCheck.installAppCheckProviderFactory(providerFactory)
            
            isInitialized = true
            Log.d(TAG, "App Check inicializado correctamente")
            
            // Log del token para debug (útil para registrar en Firebase Console)
            if (isDebug && ENABLE_APP_CHECK_IN_DEBUG) {
                logDebugToken()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar App Check: ${e.message}", e)
            // No bloquear la app si App Check falla
            isInitialized = true
        }
    }
    
    /**
     * Muestra el token de debug en Logcat para registrar en Firebase Console.
     */
    private fun logDebugToken() {
        try {
            Firebase.appCheck.getAppCheckToken(false)
                .addOnSuccessListener { tokenResponse ->
                    Log.d(TAG, "===========================================")
                    Log.d(TAG, "DEBUG TOKEN obtenido correctamente")
                    Log.d(TAG, "Registra este token en Firebase Console")
                    Log.d(TAG, "===========================================")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al obtener debug token: ${e.message}")
                    Log.e(TAG, "Busca en Logcat: 'Enter this debug secret'")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en logDebugToken: ${e.message}")
        }
    }
    
    /**
     * Verifica si App Check está inicializado.
     */
    fun isReady(): Boolean = isInitialized
}
