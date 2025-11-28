package com.example.app_verduras.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de producto para almacenamiento local en Room.
 * Sincronizada con el modelo de producto del backend.
 */
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey
    val id: String,
    val codigo: String,  // Código único del producto (ej: "VH-001")
    val nombre: String,
    val precio: Double,
    val imagen: String? = null,
    val categoria: String? = null,
    val descripcion: String? = null,
    val stock: Int = 0,
    val createdAt: String? = null
)
