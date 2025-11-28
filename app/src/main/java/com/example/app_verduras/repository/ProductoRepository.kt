package com.example.app_verduras.repository

import android.content.res.AssetManager
import android.util.Log
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.api.ApiService
import com.example.app_verduras.api.models.ProductResponse
import com.example.app_verduras.dal.ProductoDao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato para el repositorio de productos.
 * Esto permite tener implementaciones falsas para pruebas y una real para la app.
 */
interface ProductoRepository {

    fun getAll(): Flow<List<Producto>>

    suspend fun getById(id: String): Producto?

    suspend fun getCategorias(): List<String>

    suspend fun getByCodigo(codigo: String): Producto? {
        return productoDao.getProductByCodigo(codigo)
    }

    // Función para actualizar un producto
    suspend fun update(product: Producto) {
        productoDao.update(product)
    }

    // Esta función ahora es suspend y tiene un nombre más claro.
    // Solo debe ser llamada desde una corrutina.
    suspend fun initializeDatabaseIfNeeded() {
        // Primero intentar cargar desde la API
        val loadedFromApi = refreshProductsFromNetwork()
        
        // Si falló la carga desde API y no hay productos, cargar del JSON local
        if (!loadedFromApi && productoDao.count() == 0) {
            withContext(Dispatchers.IO) {
                try {
                    val json = assets.open("productos.json").let { InputStreamReader(it).readText() }
                    val listType = object : TypeToken<List<LocalJsonProducto>>() {}.type
                    val localJsonProductos: List<LocalJsonProducto> = Gson().fromJson(json, listType)

                    val productosToInsert = localJsonProductos.map {
                        Producto(
                            id = "local-${it.codigo}",
                            codigo = it.codigo,
                            nombre = it.nombre,
                            precio = it.precio,
                            imagen = it.img,
                            categoria = it.categoria,
                            descripcion = it.descripcion,
                            stock = it.stock
                        )
                    }
                    productoDao.insertAll(productosToInsert)
                    Log.d("ProductoRepository", "Cargados ${productosToInsert.size} productos desde JSON local")
                } catch (e: Exception) {
                    Log.e("ProductoRepository", "Error cargando JSON local: ${e.message}")
                }
            }
        }
    }

    /**
     * Obtiene los productos de la API de Huerto Hogar y los guarda en la base de datos local.
     * @return true si la carga fue exitosa, false en caso contrario
     */
    suspend fun refreshProductsFromNetwork(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getProducts()
                
                if (response.isSuccessful && response.body() != null) {
                    val networkProducts = response.body()!!
                    val localProducts = networkProducts.map { it.toLocalProducto() }
                    
                    // Limpiar productos antiguos y insertar nuevos
                    productoDao.deleteAll()
                    productoDao.insertAll(localProducts)
                    
                    Log.d("ProductoRepository", "Cargados ${localProducts.size} productos desde API")
                    true
                } else {
                    Log.e("ProductoRepository", "Error API: ${response.code()} - ${response.message()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ProductoRepository", "Excepción al cargar productos: ${e.message}")
                false
            }
        }
    }

    /**
     * Busca productos por nombre o descripción
     */
    suspend fun searchProducts(query: String): List<Producto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getProducts(query)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.map { it.toLocalProducto() }
                } else {
                    // Fallback a búsqueda local
                    productoDao.searchProducts("%$query%")
                }
            } catch (e: Exception) {
                Log.e("ProductoRepository", "Error en búsqueda: ${e.message}")
                productoDao.searchProducts("%$query%")
            }
        }
    }
}

/**
 * Función de extensión para convertir un ProductResponse de la API a un Producto local.
 */
fun ProductResponse.toLocalProducto(): Producto {
    return Producto(
        id = this.id,
        codigo = this.codigo,
        nombre = this.nombre,
        precio = this.precio,
        imagen = this.imagen,
        categoria = this.categoria,
        descripcion = this.descripcion,
        stock = this.stock,
        createdAt = this.createdAt
    )
}
