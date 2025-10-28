
package com.example.app_verduras.ui.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.R
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.LocationViewModel
import com.example.app_verduras.viewmodel.OrderProcessingState
import com.example.app_verduras.viewmodel.CartState
import com.example.app_verduras.viewmodel.CartItem

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    locationViewModel: LocationViewModel,
    onConfirmOrder: () -> Unit,
    onGoToCatalog: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val orderState by cartViewModel.orderProcessingState.collectAsState()

    // --- Navigation Effect ---
    LaunchedEffect(orderState) {
        if (orderState is OrderProcessingState.Success) {
            onConfirmOrder()
        }
    }

    // --- Main Screen Logic ---
    when (orderState) {
        is OrderProcessingState.Processing -> {
            ProcessingOrderScreen()
        }
        else -> { // Handles Idle, Success, and Error for the main content view
            if (cartState.items.isEmpty()) {
                EmptyCartView(onGoToCatalog = onGoToCatalog)
            } else {
                FullCartView(cartViewModel, locationViewModel)
            }
        }
    }

    // --- Error Dialog Overlay ---
    // This will show on top of the cart view if an error occurs.
    if (orderState is OrderProcessingState.Error) {
        ErrorDialog(
            message = (orderState as OrderProcessingState.Error).message,
            onDismiss = { cartViewModel.dismissError() } // Dismisses the error state
        )
    }
}

@Composable
fun FullCartView(
    cartViewModel: CartViewModel,
    locationViewModel: LocationViewModel
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val orderState by cartViewModel.orderProcessingState.collectAsState()
    val shippingCost by locationViewModel.shippingCost.collectAsState()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                locationViewModel.getDeviceLocation(context)
            } else {
                locationViewModel.setLocationEnabled(false, context)
            }
        }
    )

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.product.imagen)
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                            error = painterResource(id = android.R.drawable.ic_menu_report_image),
                            contentDescription = item.product.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.product.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("$${item.product.precio} CLP", style = MaterialTheme.typography.bodyLarge)
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
                        IconButton(onClick = { cartViewModel.remove(item.product.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Shipping and Total ---
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
                onClick = { cartViewModel.confirmOrder() },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                enabled = orderState !is OrderProcessingState.Processing
            ) {
                Text("Confirmar Pedido", style = MaterialTheme.typography.titleMedium)
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

@Composable
fun ProcessingOrderScreen() {
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.orange_skating))

    LaunchedEffect(compositionResult.isSuccess, compositionResult.error) {
        if(compositionResult.error != null) {
            Log.e("LottieError", "Animation failed to load", compositionResult.error)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                enabled = true,
                onClick = {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                compositionResult.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(100.dp),
                        color = Color.White,
                        strokeWidth = 6.dp
                    )
                }
                compositionResult.isSuccess -> {
                    LottieAnimation(
                        composition = compositionResult.value,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(250.dp)
                    )
                }
                compositionResult.error != null -> {
                    Text(
                        text = "Error: La animación no pudo cargar.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Procesando tu pedido...",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- AlertDialog for Errors ---
@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Error al Procesar el Pedido") },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
