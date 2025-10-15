package com.example.app_verduras.util

import com.example.app_verduras.Model.User

/**
 * Un objeto singleton simple para gestionar la sesión del usuario.
 * En una app real, esto podría ser reemplazado por una solución más robusta
 * como SharedPreferences encriptadas o un gestor de dependencias con скоуп de sesión.
 */
object SessionManager {
    var currentUser: User? = null

    fun login(user: User) {
        currentUser = user
    }

    fun logout() {
        currentUser = null
    }
}