
package com.example.app_verduras.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.dal.PedidoDao
import com.example.app_verduras.dal.UserDao
import com.example.app_verduras.repository.ProductoRepository
import com.example.app_verduras.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Modelos del carrito ---
data class CartItem(val product: Producto, val qty: Int)
data class CartState(val items: List<CartItem> = emptyList()) {
    val total: Double get() = items.sumOf { it.product.precio * it.qty }
}

sealed class OrderProcessingState {
    object Idle : OrderProcessingState()
    object Processing : OrderProcessingState()
    object Success : OrderProcessingState()
    data class Error(val message: String) : OrderProcessingState()
}

// --- ViewModel principal del carrito ---
class CartViewModel(
    private val productoRepository: ProductoRepository,
    private val pedidoDao: PedidoDao,
    private val userDao: UserDao
) : ViewModel() {

    private val _cartState = MutableStateFlow(CartState())
    val cartState = _cartState.asStateFlow()

    private val _orderProcessingState = MutableStateFlow<OrderProcessingState>(OrderProcessingState.Idle)
    val orderProcessingState = _orderProcessingState.asStateFlow()

    private val _userAddress = MutableStateFlow<String?>(null)
    val userAddress = _userAddress.asStateFlow()

    init {
        loadUserAddress()
    }

    private fun loadUserAddress() {
        viewModelScope.launch {
            val userEmail = SessionManager.currentUser?.email
            if (userEmail != null) {
                val user = userDao.findByEmail(userEmail)
                _userAddress.value = user?.direccion
            }
        }
    }

    fun addToCart(productId: String) {
        viewModelScope.launch {
            val product = productoRepository.getById(productId)
            product?.let { foundProduct ->
                val currentItems = _cartState.value.items.toMutableList()
                val index = currentItems.indexOfFirst { it.product.id == productId }
                if (index >= 0) {
                    val updatedItem = currentItems[index].copy(qty = currentItems[index].qty + 1)
                    currentItems[index] = updatedItem
                } else {
                    currentItems.add(CartItem(product = foundProduct, qty = 1))
                }
                _cartState.value = CartState(currentItems)
            }
        }
    }

    fun increase(productId: String) = modifyQty(productId, +1)
    fun decrease(productId: String) = modifyQty(productId, -1)

    private fun modifyQty(id: String, delta: Int) {
        val current = _cartState.value.items.toMutableList()
        val index = current.indexOfFirst { it.product.id == id }
        if (index >= 0) {
            val item = current[index]
            val newQty = (item.qty + delta).coerceAtLeast(0)
            if (newQty == 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(qty = newQty)
            }
            _cartState.value = CartState(current)
        }
    }

    fun remove(productId: String) {
        val currentItems = _cartState.value.items.toMutableList()
        currentItems.removeAll { it.product.id == productId }
        _cartState.value = CartState(currentItems)
    }

    fun confirmOrder(deliveryAddress: String, deliveryDate: String, finalTotal: Double) {
        if (_orderProcessingState.value is OrderProcessingState.Processing) return
        if (_cartState.value.items.isEmpty()) return

        viewModelScope.launch {
            _orderProcessingState.value = OrderProcessingState.Processing

            var errorMessage: String? = null
            val wasSuccessful = try {
                val userEmail = SessionManager.currentUser?.email
                    ?: throw IllegalStateException("Usuario no logueado.")

                val newPedido = Pedido(
                    userEmail = userEmail,
                    fechaEntrega = deliveryDate,
                    direccion = deliveryAddress,
                    total = finalTotal,
                    estado = "En preparación"
                )
                pedidoDao.insert(newPedido)
                true
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("CartViewModel", "Error al confirmar el pedido: ${e.message}")
                false
            }

            delay(2000L) // Simula el tiempo de procesamiento

            if (wasSuccessful) {
                _cartState.value = CartState() // Limpia el carrito
                _orderProcessingState.value = OrderProcessingState.Success
                delay(100L) 
                _orderProcessingState.value = OrderProcessingState.Idle
            } else {
                _orderProcessingState.value = OrderProcessingState.Error(errorMessage ?: "Error desconocido al procesar el pedido.")
            }
        }
    }
    
    fun dismissError() {
        _orderProcessingState.value = OrderProcessingState.Idle
    }

    class Factory(
        private val productoRepository: ProductoRepository,
        private val pedidoDao: PedidoDao,
        private val userDao: UserDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
                return CartViewModel(productoRepository, pedidoDao, userDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
