package com.example.app_verduras.Model

data class Producto(
    val codigo: String,
    val nombre: String,
    val categoria: String,
    val precio: Double,
    val stock: Int,
    val img: String? = null
)
