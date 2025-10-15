package com.example.app_verduras.repository

import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.dal.PedidoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PedidoRepository(private val pedidoDao: PedidoDao) {

    // Crear un nuevo pedido
    suspend fun insertarPedido(pedido: Pedido) = withContext(Dispatchers.IO) {
        pedidoDao.insert(pedido)
    }

    // Obtener todos los pedidos (por usuario o todos)
    suspend fun obtenerPedidosUsuario(email: String): List<Pedido> = withContext(Dispatchers.IO) {
        pedidoDao.getPedidosByUser(email)
    }

    suspend fun obtenerTodosPedidos(): List<Pedido> = withContext(Dispatchers.IO) {
        pedidoDao.getAllPedidos()
    }

    // Actualizar estado (Ej: “En preparación” → “Enviado”)
    suspend fun actualizarEstado(id: Int, estado: String) = withContext(Dispatchers.IO) {
        pedidoDao.updateEstado(id, estado)
    }

    // Eliminar pedido
    suspend fun eliminarPedido(pedido: Pedido) = withContext(Dispatchers.IO) {
        pedidoDao.delete(pedido)
    }
}
