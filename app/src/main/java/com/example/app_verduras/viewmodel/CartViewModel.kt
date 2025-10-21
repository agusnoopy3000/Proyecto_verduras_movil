package com.example.app_verduras.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.dal.PedidoDao
import com.example.app_verduras.repository.ProductoRepository
import com.example.app_verduras.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Modelos del carrito ---
data class CartItem(val product: Producto, val qty: Int)
data class CartState(val items: List<CartItem> = emptyList()) {
    val total: Double get() = items.sumOf { it.product.precio * it.qty }
}

// --- ViewModel principal del carrito ---
class CartViewModel(
    private val productoRepository: ProductoRepository,
    private val pedidoDao: PedidoDao // Se inyecta el DAO de Pedidos
) : ViewModel() {

    private val _cartState = MutableStateFlow(CartState())
    val cartState = _cartState.asStateFlow()

    fun addToCart(productId: String) {
        viewModelScope.launch {
            // Asumimos que el repositorio tiene un método para buscar por el ID de String
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

    // ✅ Confirmar pedido (Sin cambios, ya estaba correcto)
    fun confirmOrder(onSuccess: () -> Unit) {
        val currentUser = SessionManager.currentUser
        if (currentUser == null) {
            Log.e("CartViewModel", "Error: No hay un usuario logueado para confirmar el pedido.")
            return
        }

        val currentState = _cartState.value
        if (currentState.items.isEmpty()) {
            Log.w("CartViewModel", "Intento de confirmar un pedido con el carrito vacío.")
            return
        }

        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val newPedido = Pedido(
                userEmail = currentUser.email,
                fechaEntrega = today, // Se podría expandir para que el usuario elija
                direccion = "Dirección de prueba 123", // Se podría obtener del perfil del usuario
                total = currentState.total,
                estado = "Pendiente"
            )

            pedidoDao.insert(newPedido)
            _cartState.value = CartState() // Limpia el carrito

            withContext(Dispatchers.Main) {
                onSuccess() // Ejecuta la navegación
            }
        }
    }

    // --- Factory para crear el ViewModel con dependencias ---
    class Factory(
        private val productoRepository: ProductoRepository,
        private val pedidoDao: PedidoDao // Se añade el DAO de Pedidos a la Factory
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
                return CartViewModel(productoRepository, pedidoDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
