package com.example.app_verduras.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de usuario para almacenamiento local en Room.
 * Sincronizada con el modelo de usuario del backend.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val email: String,
    val nombre: String,
    val apellido: String,
    val password: String = "",  // Solo para uso local, no enviar al servidor
    val run: String? = null,
    val direccion: String? = null,
    val telefono: String? = null,
    val rol: String = "USER",
    val createdAt: String? = null
)
