package com.example.app_verduras.model

import com.example.app_verduras.Model.Producto
import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para el modelo Producto.
 * 
 * Cobertura de casos:
 * - Creación de producto con campos obligatorios
 * - Creación de producto con todos los campos
 * - Valores por defecto
 * - Cálculos de precio
 */
class ProductoTest {

    @Test
    fun `crear producto con campos obligatorios`() {
        val producto = Producto(
            id = "1",
            codigo = "VH-001",
            nombre = "Tomate Orgánico",
            precio = 2500.0
        )
        
        assertEquals("1", producto.id)
        assertEquals("VH-001", producto.codigo)
        assertEquals("Tomate Orgánico", producto.nombre)
        assertEquals(2500.0, producto.precio, 0.01)
    }

    @Test
    fun `producto tiene stock 0 por defecto`() {
        val producto = Producto(
            id = "1",
            codigo = "VH-001",
            nombre = "Tomate",
            precio = 1000.0
        )
        
        assertEquals(0, producto.stock)
    }

    @Test
    fun `crear producto con stock positivo`() {
        val producto = Producto(
            id = "1",
            codigo = "VH-001",
            nombre = "Tomate",
            precio = 1000.0,
            stock = 50
        )
        
        assertEquals(50, producto.stock)
    }

    @Test
    fun `campos opcionales son null por defecto`() {
        val producto = Producto(
            id = "1",
            codigo = "VH-001",
            nombre = "Tomate",
            precio = 1000.0
        )
        
        assertNull(producto.imagen)
        assertNull(producto.categoria)
        assertNull(producto.descripcion)
        assertNull(producto.createdAt)
    }

    @Test
    fun `crear producto con todos los campos`() {
        val producto = Producto(
            id = "123",
            codigo = "FR-001",
            nombre = "Manzana Verde",
            precio = 1500.0,
            imagen = "https://example.com/manzana.jpg",
            categoria = "Frutas",
            descripcion = "Manzanas verdes frescas y orgánicas",
            stock = 100,
            createdAt = "2024-01-15T10:30:00"
        )
        
        assertEquals("123", producto.id)
        assertEquals("FR-001", producto.codigo)
        assertEquals("Manzana Verde", producto.nombre)
        assertEquals(1500.0, producto.precio, 0.01)
        assertEquals("https://example.com/manzana.jpg", producto.imagen)
        assertEquals("Frutas", producto.categoria)
        assertEquals("Manzanas verdes frescas y orgánicas", producto.descripcion)
        assertEquals(100, producto.stock)
        assertEquals("2024-01-15T10:30:00", producto.createdAt)
    }

    @Test
    fun `verificar precio decimal`() {
        val producto = Producto(
            id = "1",
            codigo = "VH-001",
            nombre = "Lechuga",
            precio = 1299.99
        )
        
        assertEquals(1299.99, producto.precio, 0.001)
    }

    @Test
    fun `id es la clave primaria`() {
        val producto1 = Producto(id = "ABC", codigo = "VH-001", nombre = "Prod1", precio = 100.0)
        val producto2 = Producto(id = "ABC", codigo = "VH-002", nombre = "Prod2", precio = 200.0)
        
        assertEquals(producto1.id, producto2.id)
    }

    @Test
    fun `verificar igualdad de productos`() {
        val producto1 = Producto(
            id = "1",
            codigo = "VH-001",
            nombre = "Tomate",
            precio = 2500.0,
            stock = 10
        )
        val producto2 = Producto(
            id = "1",
            codigo = "VH-001",
            nombre = "Tomate",
            precio = 2500.0,
            stock = 10
        )
        
        assertEquals(producto1, producto2)
    }

    @Test
    fun `productos diferentes no son iguales`() {
        val producto1 = Producto(id = "1", codigo = "VH-001", nombre = "Tomate", precio = 1000.0)
        val producto2 = Producto(id = "2", codigo = "VH-002", nombre = "Lechuga", precio = 1500.0)
        
        assertNotEquals(producto1, producto2)
    }

    @Test
    fun `verificar copia de producto con modificaciones`() {
        val original = Producto(
            id = "1",
            codigo = "VH-001",
            nombre = "Tomate",
            precio = 1000.0,
            stock = 50
        )
        
        val modificado = original.copy(precio = 1200.0, stock = 45)
        
        assertEquals(1000.0, original.precio, 0.01)
        assertEquals(50, original.stock)
        assertEquals(1200.0, modificado.precio, 0.01)
        assertEquals(45, modificado.stock)
        assertEquals(original.id, modificado.id)
    }

    @Test
    fun `categorias validas para productos`() {
        val categorias = listOf("Verduras", "Frutas", "Hortalizas", "Orgánicos")
        
        categorias.forEach { categoria ->
            val producto = Producto(
                id = "1",
                codigo = "TEST-001",
                nombre = "Test",
                precio = 100.0,
                categoria = categoria
            )
            assertEquals(categoria, producto.categoria)
        }
    }

    @Test
    fun `precio no puede ser negativo en logica de negocio`() {
        // Nota: El modelo permite precios negativos, pero la lógica de negocio no debería
        val producto = Producto(
            id = "1",
            codigo = "VH-001",
            nombre = "Tomate",
            precio = -100.0
        )
        
        // En pruebas de integración se validaría que no se permita guardar
        assertTrue(producto.precio < 0)
    }
}
