package com.example.app_verduras.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Esta es la URL base de tu API.
    // 10.0.2.2 es una IP especial que desde el emulador de Android apunta al localhost de tu máquina.
    // Es ideal para pruebas locales antes de desplegar en EC2.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Creación "lazy" (perezosa) de la instancia de Retrofit. No se crea hasta que se necesita por primera vez.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Usa Gson para convertir JSON a objetos Kotlin
            .build()
    }

    // Un objeto público para acceder al ApiService ya configurado.
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
