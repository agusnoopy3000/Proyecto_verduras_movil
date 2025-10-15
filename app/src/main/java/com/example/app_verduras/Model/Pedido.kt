package com.example.app_verduras.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos")
data class Pedido(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String,         // Relación con User.email
    val fechaEntrega: String,      // formato ISO yyyy-MM-dd
    val direccion: String,
    val total: Double,
    val estado: String = "En preparación"
)
