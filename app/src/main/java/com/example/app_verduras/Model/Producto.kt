package com.example.app_verduras.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val codigo: String,
    val nombre: String,
    // --- CAMPO AÑADIDO ---
    val descripcion: String,
    val categoria: String,
    val precio: Double,
    val stock: Int,
    val img: String? = null
)