package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.repository.ProductoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Sealed class para representar eventos de UI de un solo uso
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}

class ProductManagementViewModel(private val productoRepository: ProductoRepository) : ViewModel() {

    private val _products = MutableStateFlow<List<Producto>>(emptyList())
    val products: StateFlow<List<Producto>> = _products.asStateFlow()

    // Canal para enviar eventos de un solo uso a la UI
    private val _eventChannel = Channel<UiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            productoRepository.getAll().collect { productList ->
                _products.value = productList
            }
        }
    }

    /**
     * Actualiza un producto y envía un evento a la UI tras el éxito.
     */
    fun updateProduct(product: Producto) {
        viewModelScope.launch {
            productoRepository.update(product)
            // Envía el evento para mostrar el Snackbar
            _eventChannel.send(UiEvent.ShowSnackbar("Save Changes"))
        }
    }

    /**
     * Factory para crear la instancia del ViewModel.
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
