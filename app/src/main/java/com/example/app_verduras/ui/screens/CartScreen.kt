package com.example.app_verduras.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.app_verduras.R
import com.example.app_verduras.viewmodel.CartItem
import com.example.app_verduras.viewmodel.CartState
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    locationViewModel: LocationViewModel,
    onGoToCatalog: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val isProcessing by cartViewModel.isProcessingOrder.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Carrito", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onGoToCatalog) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (cartState.items.isEmpty()) {
                EmptyCartView(onGoToCatalog)
            } else {
                CartContent(cartState, cartViewModel, locationViewModel)
            }

            if (isProcessing) {
                ProcessingOverlay()
            }
        }
    }
}

@Composable
fun ProcessingOverlay() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.orange_skating))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(200.dp)
        )
    }
}

@Composable
fun EmptyCartView(onGoToCatalog: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_cart))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(300.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tu carrito está vacío", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("¡Añade productos para empezar a comprar!", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGoToCatalog) {
            Text("Ir al Catálogo")
        }
    }
}

@Composable
fun CartContent(
    cartState: CartState,
    cartViewModel: CartViewModel,
    locationViewModel: LocationViewModel
) {
    val locationState by locationViewModel.locationState.collectAsState()
    var shippingEnabled by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            locationViewModel.onPermissionResult(isGranted)
        }
    )

    LaunchedEffect(locationState.isPermissionRequestInProgress) {
        if (locationState.isPermissionRequestInProgress) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cartState.items) {
                CartItemView(it, cartViewModel)
            }
        }

        // --- Sección de Envío y Total ---
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Envío a domicilio", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = shippingEnabled,
                    onCheckedChange = { isChecked ->
                        shippingEnabled = isChecked
                        if (isChecked) {
                            if (!locationState.hasPermission) {
                                locationViewModel.requestLocationPermission()
                            } else {
                                locationViewModel.fetchLastLocation()
                            }
                        }
                    },
                    enabled = cartState.items.isNotEmpty()
                )
            }

            if (shippingEnabled && locationState.hasPermission) {
                Text(
                    text = "Costo de envío: $${String.format("%.2f", locationState.shippingCost)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            val totalConEnvio = cartState.total + if (shippingEnabled && locationState.hasPermission) locationState.shippingCost else 0.0
            Text(
                text = "Total: $${String.format("%.2f", totalConEnvio)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { cartViewModel.confirmOrder() },
                modifier = Modifier.fillMaxWidth(),
                enabled = cartState.items.isNotEmpty()
            ) {
                Text("Confirmar Pedido", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CartItemView(cartItem: CartItem, viewModel: CartViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(cartItem.product.imagen),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.product.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("$${String.format("%.2f", cartItem.product.precio)}", color = Color.Gray)
                Text("Subtotal: $${String.format("%.2f", cartItem.product.precio * cartItem.qty)}", fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.decrease(cartItem.product.id) },
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, Color.Red, CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Quitar", tint = Color.Red)
                }
                Text("${cartItem.qty}", modifier = Modifier.padding(horizontal = 8.dp), fontSize = 18.sp)
                IconButton(
                    onClick = { viewModel.increase(cartItem.product.id) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                }
            }
        }
    }
}
