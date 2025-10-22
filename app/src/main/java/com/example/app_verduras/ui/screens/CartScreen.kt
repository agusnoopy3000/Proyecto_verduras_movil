package com.example.app_verduras.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.LocationViewModel

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    locationViewModel: LocationViewModel,
    onConfirmOrder: () -> Unit,
    onGoToCatalog: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val shippingCost by locationViewModel.shippingCost.collectAsState()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                locationViewModel.getDeviceLocation(context)
            } else {
                // Handle permission denial
                locationViewModel.setLocationEnabled(false, context)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (cartState.items.isEmpty()) {
            EmptyCartView(onGoToCatalog = onGoToCatalog)
        } else {
            Text("Tu Carrito", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartState.items) { item ->
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
                                IconButton(onClick = { cartViewModel.decrease(item.product.id) }) {
                                    Text("-", style = MaterialTheme.typography.titleLarge)
                                }
                                Text("${item.qty}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = { cartViewModel.increase(item.product.id) }) {
                                    Text("+", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Resumen y costo de envío ---
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Costo de envío a domicilio", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = locationViewModel.locationEnabled,
                        onCheckedChange = {
                            locationViewModel.setLocationEnabled(it, context)
                            if (it) {
                                if (!locationViewModel.hasLocationPermission(context)) {
                                    locationPermissionLauncher.launch(arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ))
                                }
                            }
                        }
                    )
                }

                if (locationViewModel.locationEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Costo de envío:")
                        Text("$${shippingCost.toInt()} CLP")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                val finalTotal = cartState.total + shippingCost

                Text(
                    "Total: $${finalTotal.toInt()} CLP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { cartViewModel.confirmOrder(onConfirmOrder) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("Confirmar Pedido", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(onGoToCatalog: () -> Unit) {
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
}
