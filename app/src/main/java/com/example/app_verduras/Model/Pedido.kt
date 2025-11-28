package com.example.app_verduras.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de pedido para almacenamiento local en Room.
 * Sincronizada con el modelo de pedido del backend.
 */
@Entity(tableName = "pedidos")
data class Pedido(
    @PrimaryKey
    val id: Long = 0,
    val userEmail: String,
    val fechaEntrega: String? = null,  // formato ISO yyyy-MM-dd
    val direccionEntrega: String,
    val region: String? = null,
    val comuna: String? = null,
    val comentarios: String? = null,
    val total: Double,
    val estado: String = "PENDIENTE",  // PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO
    val createdAt: String? = null
)

/**
 * Enum para estados de pedido con descripciones
 */
enum class EstadoPedido(val descripcion: String, val color: Long) {
    PENDIENTE("Pendiente de confirmación", 0xFFFFA000),  // Naranja
    CONFIRMADO("Confirmado por administrador", 0xFF2196F3),  // Azul
    ENVIADO("En camino al cliente", 0xFF9C27B0),  // Púrpura
    ENTREGADO("Entregado al cliente", 0xFF4CAF50),  // Verde
    CANCELADO("Pedido cancelado", 0xFFF44336)  // Rojo
}
