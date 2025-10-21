package com.example.app_verduras.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

// Representa un producto tal como viene de la API de red.
// Es una buena práctica tener modelos separados para la red y para la base de datos local.
data class NetworkProducto(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("imagen") val imagen: String,
    @SerializedName("categoria") val categoria: String
)

interface ApiService {
    /**
     * Obtiene la lista completa de productos desde el servidor.
     * La anotación @GET especifica la ruta del endpoint en la API.
     * La función está marcada como "suspend" porque es una operación de red asíncrona.
     */
    @GET("productos")
    suspend fun getProducts(): List<NetworkProducto>
}
