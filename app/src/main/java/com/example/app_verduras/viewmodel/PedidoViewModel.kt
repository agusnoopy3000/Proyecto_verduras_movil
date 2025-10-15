package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
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

    // Crear un nuevo pedido
    fun crearPedido(pedido: Pedido) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(cargando = true)
                repository.insertarPedido(pedido)
                _uiState.value = _uiState.value.copy(mensaje = "Pedido registrado con éxito ✅")
                obtenerPedidosUsuario(pedido.userEmail)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(mensaje = "Error: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(cargando = false)
            }
        }
    }

    // Cargar pedidos por usuario
    fun obtenerPedidosUsuario(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            val lista = repository.obtenerPedidosUsuario(email)
            _uiState.value = _uiState.value.copy(pedidos = lista, cargando = false)
        }
    }

    // Actualizar estado
    fun actualizarEstado(id: Int, nuevoEstado: String) {
        viewModelScope.launch {
            repository.actualizarEstado(id, nuevoEstado)
            _uiState.value = _uiState.value.copy(mensaje = "Estado actualizado a '$nuevoEstado'")
        }
    }

    // Eliminar pedido
    fun eliminarPedido(pedido: Pedido) {
        viewModelScope.launch {
            repository.eliminarPedido(pedido)
            obtenerPedidosUsuario(pedido.userEmail)
        }
    }

    // Limpiar mensajes temporales
    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
}
