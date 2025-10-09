package com.example.app_verduras.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_verduras.viewmodel.CartViewModel

@Composable
fun CartScreen(viewModel: CartViewModel) {
    val state by viewModel.cartState.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Carrito", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        if (state.items.isEmpty()) {
            Text("Tu carrito está vacío.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.items) { item ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.product.nombre, fontWeight = FontWeight.SemiBold)
                                Text("${item.product.precio.toInt()} CLP c/u")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.decrease(item.product.codigo) }) {
                                    Text("-")
                                }
                                Text("${item.qty}", modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = { viewModel.increase(item.product.codigo) }) {
                                    Text("+")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Text("Total: ${state.total.toInt()} CLP", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.confirmOrder() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Confirmar pedido") }
        }
    }
}
