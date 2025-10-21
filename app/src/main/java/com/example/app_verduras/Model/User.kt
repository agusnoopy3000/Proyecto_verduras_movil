package com.example.app_verduras.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val email: String,
    val nombre: String,
    val apellido: String,
    val password: String,
    val direccion: String? = null,
    val telefono: String? = null
)
