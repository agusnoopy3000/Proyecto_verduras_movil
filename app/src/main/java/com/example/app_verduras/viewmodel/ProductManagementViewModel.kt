package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.repository.ProductoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductManagementViewModel(private val repository: ProductoRepository) : ViewModel() {

    // Este bloque se ejecuta una sola vez cuando el ViewModel es creado.
    init {
        // Lanzamos una corutina en el scope del ViewModel para hacer la llamada de red.
        viewModelScope.launch {
            // Le pedimos al repositorio que actualice los productos desde la red.
            repository.refreshProductsFromNetwork()
        }
    }

    // La vista sigue observando el flujo de productos de la base de datos local.
    // La magia es que este flujo se actualizará automáticamente cuando refreshProductsFromNetwork() inserte los nuevos datos.
    val products: StateFlow<List<Producto>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    companion object {
        fun Factory(repository: ProductoRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ProductManagementViewModel::class.java)) {
                        return ProductManagementViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
