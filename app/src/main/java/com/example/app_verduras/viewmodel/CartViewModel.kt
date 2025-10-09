package com.example.app_verduras.viewmodel

import com.example.app_verduras.repository.RepositorioPruebas
import androidx.lifecycle.ViewModel
import com.example.app_verduras.Model.Producto

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// --- Modelo de ítem en carrito ---
data class CartItem(
    val product: Producto,
    val qty: Int
)

// --- Estado global del carrito ---
data class CartState(
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0
)

class CartViewModel : ViewModel() {

    private val repo = RepositorioPruebas()

    private val _cartState = MutableStateFlow(CartState())
    val cartState = _cartState.asStateFlow()

    // 🔹 Agregar producto al carrito
    fun addToCart(code: String) {
        val product = repo.getProducts().find { it.codigo == code } ?: return
        val current = _cartState.value

        // Si ya existe el producto, aumenta la cantidad
        val existing = current.items.find { it.product.codigo == code }
        val updatedItems = if (existing != null) {
            current.items.map {
                if (it.product.codigo == code) it.copy(qty = it.qty + 1) else it
            }
        } else {
            current.items + CartItem(product, 1)
        }

        updateCart(updatedItems)
    }

    // 🔹 Aumentar cantidad
    fun increase(code: String) {
        val updated = _cartState.value.items.map {
            if (it.product.codigo == code) it.copy(qty = it.qty + 1) else it
        }
        updateCart(updated)
    }

    // 🔹 Disminuir cantidad
    fun decrease(code: String) {
        val updated = _cartState.value.items.mapNotNull {
            if (it.product.codigo == code) {
                if (it.qty > 1) it.copy(qty = it.qty - 1) else null
            } else it
        }
        updateCart(updated)
    }

    // 🔹 Eliminar producto del carrito
    fun remove(code: String) {
        val updated = _cartState.value.items.filterNot { it.product.codigo == code }
        updateCart(updated)
    }

    // 🔹 Confirmar pedido (simulado)
    fun confirmOrder() {
        _cartState.value = CartState() // Limpia el carrito
    }

    // --- Método auxiliar ---
    private fun updateCart(updatedItems: List<CartItem>) {
        val total = updatedItems.sumOf { it.product.precio * it.qty }
        _cartState.value = CartState(items = updatedItems, total = total)
    }
}
