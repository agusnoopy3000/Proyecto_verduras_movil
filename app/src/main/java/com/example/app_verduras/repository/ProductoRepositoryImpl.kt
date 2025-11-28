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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class ProductoRepositoryImpl(
    private val productoDao: ProductoDao,
    private val assets: AssetManager,
    private val apiService: ApiService
) : ProductoRepository {

    private data class LocalJsonProducto(
        val codigo: String,
        val nombre: String,
        val descripcion: String,
        val categoria: String,
        val precio: Double,
        val stock: Int,
        val img: String?
    )

    override fun getAll(): Flow<List<Producto>> = productoDao.getAllProductsFlow()

    override suspend fun getById(id: String): Producto? {
        return productoDao.getProductById(id)
    }

    override suspend fun getCategorias(): List<String> {
        return withContext(Dispatchers.IO) {
            productoDao.getAllProductsFlow().first().map { it.categoria }.distinct()
        }
    }

    override suspend fun update(product: Producto) {
        productoDao.update(product)
    }

    override suspend fun initializeDatabaseIfNeeded() {
        if (productoDao.count() == 0) {
            withContext(Dispatchers.IO) {
                val json = assets.open("productos.json").let { InputStreamReader(it).readText() }
                val listType = object : TypeToken<List<LocalJsonProducto>>() {}.type
                val localJsonProductos: List<LocalJsonProducto> = Gson().fromJson(json, listType)

                val productosToInsert = localJsonProductos.map {
                    Producto(
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

    override suspend fun refreshProductsFromNetwork() {
        withContext(Dispatchers.IO) {
            try {
                val networkProducts = apiService.getProducts()
                val localProducts = networkProducts.map { it.toLocalProducto() }
                productoDao.insertAll(localProducts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun insertAll(productos: List<Producto>) {
        productoDao.insertAll(productos)
    }

    override suspend fun deleteAll() {

        productoDao.clearAll()
    }
}

fun NetworkProducto.toLocalProducto(): Producto {
    return Producto(
        id = this.id,
        nombre = this.nombre,
        precio = this.precio,
        imagen = this.imagen,
        categoria = this.categoria,
        descripcion = "",
        stock = 0,
        codigo = ""
    )
}
