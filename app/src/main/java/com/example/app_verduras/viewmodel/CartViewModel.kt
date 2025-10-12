package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.repository.RepositorioPruebas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// --- Modelos del carrito ---
data class CartItem(val product: Producto, val qty: Int)
data class CartState(val items: List<CartItem> = emptyList()) {
    val total: Double get() = items.sumOf { it.product.precio * it.qty }
}

// --- ViewModel principal del carrito ---
class CartViewModel(
    private val repo: RepositorioPruebas = RepositorioPruebas()
) : ViewModel() {

    private val _cartState = MutableStateFlow(CartState())
    val cartState = _cartState.asStateFlow()

    // 🛒 Agregar producto al carrito
    fun addToCart(productCode: String) {
        // Busca el producto real desde el repositorio
        val product = repo.getProducts().find { it.codigo == productCode }

        // Si no lo encuentra, crea un dummy genérico para evitar crash
        val productoFinal = product ?: Producto(
            codigo = productCode,
            nombre = "Producto $productCode",
            categoria = "Frutas",
            precio = 1200.0,
            stock = 10,
            img = null
        )

        val current = _cartState.value.items.toMutableList()
        val index = current.indexOfFirst { it.product.codigo == productCode }

        if (index >= 0) {
            val updated = current[index].copy(qty = current[index].qty + 1)
            current[index] = updated
        } else {
            current.add(CartItem(product = productoFinal, qty = 1))
        }

        _cartState.value = CartState(current)
    }

    // ➕ Aumentar cantidad
    fun increase(productCode: String) = modifyQty(productCode, +1)

    // ➖ Disminuir cantidad
    fun decrease(productCode: String) = modifyQty(productCode, -1)

    // 🔄 Modifica cantidad interna
    private fun modifyQty(code: String, delta: Int) {
        val current = _cartState.value.items.toMutableList()
        val index = current.indexOfFirst { it.product.codigo == code }
        if (index >= 0) {
            val item = current[index]
            val newQty = (item.qty + delta).coerceAtLeast(0)
            if (newQty == 0) current.removeAt(index)
            else current[index] = item.copy(qty = newQty)
            _cartState.value = CartState(current)
        }
    }

    // ✅ Confirmar pedido
    fun confirmOrder() {
        println("✅ Pedido confirmado con ${_cartState.value.items.size} productos.")
        _cartState.value = CartState() // Limpia el carrito
    }
}
