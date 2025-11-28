package com.example.app_verduras.api.models

import com.google.gson.annotations.SerializedName

/**
 * Item de un pedido en la request.
 * IMPORTANTE: productoId debe ser el CÓDIGO del producto (ej: "VH-001"), NO el id numérico
 */
data class OrderItemRequest(
    @SerializedName("productoId") val productoId: String,  // Código del producto (ej: "VH-001")
    @SerializedName("cantidad") val cantidad: Int
)

/**
 * Request para crear un pedido
 */
data class OrderRequest(
    @SerializedName("direccionEntrega") val direccionEntrega: String,
    @SerializedName("fechaEntrega") val fechaEntrega: String?,  // Formato ISO: "2025-12-01"
    @SerializedName("region") val region: String? = null,
    @SerializedName("comuna") val comuna: String? = null,
    @SerializedName("comentarios") val comentarios: String? = null,
    @SerializedName("items") val items: List<OrderItemRequest>
)

/**
 * Item de un pedido en la respuesta
 */
data class OrderItemResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("productoId") val productoId: String,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precioUnitario") val precioUnitario: Double
)

/**
 * Respuesta de un pedido
 */
data class OrderResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("user") val user: UserResponse,
    @SerializedName("items") val items: List<OrderItemResponse>,
    @SerializedName("total") val total: Double,
    @SerializedName("estado") val estado: String,  // PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO
    @SerializedName("direccionEntrega") val direccionEntrega: String,
    @SerializedName("region") val region: String?,
    @SerializedName("comuna") val comuna: String?,
    @SerializedName("comentarios") val comentarios: String?,
    @SerializedName("fechaEntrega") val fechaEntrega: String?,
    @SerializedName("createdAt") val createdAt: String?
)

/**
 * Enum para estados de pedido
 */
enum class OrderStatus(val descripcion: String) {
    PENDIENTE("Pendiente de confirmación"),
    CONFIRMADO("Confirmado por administrador"),
    ENVIADO("En camino al cliente"),
    ENTREGADO("Entregado al cliente"),
    CANCELADO("Pedido cancelado")
}
