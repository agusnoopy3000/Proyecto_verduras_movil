package com.example.app_verduras.api.models

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de producto desde la API
 */
data class ProductResponse(
    @SerializedName("id") val id: String,
    @SerializedName("codigo") val codigo: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("imagen") val imagen: String?,
    @SerializedName("categoria") val categoria: String?,
    @SerializedName("createdAt") val createdAt: String?
)
