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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app_verduras.viewmodel.CartViewModel

@Composable
fun CartScreen(navController: NavController, viewModel: CartViewModel = viewModel()) {
    val state by viewModel.cartState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Carrito", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        // Si el carrito está vacío, mostrar un mensaje
        //el boton de mostrar catalago cierra la app por error en producto
        if (state.items.isEmpty()) {
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
                Button(onClick = { navController.navigate("catalog") }) {
                    Text("Ir al catálogo")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:", style = MaterialTheme.typography.titleMedium)
                Text("${state.total.toInt()} CLP", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.confirmOrder()
                    navController.navigate("pedido")
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Confirmar pedido") }
        }
    }
}
