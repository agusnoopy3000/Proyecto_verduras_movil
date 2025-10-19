package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductManagementViewModel(private val productoRepository: ProductoRepository) : ViewModel() {

    private val _products = MutableStateFlow<List<Producto>>(emptyList())
    val products: StateFlow<List<Producto>> = _products.asStateFlow()

    init {
        viewModelScope.launch {
            // Se suscribe al Flow del repositorio para obtener actualizaciones automáticas
            productoRepository.getAll().collect { productList ->
                _products.value = productList
            }
        }
    }

    /**
     * Lanza una corrutina para actualizar un producto en la base de datos
     * a través del repositorio.
     */
    fun updateProduct(product: Producto) {
        viewModelScope.launch {
            // Esta función la crearemos en el repositorio en el siguiente paso
            productoRepository.update(product)
        }
    }

    /**
     * Factory para crear una instancia de ProductManagementViewModel con un ProductoRepository.
     */
    class Factory(private val repository: ProductoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductManagementViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
