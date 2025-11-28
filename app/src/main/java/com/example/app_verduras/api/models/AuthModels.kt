package com.example.app_verduras.api.models

import com.google.gson.annotations.SerializedName

/**
 * Request para registro de usuario.
 * IMPORTANTE: El campo se llama "apellidos" (plural) en el backend
 */
data class RegisterRequest(
    @SerializedName("run") val run: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,  // ¡PLURAL! - Crítico
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("telefono") val telefono: String? = null
)

/**
 * Request para login
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * Request para sincronización con Firebase.
 * Se envía el token de Firebase para que el backend lo valide y genere su propio JWT.
 */
data class FirebaseSyncRequest(
    @SerializedName("firebaseIdToken") val firebaseIdToken: String,
    @SerializedName("run") val run: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("apellidos") val apellidos: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("telefono") val telefono: String? = null
)

/**
 * Response de autenticación (login y registro)
 */
data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserResponse
)

/**
 * Información del usuario en respuestas
 */
data class UserResponse(
    @SerializedName("email") val email: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("run") val run: String?,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("rol") val rol: String,
    @SerializedName("createdAt") val createdAt: String?
)
