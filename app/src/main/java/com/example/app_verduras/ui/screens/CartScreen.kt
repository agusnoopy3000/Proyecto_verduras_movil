package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.app_verduras.viewmodel.CartViewModel

// --- CAMBIO 1: La firma ahora incluye onGoToCatalog ---
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onConfirmOrder: () -> Unit,
    onGoToCatalog: () -> Unit
) {
    val state by viewModel.cartState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (state.items.isEmpty()) {
            // --- CAMBIO 2: Vista mejorada para el carrito vacío ---
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito vacío",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Tu carrito está vacío",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Parece que todavía no has añadido nada. ¡Explora nuestros productos!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onGoToCatalog) {
                    Text("Ir al catálogo")
                }
            }
        } else {
            Text("Tu Carrito", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.items) { item ->
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.nombre, fontWeight = FontWeight.SemiBold)
                                Text("$${item.product.precio} CLP")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.decrease(item.product.id) }) {
                                    Text("-", style = MaterialTheme.typography.titleLarge)
                                }
                                Text("${item.qty}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = { viewModel.increase(item.product.id) }) {
                                    Text("+", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Resumen del total
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Total: $${state.total} CLP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.confirmOrder(onConfirmOrder) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("Confirmar Pedido", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
