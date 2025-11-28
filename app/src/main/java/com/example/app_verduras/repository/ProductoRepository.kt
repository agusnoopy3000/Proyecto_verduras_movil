package com.example.app_verduras.repository

import com.example.app_verduras.Model.Producto
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato para el repositorio de productos.
 * Esto permite tener implementaciones falsas para pruebas y una real para la app.
 */
interface ProductoRepository {

    fun getAll(): Flow<List<Producto>>

    suspend fun getById(id: String): Producto?

    suspend fun getCategorias(): List<String>

    suspend fun update(product: Producto)

    suspend fun initializeDatabaseIfNeeded()

    suspend fun refreshProductsFromNetwork()

    suspend fun insertAll(productos: List<Producto>)

    suspend fun deleteAll()
}
