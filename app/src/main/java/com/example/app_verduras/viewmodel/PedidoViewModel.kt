package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PedidoUiState(
    val pedidos: List<Pedido> = emptyList(),
    val mensaje: String? = null,
    val cargando: Boolean = false
)

class PedidoViewModel(private val repository: PedidoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidoUiState())
    val uiState = _uiState.asStateFlow()

    fun crearPedido(pedido: Pedido) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(cargando = true)
                repository.insert(pedido) // Corregido
                _uiState.value = _uiState.value.copy(mensaje = "Pedido registrado con éxito ✅")
                obtenerPedidosUsuario(pedido.userEmail)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(mensaje = "Error: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(cargando = false)
            }
        }
    }

    fun obtenerPedidosUsuario(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            try {
                val lista = repository.getPedidosByUserEmail(email) // Corregido
                _uiState.value = _uiState.value.copy(pedidos = lista)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(mensaje = "Error al obtener pedidos: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(cargando = false)
            }
        }
    }

    fun actualizarEstado(id: Long, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                repository.updatePedidoStatus(id, nuevoEstado) // Corregido
                _uiState.value = _uiState.value.copy(mensaje = "Estado actualizado a '$nuevoEstado'")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(mensaje = "Error al actualizar estado: ${e.message}")
            }
        }
    }

    fun eliminarPedido(pedido: Pedido) {
        viewModelScope.launch {
            try {
                repository.delete(pedido) // Corregido
                obtenerPedidosUsuario(pedido.userEmail)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(mensaje = "Error al eliminar el pedido: ${e.message}")
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
}

// Factory para poder instanciar el ViewModel con parámetros
class PedidoViewModelFactory(private val repository: PedidoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PedidoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PedidoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
