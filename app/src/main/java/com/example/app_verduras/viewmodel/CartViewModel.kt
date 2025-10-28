
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

// PASO 1: Añadir un estado de Error a la sealed class
sealed class OrderProcessingState {
    object Idle : OrderProcessingState()
    object Processing : OrderProcessingState()
    object Success : OrderProcessingState()
    data class Error(val message: String) : OrderProcessingState() // Estado de error con mensaje
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

    // PASO 2: Modificar confirmOrder para que emita el estado de Error
    fun confirmOrder() {
        if (_orderProcessingState.value is OrderProcessingState.Processing) return
        if (_cartState.value.items.isEmpty()) return

        viewModelScope.launch {
            _orderProcessingState.value = OrderProcessingState.Processing

            var errorMessage: String? = null
            val wasSuccessful = try {
                val userEmail = SessionManager.currentUser?.email
                    ?: throw IllegalStateException("Usuario no logueado.")

                val user = userDao.findByEmail(userEmail)
                    ?: throw IllegalStateException("Usuario no encontrado en la base de datos.")

                val newPedido = Pedido(
                    userEmail = user.email,
                    fechaEntrega = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    direccion = user.direccion ?: "Dirección no disponible",
                    total = _cartState.value.total,
                    estado = "En preparación"
                )
                pedidoDao.insert(newPedido)
                true
            } catch (e: Exception) {
                errorMessage = e.message // Capturamos el mensaje de error
                Log.e("CartViewModel", "Error al confirmar el pedido: ${e.message}")
                false
            }

            delay(4000L)

            if (wasSuccessful) {
                _cartState.value = CartState()
                _orderProcessingState.value = OrderProcessingState.Success
                delay(100L) // Pausa para asegurar que la UI procese el estado Success
                _orderProcessingState.value = OrderProcessingState.Idle // Volvemos a Idle
            } else {
                // Si falló, emitimos el estado de Error con el mensaje
                _orderProcessingState.value = OrderProcessingState.Error(errorMessage ?: "Error desconocido al procesar el pedido.")
            }
        }
    }
    
    // PASO 3: Añadir una función para resetear el estado desde la UI
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
