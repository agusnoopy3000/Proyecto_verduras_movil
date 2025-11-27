package com.example.app_verduras.viewmodel

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para PedidoUiState.
 * 
 * Estas pruebas verifican el estado de la UI para la gestión de pedidos.
 */
class PedidoViewModelTest {

    @Test
    fun `estado inicial tiene lista vacia`() {
        val state = PedidoUiState()
        
        assertTrue(state.pedidos.isEmpty())
        assertNull(state.mensaje)
        assertFalse(state.cargando)
    }

    @Test
    fun `estado de carga activa`() {
        val state = PedidoUiState(cargando = true)
        
        assertTrue(state.cargando)
    }

    @Test
    fun `estado con mensaje de exito`() {
        val state = PedidoUiState(mensaje = "Pedido creado exitosamente")
        
        assertEquals("Pedido creado exitosamente", state.mensaje)
        assertFalse(state.cargando)
    }

    @Test
    fun `estado con mensaje de error`() {
        val state = PedidoUiState(mensaje = "Error al crear pedido")
        
        assertEquals("Error al crear pedido", state.mensaje)
    }

    @Test
    fun `copia de estado con carga finalizada`() {
        val stateInicial = PedidoUiState(cargando = true)
        val stateFinal = stateInicial.copy(
            cargando = false,
            mensaje = "Operación completada"
        )
        
        assertTrue(stateInicial.cargando)
        assertFalse(stateFinal.cargando)
        assertEquals("Operación completada", stateFinal.mensaje)
    }

    @Test
    fun `limpiar mensaje del estado`() {
        val stateConMensaje = PedidoUiState(mensaje = "Algún mensaje")
        val stateSinMensaje = stateConMensaje.copy(mensaje = null)
        
        assertNotNull(stateConMensaje.mensaje)
        assertNull(stateSinMensaje.mensaje)
    }
}
