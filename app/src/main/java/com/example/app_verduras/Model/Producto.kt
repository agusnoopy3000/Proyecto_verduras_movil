package com.example.app_verduras.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey // El ID ahora viene de la red, no se autogenera.
    val id: String,
    val nombre: String,
    val precio: Double,
    val imagen: String, // Coincide con el modelo de red
    val categoria: String,
    // Campos que solo existen localmente o que se llenarán después.
    val descripcion: String? = null,
    val stock: Int = 0,
    val codigo: String? = null
)
