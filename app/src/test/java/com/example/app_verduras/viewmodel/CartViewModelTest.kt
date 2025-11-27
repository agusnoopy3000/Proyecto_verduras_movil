package com.example.app_verduras.viewmodel

import com.example.app_verduras.Model.Producto
import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para CartItem, CartState y OrderProcessingState.
 * 
 * Estas pruebas verifican la lógica del carrito de compras sin necesidad
 * de mocks complejos de Room o coroutines.
 */
class CartViewModelTest {

    // ===== Tests para CartItem =====

    @Test
    fun `crear CartItem con producto y cantidad`() {
        val producto = createTestProducto()
        val cartItem = CartItem(product = producto, qty = 3)
        
        assertEquals("PROD-001", cartItem.product.id)
        assertEquals(3, cartItem.qty)
    }

    @Test
    fun `cantidad inicial de CartItem es correcta`() {
        val producto = createTestProducto(precio = 1000.0)
        val cartItem = CartItem(product = producto, qty = 1)
        
        assertEquals(1, cartItem.qty)
    }

    @Test
    fun `copia de CartItem con cantidad modificada`() {
        val producto = createTestProducto()
        val original = CartItem(product = producto, qty = 2)
        val modificado = original.copy(qty = 5)
        
        assertEquals(2, original.qty)
        assertEquals(5, modificado.qty)
        assertEquals(original.product.id, modificado.product.id)
    }

    // ===== Tests para CartState =====

    @Test
    fun `CartState vacio tiene total 0`() {
        val cartState = CartState()
        
        assertTrue(cartState.items.isEmpty())
        assertEquals(0.0, cartState.total, 0.01)
    }

    @Test
    fun `CartState calcula total correctamente con un item`() {
        val producto = createTestProducto(precio = 2500.0)
        val items = listOf(CartItem(product = producto, qty = 2))
        val cartState = CartState(items = items)
        
        assertEquals(5000.0, cartState.total, 0.01) // 2500 * 2 = 5000
    }

    @Test
    fun `CartState calcula total con multiples items`() {
        val producto1 = createTestProducto(id = "1", precio = 1000.0)
        val producto2 = createTestProducto(id = "2", precio = 500.0)
        val items = listOf(
            CartItem(product = producto1, qty = 3), // 3000
            CartItem(product = producto2, qty = 4)  // 2000
        )
        val cartState = CartState(items = items)
        
        assertEquals(5000.0, cartState.total, 0.01) // 3000 + 2000 = 5000
    }

    @Test
    fun `CartState con cantidad 0 no afecta total`() {
        val producto = createTestProducto(precio = 1000.0)
        val items = listOf(CartItem(product = producto, qty = 0))
        val cartState = CartState(items = items)
        
        assertEquals(0.0, cartState.total, 0.01)
    }

    @Test
    fun `CartState cuenta items correctamente`() {
        val items = listOf(
            CartItem(product = createTestProducto(id = "1"), qty = 2),
            CartItem(product = createTestProducto(id = "2"), qty = 3),
            CartItem(product = createTestProducto(id = "3"), qty = 1)
        )
        val cartState = CartState(items = items)
        
        assertEquals(3, cartState.items.size)
    }

    @Test
    fun `total con precios decimales`() {
        val producto = createTestProducto(precio = 1499.99)
        val items = listOf(CartItem(product = producto, qty = 2))
        val cartState = CartState(items = items)
        
        assertEquals(2999.98, cartState.total, 0.01)
    }

    // ===== Tests para OrderProcessingState =====

    @Test
    fun `OrderProcessingState Idle es el estado inicial`() {
        val state: OrderProcessingState = OrderProcessingState.Idle
        
        assertTrue(state is OrderProcessingState.Idle)
        assertFalse(state is OrderProcessingState.Processing)
        assertFalse(state is OrderProcessingState.Success)
        assertFalse(state is OrderProcessingState.Error)
    }

    @Test
    fun `OrderProcessingState Processing indica procesamiento`() {
        val state: OrderProcessingState = OrderProcessingState.Processing
        
        assertTrue(state is OrderProcessingState.Processing)
    }

    @Test
    fun `OrderProcessingState Success indica exito`() {
        val state: OrderProcessingState = OrderProcessingState.Success()
        
        assertTrue(state is OrderProcessingState.Success)
    }

    @Test
    fun `OrderProcessingState Error contiene mensaje`() {
        val errorMessage = "No se pudo procesar el pedido"
        val state = OrderProcessingState.Error(errorMessage)
        
        assertTrue(state is OrderProcessingState.Error)
        assertEquals(errorMessage, (state as OrderProcessingState.Error).message)
    }

    @Test
    fun `diferentes mensajes de error`() {
        val error1 = OrderProcessingState.Error("Error de red")
        val error2 = OrderProcessingState.Error("Usuario no logueado")
        
        assertNotEquals(error1.message, error2.message)
    }

    // ===== Tests adicionales de lógica de carrito =====

    @Test
    fun `agregar item aumenta el total`() {
        val producto = createTestProducto(precio = 1000.0)
        
        val stateBefore = CartState(items = emptyList())
        val stateAfter = CartState(items = listOf(CartItem(producto, 1)))
        
        assertEquals(0.0, stateBefore.total, 0.01)
        assertEquals(1000.0, stateAfter.total, 0.01)
    }

    @Test
    fun `eliminar item reduce el total`() {
        val producto1 = createTestProducto(id = "1", precio = 1000.0)
        val producto2 = createTestProducto(id = "2", precio = 500.0)
        
        val stateBefore = CartState(items = listOf(
            CartItem(producto1, 1),
            CartItem(producto2, 1)
        ))
        
        val stateAfter = CartState(items = listOf(
            CartItem(producto1, 1)
        ))
        
        assertEquals(1500.0, stateBefore.total, 0.01)
        assertEquals(1000.0, stateAfter.total, 0.01)
    }

    @Test
    fun `aumentar cantidad aumenta total`() {
        val producto = createTestProducto(precio = 1000.0)
        
        val stateBefore = CartState(items = listOf(CartItem(producto, 2)))
        val stateAfter = CartState(items = listOf(CartItem(producto, 3)))
        
        assertEquals(2000.0, stateBefore.total, 0.01)
        assertEquals(3000.0, stateAfter.total, 0.01)
    }

    @Test
    fun `disminuir cantidad disminuye total`() {
        val producto = createTestProducto(precio = 1000.0)
        
        val stateBefore = CartState(items = listOf(CartItem(producto, 3)))
        val stateAfter = CartState(items = listOf(CartItem(producto, 2)))
        
        assertEquals(3000.0, stateBefore.total, 0.01)
        assertEquals(2000.0, stateAfter.total, 0.01)
    }

    // ===== Helper Functions =====

    private fun createTestProducto(
        id: String = "PROD-001",
        codigo: String = "VH-001",
        nombre: String = "Producto Test",
        precio: Double = 1000.0,
        stock: Int = 100
    ): Producto {
        return Producto(
            id = id,
            codigo = codigo,
            nombre = nombre,
            precio = precio,
            imagen = null,
            categoria = "Test",
            descripcion = "Descripción de prueba",
            stock = stock,
            createdAt = null
        )
    }
}
