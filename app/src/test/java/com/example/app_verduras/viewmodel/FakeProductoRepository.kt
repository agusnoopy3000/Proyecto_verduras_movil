package com.example.app_verduras.viewmodel

import com.example.app_verduras.Model.Producto
import com.example.app_verduras.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductoRepository : ProductoRepository {

    private val productos = mutableListOf<Producto>()
    private val productosFlow = MutableStateFlow<List<Producto>>(productos)

    fun setProductos(productosList: List<Producto>) {
        productos.clear()
        productos.addAll(productosList)
        productosFlow.value = productos
    }

    override fun getAll(): Flow<List<Producto>> {
        return productosFlow
    }

    override suspend fun getById(id: String): Producto? {
        return productos.find { it.id == id }
    }

    override suspend fun getCategorias(): List<String> {
        return productos.map { it.categoria }.distinct()
    }

    override suspend fun update(product: Producto) {
        val index = productos.indexOfFirst { it.id == product.id }
        if (index != -1) {
            productos[index] = product
            productosFlow.value = productos
        }
    }

    override suspend fun initializeDatabaseIfNeeded() {}

    override suspend fun refreshProductsFromNetwork() {}

    override suspend fun insertAll(productos: List<Producto>) {
        this.productos.addAll(productos)
        productosFlow.value = this.productos
    }

    override suspend fun deleteAll() {
        productos.clear()
        productosFlow.value = emptyList()
    }
}
