
package com.example.app_verduras.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.example.app_verduras.R
import com.example.app_verduras.viewmodel.CartViewModel

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onConfirmOrder: () -> Unit,
    onGoToCatalog: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()

    if (cartState.items.isEmpty()) {
        EmptyCartView(onGoToCatalog = onGoToCatalog)
    } else {
        FullCartView(cartViewModel, onConfirmOrder)
    }
}

@Composable
fun FullCartView(
    cartViewModel: CartViewModel,
    onConfirmOrder: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tu Carrito", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cartState.items, key = { it.product.id }) { item ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.product.imagen).crossfade(true).build(),
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                            error = painterResource(id = android.R.drawable.ic_menu_report_image),
                            contentDescription = item.product.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.product.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("$${item.product.precio} CLP", style = MaterialTheme.typography.bodyLarge)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { cartViewModel.decrease(item.product.id) }) { Text("-", style = MaterialTheme.typography.titleLarge) }
                            Text("${item.qty}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(onClick = { cartViewModel.increase(item.product.id) }) { Text("+", style = MaterialTheme.typography.titleLarge) }
                        }
                        IconButton(onClick = { cartViewModel.remove(item.product.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Subtotal y Botón para continuar ---
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Subtotal: $${cartState.total.toInt()} CLP",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onConfirmOrder,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text("Continuar a la Entrega", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun EmptyCartView(onGoToCatalog: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_cart))
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(250.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("Tu carrito está vacío", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Parece que todavía no has añadido nada. ¡Explora nuestros productos!", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onGoToCatalog) {
            Text("Ir al catálogo")
        }
    }
}
