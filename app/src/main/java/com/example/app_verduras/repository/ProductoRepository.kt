package com.example.app_verduras.repository

import android.content.res.AssetManager
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.api.ApiService
import com.example.app_verduras.api.NetworkProducto
import com.example.app_verduras.dal.ProductoDao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class ProductoRepository(
    private val productoDao: ProductoDao,
    private val assets: AssetManager,
    private val apiService: ApiService
) {

    // Clase temporal que representa la estructura del JSON local
    private data class LocalJsonProducto(
        val codigo: String,
        val nombre: String,
        val descripcion: String,
        val categoria: String,
        val precio: Double,
        val stock: Int,
        val img: String?
    )

    fun getAll(): Flow<List<Producto>> = productoDao.getAllProductsFlow()

    suspend fun getById(id: String): Producto? {
        return productoDao.getProductById(id)
    }

    // Función para actualizar un producto
    suspend fun update(product: Producto) {
        productoDao.update(product)
    }

    // Esta función ahora es suspend y tiene un nombre más claro.
    // Solo debe ser llamada desde una corrutina.
    suspend fun initializeDatabaseIfNeeded() {
        if (productoDao.count() == 0) {
            withContext(Dispatchers.IO) { // Aseguramos que la lectura de archivo se haga fuera del hilo principal
                val json = assets.open("productos.json").let { InputStreamReader(it).readText() }
                val listType = object : TypeToken<List<LocalJsonProducto>>() {}.type
                val localJsonProductos: List<LocalJsonProducto> = Gson().fromJson(json, listType)

                val productosToInsert = localJsonProductos.map {
                    Producto(
                        // Se corrige para usar el "codigo" del JSON, que es único,
                        // para generar el id de la base de datos.
                        id = "local-${it.codigo}",
                        nombre = it.nombre,
                        precio = it.precio,
                        imagen = it.img ?: "",
                        categoria = it.categoria,
                        descripcion = it.descripcion,
                        stock = it.stock,
                        codigo = it.codigo
                    )
                }
                productoDao.insertAll(productosToInsert)
            }
        }
    }

    /**
     * Obtiene los productos de la red, los convierte al modelo local y los inserta en la base de datos.
     */
    suspend fun refreshProductsFromNetwork() {
        withContext(Dispatchers.IO) {
            try {
                val networkProducts = apiService.getProducts()
                val localProducts = networkProducts.map { it.toLocalProducto() }
                productoDao.insertAll(localProducts)
            } catch (e: Exception) {
                // Manejar el error, por ejemplo, loggeándolo.
                // Por ahora, si falla, la app seguirá usando los datos locales.
                e.printStackTrace()
            }
        }
    }
}

/**
 * Función de extensión para convertir un producto de red a un producto de la base de datos local.
 * Corregido para proveer valores por defecto para los campos que no vienen de la red.
 */
fun NetworkProducto.toLocalProducto(): Producto {
    return Producto(
        id = this.id,
        nombre = this.nombre,
        precio = this.precio,
        imagen = this.imagen,
        categoria = this.categoria,
        // Se añaden valores por defecto para los campos que no vienen de la red.
        descripcion = "",
        stock = 0,
        codigo = ""
    )
}
