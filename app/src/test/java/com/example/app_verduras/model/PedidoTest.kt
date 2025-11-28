package com.example.app_verduras.model

import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.Model.EstadoPedido
import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para el modelo Pedido.
 * 
 * Cobertura de casos:
 * - Creación de pedido con campos obligatorios
 * - Estados de pedido
 * - Validaciones de datos
 */
class PedidoTest {

    @Test
    fun `crear pedido con campos obligatorios`() {
        val pedido = Pedido(
            id = 1L,
            userEmail = "user@example.com",
            direccionEntrega = "Av. Principal 123",
            total = 15000.0
        )
        
        assertEquals(1L, pedido.id)
        assertEquals("user@example.com", pedido.userEmail)
        assertEquals("Av. Principal 123", pedido.direccionEntrega)
        assertEquals(15000.0, pedido.total, 0.01)
    }

    @Test
    fun `pedido tiene estado PENDIENTE por defecto`() {
        val pedido = Pedido(
            userEmail = "user@example.com",
            direccionEntrega = "Dirección Test",
            total = 10000.0
        )
        
        assertEquals("PENDIENTE", pedido.estado)
    }

    @Test
    fun `crear pedido con todos los campos`() {
        val pedido = Pedido(
            id = 100L,
            userEmail = "cliente@example.com",
            fechaEntrega = "2024-12-25",
            direccionEntrega = "Calle Principal 456",
            region = "Metropolitana",
            comuna = "Santiago",
            comentarios = "Dejar en conserjería",
            total = 25500.50,
            estado = "CONFIRMADO",
            createdAt = "2024-01-01T10:00:00"
        )
        
        assertEquals(100L, pedido.id)
        assertEquals("cliente@example.com", pedido.userEmail)
        assertEquals("2024-12-25", pedido.fechaEntrega)
        assertEquals("Calle Principal 456", pedido.direccionEntrega)
        assertEquals("Metropolitana", pedido.region)
        assertEquals("Santiago", pedido.comuna)
        assertEquals("Dejar en conserjería", pedido.comentarios)
        assertEquals(25500.50, pedido.total, 0.01)
        assertEquals("CONFIRMADO", pedido.estado)
        assertEquals("2024-01-01T10:00:00", pedido.createdAt)
    }

    @Test
    fun `campos opcionales son null por defecto`() {
        val pedido = Pedido(
            userEmail = "user@example.com",
            direccionEntrega = "Dirección",
            total = 5000.0
        )
        
        assertNull(pedido.fechaEntrega)
        assertNull(pedido.region)
        assertNull(pedido.comuna)
        assertNull(pedido.comentarios)
        assertNull(pedido.createdAt)
    }

    @Test
    fun `id es 0 por defecto para autogeneración`() {
        val pedido = Pedido(
            userEmail = "user@example.com",
            direccionEntrega = "Dirección",
            total = 5000.0
        )
        
        assertEquals(0L, pedido.id)
    }

    @Test
    fun `verificar estados de pedido válidos`() {
        val estadosValidos = listOf("PENDIENTE", "CONFIRMADO", "ENVIADO", "ENTREGADO", "CANCELADO")
        
        estadosValidos.forEach { estado ->
            val pedido = Pedido(
                userEmail = "user@example.com",
                direccionEntrega = "Dirección",
                total = 1000.0,
                estado = estado
            )
            assertEquals(estado, pedido.estado)
        }
    }

    @Test
    fun `enum EstadoPedido tiene valores correctos`() {
        assertEquals("Pendiente de confirmación", EstadoPedido.PENDIENTE.descripcion)
        assertEquals("Confirmado por administrador", EstadoPedido.CONFIRMADO.descripcion)
        assertEquals("En camino al cliente", EstadoPedido.ENVIADO.descripcion)
        assertEquals("Entregado al cliente", EstadoPedido.ENTREGADO.descripcion)
        assertEquals("Pedido cancelado", EstadoPedido.CANCELADO.descripcion)
    }

    @Test
    fun `enum EstadoPedido tiene colores definidos`() {
        assertNotEquals(0L, EstadoPedido.PENDIENTE.color)
        assertNotEquals(0L, EstadoPedido.CONFIRMADO.color)
        assertNotEquals(0L, EstadoPedido.ENVIADO.color)
        assertNotEquals(0L, EstadoPedido.ENTREGADO.color)
        assertNotEquals(0L, EstadoPedido.CANCELADO.color)
    }

    @Test
    fun `verificar igualdad de pedidos`() {
        val pedido1 = Pedido(
            id = 1L,
            userEmail = "user@example.com",
            direccionEntrega = "Dirección",
            total = 10000.0
        )
        val pedido2 = Pedido(
            id = 1L,
            userEmail = "user@example.com",
            direccionEntrega = "Dirección",
            total = 10000.0
        )
        
        assertEquals(pedido1, pedido2)
    }

    @Test
    fun `pedidos con diferente id no son iguales`() {
        val pedido1 = Pedido(id = 1L, userEmail = "user@example.com", direccionEntrega = "Dir", total = 1000.0)
        val pedido2 = Pedido(id = 2L, userEmail = "user@example.com", direccionEntrega = "Dir", total = 1000.0)
        
        assertNotEquals(pedido1, pedido2)
    }

    @Test
    fun `verificar copia de pedido con cambio de estado`() {
        val original = Pedido(
            id = 1L,
            userEmail = "user@example.com",
            direccionEntrega = "Dirección",
            total = 10000.0,
            estado = "PENDIENTE"
        )
        
        val actualizado = original.copy(estado = "CONFIRMADO")
        
        assertEquals("PENDIENTE", original.estado)
        assertEquals("CONFIRMADO", actualizado.estado)
        assertEquals(original.id, actualizado.id)
    }

    @Test
    fun `total con decimales`() {
        val pedido = Pedido(
            userEmail = "user@example.com",
            direccionEntrega = "Dirección",
            total = 15999.99
        )
        
        assertEquals(15999.99, pedido.total, 0.001)
    }

    @Test
    fun `obtener estado desde enum por nombre`() {
        val estadoString = "ENVIADO"
        val estadoEnum = EstadoPedido.valueOf(estadoString)
        
        assertEquals(EstadoPedido.ENVIADO, estadoEnum)
        assertEquals("En camino al cliente", estadoEnum.descripcion)
    }

    @Test
    fun `todos los estados del enum`() {
        val estados = EstadoPedido.values()
        
        assertEquals(5, estados.size)
        assertTrue(estados.contains(EstadoPedido.PENDIENTE))
        assertTrue(estados.contains(EstadoPedido.CONFIRMADO))
        assertTrue(estados.contains(EstadoPedido.ENVIADO))
        assertTrue(estados.contains(EstadoPedido.ENTREGADO))
        assertTrue(estados.contains(EstadoPedido.CANCELADO))
    }
}
