package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.repository.PedidoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderManagementViewModel(private val repository: PedidoRepository) : ViewModel() {

    val pedidos: StateFlow<List<Pedido>> = repository.todosLosPedidos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _eventChannel = Channel<UiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    fun actualizarPedido(pedido: Pedido) {
        viewModelScope.launch {
            repository.update(pedido)
            _eventChannel.send(UiEvent.ShowSnackbar("Cambios guardados correctamente"))
            _eventChannel.send(UiEvent.DismissEditModal) // Send event to dismiss modal
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object DismissEditModal : UiEvent() // New event
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
