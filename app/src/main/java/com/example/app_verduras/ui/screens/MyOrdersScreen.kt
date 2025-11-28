package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.app_verduras.R
import com.example.app_verduras.api.models.OrderResponse
import com.example.app_verduras.api.models.OrderStatus
import com.example.app_verduras.viewmodel.CartViewModel
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import com.example.app_verduras.ui.theme.HuertoHogarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    val orders by cartViewModel.myOrders.collectAsState()
    val isLoading by cartViewModel.ordersLoading.collectAsState()

    // Cargar pedidos al entrar a la pantalla
    LaunchedEffect(Unit) {
        cartViewModel.loadMyOrders()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HuertoHogarColors.Primary.copy(alpha = 0.08f),
                        HuertoHogarColors.Accent.copy(alpha = 0.5f),
                        HuertoHogarColors.Background
                    )
                )
            )
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = HuertoHogarColors.Primary
                )
            }
            orders.isEmpty() -> {
                EmptyOrdersView()
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(order = order)
                        }
                    }
                }
            }
        }
}

@Composable
fun OrderCard(order: OrderResponse) {
    val statusColor = when (order.estado) {
        "PENDIENTE" -> Color(0xFFFFA000)  // Naranja
        "CONFIRMADO" -> Color(0xFF2196F3)  // Azul
        "ENVIADO" -> Color(0xFF9C27B0)  // Púrpura
        "ENTREGADO" -> Color(0xFF4CAF50)  // Verde
        "CANCELADO" -> Color(0xFFF44336)  // Rojo
        else -> MaterialTheme.colorScheme.outline
    }

    val statusDescription = try {
        OrderStatus.valueOf(order.estado).descripcion
    } catch (e: Exception) {
        order.estado
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado: ID y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = statusDescription,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fecha de creación
            order.createdAt?.let { fecha ->
                Text(
                    text = "📅 Creado: ${formatDate(fecha)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Fecha de entrega
            order.fechaEntrega?.let { fecha ->
                Text(
                    text = "🚚 Entrega: $fecha",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dirección
            Text(
                text = "📍 ${order.direccionEntrega}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Región y comuna
            if (order.region != null || order.comuna != null) {
                Text(
                    text = listOfNotNull(order.comuna, order.region).joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items del pedido
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.cantidad}x ${item.productoId}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$${(item.cantidad * item.precioUnitario).toInt()} CLP",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${order.total.toInt()} CLP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Comentarios
            order.comentarios?.let { comentario ->
                if (comentario.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💬 $comentario",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyOrdersView() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.empty_cart)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tienes pedidos aún",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¡Explora nuestro catálogo y haz tu primer pedido!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

/**
 * Formatea una fecha ISO a un formato más legible
 */
private fun formatDate(isoDate: String): String {
    return try {
        val parts = isoDate.split("T")
        if (parts.isNotEmpty()) {
            val datePart = parts[0]
            val dateComponents = datePart.split("-")
            if (dateComponents.size == 3) {
                "${dateComponents[2]}/${dateComponents[1]}/${dateComponents[0]}"
            } else {
                datePart
            }
        } else {
            isoDate
        }
    } catch (e: Exception) {
        isoDate
    }
}
