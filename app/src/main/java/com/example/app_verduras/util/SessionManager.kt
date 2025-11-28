package com.example.app_verduras.util

import android.content.Context
import com.example.app_verduras.Model.User
import com.example.app_verduras.api.TokenManager
import com.example.app_verduras.api.models.AuthResponse

/**
 * Gestor de sesión del usuario.
 * Trabaja en conjunto con TokenManager para mantener el estado de autenticación.
 */
object SessionManager {
    var currentUser: User? = null
        private set

    /**
     * Inicializa el SessionManager con el contexto de la aplicación.
     * Debe llamarse al inicio de la app.
     */
    fun init(context: Context) {
        TokenManager.init(context)
        
        // Restaurar usuario desde SharedPreferences si existe
        if (TokenManager.isLoggedIn()) {
            val email = TokenManager.getUserEmail()
            val nombre = TokenManager.getUserName()
            val apellido = TokenManager.getUserApellido()
            
            if (email != null && nombre != null && apellido != null) {
                currentUser = User(
                    email = email,
                    nombre = nombre,
                    apellido = apellido,
                    run = TokenManager.getUserRun(),
                    direccion = TokenManager.getUserDireccion(),
                    telefono = TokenManager.getUserTelefono(),
                    rol = TokenManager.getUserRol() ?: "USER"
                )
            }
        }
    }

    /**
     * Inicia sesión con la respuesta de autenticación de la API.
     */
    fun loginWithAuthResponse(authResponse: AuthResponse) {
        // Guardar token
        TokenManager.saveToken(authResponse.token)
        
        // Guardar datos del usuario
        val user = authResponse.user
        TokenManager.saveUserData(
            email = user.email,
            nombre = user.nombre,
            apellido = user.apellido,
            run = user.run,
            direccion = user.direccion,
            telefono = user.telefono,
            rol = user.rol
        )
        
        // Crear usuario local
        currentUser = User(
            email = user.email,
            nombre = user.nombre,
            apellido = user.apellido,
            run = user.run,
            direccion = user.direccion,
            telefono = user.telefono,
            rol = user.rol,
            createdAt = user.createdAt
        )
    }

    /**
     * Inicia sesión con un usuario local (compatibilidad con código existente).
     */
    fun login(user: User) {
        currentUser = user
    }

    /**
     * Cierra la sesión actual.
     */
    fun logout() {
        currentUser = null
        TokenManager.clearAll()
    }

    /**
     * Verifica si hay una sesión activa.
     */
    fun isLoggedIn(): Boolean {
        return TokenManager.isLoggedIn() && currentUser != null
    }

    /**
     * Obtiene el token JWT actual.
     */
    fun getToken(): String? = TokenManager.getToken()

    /**
     * Verifica si el usuario actual es administrador.
     */
    fun isAdmin(): Boolean {
        return currentUser?.rol?.equals("ADMIN", ignoreCase = true) == true ||
               currentUser?.email?.equals("superadmin@huertohogar.cl", ignoreCase = true) == true
    }
}