package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.repository.PedidoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de pedidos del administrador.
 * 
 * FUNCIONALIDADES:
 * - Ver TODOS los pedidos de TODOS los usuarios (desde Firebase)
 * - Actualizar estado de pedidos (sincronizado con Firebase)
 * - Filtrar pedidos por estado
 * - Ver estado de sincronización en tiempo real
 */
class OrderManagementViewModel(private val repository: PedidoRepository) : ViewModel() {

    // Pedidos en tiempo real desde Firebase (TODOS los pedidos)
    val pedidos: StateFlow<List<Pedido>> = repository.todosLosPedidos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Estado de sincronización
    val syncStatus: StateFlow<PedidoRepository.SyncStatus> = repository.syncStatus
    
    // Filtro de estado actual
    private val _filtroEstado = MutableStateFlow<String?>(null)
    val filtroEstado: StateFlow<String?> = _filtroEstado.asStateFlow()
    
    // Indicador de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventChannel = Channel<UiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()
    
    init {
        // Sincronizar pedidos al iniciar
        refreshPedidos()
    }
    
    /**
     * Fuerza una sincronización de pedidos desde Firebase.
     */
    fun refreshPedidos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.syncFromFirebase()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Filtra los pedidos por estado.
     */
    fun setFiltroEstado(estado: String?) {
        _filtroEstado.value = estado
    }

    /**
     * Actualiza un pedido completo.
     */
    fun actualizarPedido(pedido: Pedido) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.update(pedido)
                _eventChannel.send(UiEvent.ShowSnackbar("Pedido actualizado y sincronizado"))
                _eventChannel.send(UiEvent.DismissEditModal)
            } catch (e: Exception) {
                _eventChannel.send(UiEvent.ShowSnackbar("Error al actualizar: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Actualiza solo el estado de un pedido.
     * Sincroniza automáticamente con Firebase.
     */
    fun actualizarEstadoPedido(pedidoId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updatePedidoStatus(pedidoId, nuevoEstado)
                _eventChannel.send(UiEvent.ShowSnackbar("Estado actualizado: $nuevoEstado"))
            } catch (e: Exception) {
                _eventChannel.send(UiEvent.ShowSnackbar("Error: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Elimina un pedido.
     */
    fun eliminarPedido(pedido: Pedido) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.delete(pedido)
                _eventChannel.send(UiEvent.ShowSnackbar("Pedido eliminado"))
            } catch (e: Exception) {
                _eventChannel.send(UiEvent.ShowSnackbar("Error: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object DismissEditModal : UiEvent()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: PedidoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderManagementViewModel::class.java)) {
                return OrderManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
