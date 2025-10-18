package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_verduras.viewmodel.CartViewModel

// --- CAMBIO 1: La firma de la función cambia ---
// Ya no necesita NavController, ahora recibe onConfirmOrder
@Composable
fun CartScreen(viewModel: CartViewModel, onConfirmOrder: () -> Unit) {
    val state by viewModel.cartState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Tu carrito está vacío.", style = MaterialTheme.typography.headlineSmall)
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

                // --- CAMBIO 2: El onClick ahora es mucho más limpio ---
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
