package com.example.app_verduras.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val precio: Double,
    val imagen: String,
    val categoria: String,
    val descripcion: String? = null,
    val stock: Int = 0,
    val codigo: String? = null
)
