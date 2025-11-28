package com.example.app_verduras.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.MultiFactorAssertion
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.auth.PhoneMultiFactorInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Manager para la autenticación multi-factor (MFA) con SMS de Firebase.
 * 
 * Soporta dos flujos:
 * 1. ENROLAMIENTO: Usuario habilita MFA después de iniciar sesión
 * 2. VERIFICACIÓN: Usuario verifica su segundo factor durante login
 * 
 * FLUJO DE ENROLAMIENTO (Habilitar MFA):
 * 1. Usuario inicia sesión normalmente
 * 2. Usuario va a configuración de seguridad
 * 3. Se envía código SMS al número de teléfono
 * 4. Usuario ingresa el código
 * 5. MFA queda habilitado para futuras sesiones
 * 
 * FLUJO DE VERIFICACIÓN (Durante login con MFA habilitado):
 * 1. Usuario ingresa email/password
 * 2. Firebase detecta que tiene MFA y lanza FirebaseAuthMultiFactorException
 * 3. Se envía código SMS al número registrado
 * 4. Usuario ingresa el código
 * 5. Login completado exitosamente
 */
object FirebaseMFAManager {
    
    private const val TAG = "FirebaseMFAManager"
    
    // Timeout para verificación SMS
    private const val SMS_TIMEOUT_SECONDS = 60L
    
    // Estado actual del proceso MFA
    private val _mfaState = MutableStateFlow<MFAState>(MFAState.Idle)
    val mfaState: StateFlow<MFAState> = _mfaState.asStateFlow()
    
    // Almacena el verification ID temporalmente
    private var currentVerificationId: String? = null
    
    // Almacena el resolver para completar MFA durante login
    private var currentResolver: MultiFactorResolver? = null
    
    // Token de reenvío para el resend
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    /**
     * Estados posibles del proceso MFA
     */
    sealed class MFAState {
        object Idle : MFAState()
        object SendingCode : MFAState()
        data class CodeSent(val verificationId: String, val phoneNumber: String) : MFAState()
        object VerifyingCode : MFAState()
        object Success : MFAState()
        data class Error(val message: String) : MFAState()
        data class MFARequired(
            val resolver: MultiFactorResolver,
            val hints: List<String> // Números de teléfono enmascarados
        ) : MFAState()
    }
    
    /**
     * Resultado de operaciones MFA
     */
    sealed class MFAResult {
        object Success : MFAResult()
        data class CodeSent(val verificationId: String) : MFAResult()
        data class MFARequired(
            val resolver: MultiFactorResolver,
            val hints: List<String>
        ) : MFAResult()
        data class Error(val message: String) : MFAResult()
    }
    
    /**
     * Verifica si el usuario actual tiene MFA habilitado.
     */
    fun isUserEnrolledInMFA(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.multiFactor?.enrolledFactors?.isNotEmpty() == true
    }
    
    /**
     * Obtiene la lista de factores MFA enrollados del usuario.
     */
    fun getEnrolledFactors(): List<String> {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.multiFactor?.enrolledFactors?.mapNotNull { factor ->
            when (factor) {
                is PhoneMultiFactorInfo -> factor.phoneNumber
                else -> null
            }
        } ?: emptyList()
    }
    
    // ==================== ENROLAMIENTO MFA ====================
    
    /**
     * Paso 1 del enrolamiento: Envía código SMS para habilitar MFA.
     * 
     * IMPORTANTE: El usuario debe haber iniciado sesión recientemente.
     * Si han pasado más de 5 minutos, Firebase requerirá re-autenticación.
     * 
     * @param phoneNumber Número de teléfono con código de país (ej: +56912345678)
     * @param activity Activity necesaria para reCAPTCHA
     * @return Flow con el resultado del envío
     */
    fun startMFAEnrollment(
        phoneNumber: String,
        activity: Activity
    ): Flow<MFAResult> = callbackFlow {
        val user = FirebaseAuth.getInstance().currentUser
        
        if (user == null) {
            trySend(MFAResult.Error("Debes iniciar sesión primero"))
            close()
            return@callbackFlow
        }
        
        _mfaState.value = MFAState.SendingCode
        Log.d(TAG, "Iniciando enrolamiento MFA para: $phoneNumber")
        
        // Obtener sesión multi-factor
        user.multiFactor.session.addOnCompleteListener { sessionTask ->
            if (!sessionTask.isSuccessful) {
                val error = sessionTask.exception?.message ?: "Error obteniendo sesión"
                Log.e(TAG, "Error obteniendo sesión MFA: $error")
                _mfaState.value = MFAState.Error(mapMFAError(error))
                trySend(MFAResult.Error(mapMFAError(error)))
                close()
                return@addOnCompleteListener
            }
            
            val multiFactorSession = sessionTask.result
            
            // Callbacks para el proceso de verificación
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verificación (poco común para MFA)
                    Log.d(TAG, "Verificación automática completada")
                    completeEnrollment(credential, user)
                }
                
                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "Error en verificación: ${e.message}")
                    val errorMsg = mapMFAError(e.message ?: "Error desconocido")
                    _mfaState.value = MFAState.Error(errorMsg)
                    trySend(MFAResult.Error(errorMsg))
                    close()
                }
                
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG, "Código SMS enviado a: $phoneNumber")
                    currentVerificationId = verificationId
                    resendToken = token
                    _mfaState.value = MFAState.CodeSent(verificationId, phoneNumber)
                    trySend(MFAResult.CodeSent(verificationId))
                }
            }
            
            // Configurar opciones para envío SMS
            val phoneAuthOptions = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setTimeout(SMS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(activity)
                .setMultiFactorSession(multiFactorSession)
                .setCallbacks(callbacks)
                .build()
            
            // Iniciar verificación
            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
        }
        
        awaitClose { 
            Log.d(TAG, "Flow de enrolamiento cerrado")
        }
    }
    
    /**
     * Paso 2 del enrolamiento: Verifica el código SMS y completa el enrolamiento.
     * 
     * @param verificationCode Código de 6 dígitos recibido por SMS
     * @return true si el enrolamiento fue exitoso
     */
    suspend fun completeMFAEnrollment(verificationCode: String): MFAResult {
        val verificationId = currentVerificationId
        val user = FirebaseAuth.getInstance().currentUser
        
        if (verificationId == null) {
            return MFAResult.Error("No hay verificación pendiente")
        }
        
        if (user == null) {
            return MFAResult.Error("No hay usuario autenticado")
        }
        
        _mfaState.value = MFAState.VerifyingCode
        Log.d(TAG, "Completando enrolamiento MFA...")
        
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
            val assertion = PhoneMultiFactorGenerator.getAssertion(credential)
            
            // Completar el enrolamiento
            user.multiFactor.enroll(assertion, "Teléfono principal").await()
            
            Log.d(TAG, "Enrolamiento MFA completado exitosamente")
            _mfaState.value = MFAState.Success
            currentVerificationId = null
            
            MFAResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Error completando enrolamiento: ${e.message}")
            val errorMsg = mapMFAError(e.message ?: "Error verificando código")
            _mfaState.value = MFAState.Error(errorMsg)
            MFAResult.Error(errorMsg)
        }
    }
    
    /**
     * Completa el enrolamiento con credencial (auto-verificación)
     */
    private fun completeEnrollment(credential: PhoneAuthCredential, user: FirebaseUser) {
        val assertion = PhoneMultiFactorGenerator.getAssertion(credential)
        
        user.multiFactor.enroll(assertion, "Teléfono principal")
            .addOnSuccessListener {
                Log.d(TAG, "Enrolamiento MFA completado (auto)")
                _mfaState.value = MFAState.Success
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error en enrolamiento auto: ${e.message}")
                _mfaState.value = MFAState.Error(mapMFAError(e.message ?: "Error"))
            }
    }
    
    // ==================== VERIFICACIÓN MFA (Durante Login) ====================
    
    /**
     * Guarda el resolver MFA cuando Firebase detecta que se requiere segundo factor.
     * Este método es llamado desde HybridAuthRepository cuando captura
     * FirebaseAuthMultiFactorException.
     * 
     * @param resolver El resolver proporcionado por Firebase
     * @return Lista de números de teléfono enmascarados disponibles
     */
    fun setMFAResolver(resolver: MultiFactorResolver): List<String> {
        currentResolver = resolver
        
        // Extraer los hints (números de teléfono enmascarados)
        val hints = resolver.hints.mapNotNull { hint ->
            when (hint) {
                is PhoneMultiFactorInfo -> hint.phoneNumber
                else -> null
            }
        }
        
        Log.d(TAG, "MFA requerido. Opciones disponibles: $hints")
        _mfaState.value = MFAState.MFARequired(resolver, hints)
        
        return hints
    }
    
    /**
     * Paso 1 de verificación MFA: Envía código SMS al número seleccionado.
     * Usado durante el login cuando el usuario ya tiene MFA habilitado.
     * 
     * @param hintIndex Índice del hint seleccionado (generalmente 0)
     * @param activity Activity necesaria para reCAPTCHA
     */
    fun sendVerificationCode(
        hintIndex: Int = 0,
        activity: Activity
    ): Flow<MFAResult> = callbackFlow {
        val resolver = currentResolver
        
        if (resolver == null) {
            trySend(MFAResult.Error("No hay verificación MFA pendiente"))
            close()
            return@callbackFlow
        }
        
        if (hintIndex >= resolver.hints.size) {
            trySend(MFAResult.Error("Opción de verificación no válida"))
            close()
            return@callbackFlow
        }
        
        _mfaState.value = MFAState.SendingCode
        Log.d(TAG, "Enviando código de verificación MFA...")
        
        val selectedHint = resolver.hints[hintIndex] as? PhoneMultiFactorInfo
        if (selectedHint == null) {
            trySend(MFAResult.Error("Solo se soporta verificación por SMS"))
            close()
            return@callbackFlow
        }
        
        // Callbacks para la verificación
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Verificación automática durante login MFA")
                // Completar login automáticamente
                completeSignInWithMFA(credential)
            }
            
            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Error enviando código MFA: ${e.message}")
                val errorMsg = mapMFAError(e.message ?: "Error desconocido")
                _mfaState.value = MFAState.Error(errorMsg)
                trySend(MFAResult.Error(errorMsg))
                close()
            }
            
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                val phoneNumber = selectedHint.phoneNumber ?: "****"
                Log.d(TAG, "Código MFA enviado a: $phoneNumber")
                currentVerificationId = verificationId
                resendToken = token
                _mfaState.value = MFAState.CodeSent(verificationId, phoneNumber)
                trySend(MFAResult.CodeSent(verificationId))
            }
        }
        
        // Configurar opciones
        val phoneAuthOptions = PhoneAuthOptions.newBuilder()
            .setMultiFactorHint(selectedHint)
            .setTimeout(SMS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .setMultiFactorSession(resolver.session)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
        
        awaitClose {
            Log.d(TAG, "Flow de verificación MFA cerrado")
        }
    }
    
    /**
     * Paso 2 de verificación MFA: Completa el login con el código recibido.
     * 
     * @param verificationCode Código de 6 dígitos recibido por SMS
     * @return MFAResult indicando éxito o error
     */
    suspend fun completeSignIn(verificationCode: String): MFAResult {
        val verificationId = currentVerificationId
        val resolver = currentResolver
        
        if (verificationId == null || resolver == null) {
            return MFAResult.Error("No hay verificación pendiente")
        }
        
        _mfaState.value = MFAState.VerifyingCode
        Log.d(TAG, "Completando login con MFA...")
        
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
            val assertion = PhoneMultiFactorGenerator.getAssertion(credential)
            
            // Completar el sign in con el segundo factor
            resolver.resolveSignIn(assertion).await()
            
            Log.d(TAG, "Login MFA completado exitosamente")
            _mfaState.value = MFAState.Success
            
            // Limpiar estado
            currentVerificationId = null
            currentResolver = null
            
            MFAResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Error completando login MFA: ${e.message}")
            val errorMsg = mapMFAError(e.message ?: "Código inválido")
            _mfaState.value = MFAState.Error(errorMsg)
            MFAResult.Error(errorMsg)
        }
    }
    
    /**
     * Completa el login automáticamente (cuando Firebase auto-verifica)
     */
    private fun completeSignInWithMFA(credential: PhoneAuthCredential) {
        val resolver = currentResolver ?: return
        
        val assertion = PhoneMultiFactorGenerator.getAssertion(credential)
        
        resolver.resolveSignIn(assertion)
            .addOnSuccessListener {
                Log.d(TAG, "Login MFA completado (auto)")
                _mfaState.value = MFAState.Success
                currentResolver = null
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error en login MFA auto: ${e.message}")
                _mfaState.value = MFAState.Error(mapMFAError(e.message ?: "Error"))
            }
    }
    
    // ==================== DESHABILITACIÓN MFA ====================
    
    /**
     * Deshabilita MFA para el usuario actual.
     * IMPORTANTE: El usuario debe haber iniciado sesión recientemente.
     */
    suspend fun unenrollMFA(): MFAResult {
        val user = FirebaseAuth.getInstance().currentUser
        
        if (user == null) {
            return MFAResult.Error("No hay usuario autenticado")
        }
        
        val factors = user.multiFactor.enrolledFactors
        if (factors.isEmpty()) {
            return MFAResult.Error("El usuario no tiene MFA habilitado")
        }
        
        Log.d(TAG, "Deshabilitando MFA...")
        
        return try {
            // Deshabilitar todos los factores
            factors.forEach { factor ->
                user.multiFactor.unenroll(factor).await()
            }
            
            Log.d(TAG, "MFA deshabilitado exitosamente")
            MFAResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Error deshabilitando MFA: ${e.message}")
            MFAResult.Error(mapMFAError(e.message ?: "Error al deshabilitar"))
        }
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Reenvía el código SMS.
     */
    fun resendCode(
        phoneNumber: String,
        activity: Activity
    ): Flow<MFAResult> = callbackFlow {
        val token = resendToken
        
        if (token == null) {
            trySend(MFAResult.Error("No hay verificación activa para reenviar"))
            close()
            return@callbackFlow
        }
        
        _mfaState.value = MFAState.SendingCode
        Log.d(TAG, "Reenviando código SMS...")
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verificación
            }
            
            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Error reenviando: ${e.message}")
                trySend(MFAResult.Error(mapMFAError(e.message ?: "Error")))
                close()
            }
            
            override fun onCodeSent(
                verificationId: String,
                newToken: PhoneAuthProvider.ForceResendingToken
            ) {
                currentVerificationId = verificationId
                resendToken = newToken
                _mfaState.value = MFAState.CodeSent(verificationId, phoneNumber)
                trySend(MFAResult.CodeSent(verificationId))
            }
        }
        
        // Opciones con token de reenvío
        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phoneNumber)
            .setTimeout(SMS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
        
        awaitClose { }
    }
    
    /**
     * Resetea el estado MFA.
     */
    fun resetState() {
        _mfaState.value = MFAState.Idle
        currentVerificationId = null
        currentResolver = null
        resendToken = null
    }
    
    /**
     * Mapea errores de Firebase MFA a mensajes amigables.
     */
    private fun mapMFAError(message: String): String {
        return when {
            message.contains("INVALID_VERIFICATION_CODE", ignoreCase = true) ||
            message.contains("invalid verification code", ignoreCase = true) ->
                "El código ingresado no es válido"
            
            message.contains("SESSION_EXPIRED", ignoreCase = true) ||
            message.contains("session expired", ignoreCase = true) ->
                "La sesión expiró. Solicita un nuevo código"
            
            message.contains("TOO_MANY_REQUESTS", ignoreCase = true) ||
            message.contains("too many requests", ignoreCase = true) ->
                "Demasiados intentos. Intenta en unos minutos"
            
            message.contains("QUOTA_EXCEEDED", ignoreCase = true) ->
                "Límite de SMS excedido. Intenta más tarde"
            
            message.contains("INVALID_PHONE_NUMBER", ignoreCase = true) ||
            message.contains("invalid phone", ignoreCase = true) ->
                "Número de teléfono no válido. Usa formato +56XXXXXXXXX"
            
            message.contains("MISSING_PHONE_NUMBER", ignoreCase = true) ->
                "Debes ingresar un número de teléfono"
            
            message.contains("REQUIRES_RECENT_LOGIN", ignoreCase = true) ||
            message.contains("recent login", ignoreCase = true) ->
                "Por seguridad, vuelve a iniciar sesión"
            
            message.contains("NETWORK", ignoreCase = true) ||
            message.contains("network", ignoreCase = true) ->
                "Error de conexión. Verifica tu internet"
            
            message.contains("CAPTCHA", ignoreCase = true) ||
            message.contains("reCAPTCHA", ignoreCase = true) ->
                "Error de verificación. Intenta de nuevo"
            
            message.contains("UNSUPPORTED_FIRST_FACTOR", ignoreCase = true) ->
                "MFA no disponible para este tipo de cuenta"
            
            else -> "Error: $message"
        }
    }
}
