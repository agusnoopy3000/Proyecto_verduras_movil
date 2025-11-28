package com.example.app_verduras.repository

import com.example.app_verduras.dal.PedidoDao
import com.example.app_verduras.Model.Pedido
import kotlinx.coroutines.flow.Flow

class PedidoRepository(private val pedidoDao: PedidoDao) {

    // Flujo de datos para observar todos los pedidos (usado por el Admin).
    val todosLosPedidos: Flow<List<Pedido>> = pedidoDao.obtenerTodosLosPedidosFlow()

    // Obtener los pedidos para un usuario específico (usado por el cliente).
    suspend fun getPedidosByUserEmail(email: String): List<Pedido> {
        return pedidoDao.getPedidosByUserEmail(email)
    }

    // Insertar un nuevo pedido.
    suspend fun insert(pedido: Pedido) {
        pedidoDao.insert(pedido)
    }

    // Actualizar un pedido completo.
    suspend fun update(pedido: Pedido) {
        pedidoDao.update(pedido)
    }

    // Actualizar solo el estado de un pedido (usado por el Admin).
    suspend fun updatePedidoStatus(id: Long, nuevoEstado: String) {
        pedidoDao.updatePedidoStatus(id, nuevoEstado)
    }

    // Eliminar un pedido.
    suspend fun delete(pedido: Pedido) {
        pedidoDao.delete(pedido)
    }
}
