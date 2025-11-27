package com.example.app_verduras.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.app_verduras.R
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.LocationViewModel
import com.example.app_verduras.viewmodel.OrderProcessingState
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Calendar

private fun isDateValid(dateStr: String): Boolean {
    if (dateStr.length != 10) return false
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        sdf.isLenient = false
        val enteredDate = sdf.parse(dateStr)

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
        val today = calendar.time

        !enteredDate.before(today)
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    locationViewModel: LocationViewModel
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val orderState by cartViewModel.orderProcessingState.collectAsState()
    val shippingCost by locationViewModel.shippingCost.collectAsState()
    val userAddress by cartViewModel.userAddress.collectAsState()
    val isHomeDelivery by locationViewModel.locationEnabled // <-- Usamos el estado del ViewModel

    var deliveryAddress by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var selectedStore by remember { mutableStateOf<String?>(null) }

    val isDateValid by remember(deliveryDate) { derivedStateOf { isDateValid(deliveryDate) } }

    val context = LocalContext.current

    val stores = listOf("Tienda Principal - Av. Siempreviva 742", "Sucursal del Centro - Calle Falsa 123", "Tienda del Sur - Bv. de los Sueños Rotos")

    // --- Effects ---
    LaunchedEffect(Unit) {
        // Calcula el costo de envío inicial al entrar a la pantalla
        if (isHomeDelivery) {
            locationViewModel.getDeviceLocation(context)
        }
    }

    LaunchedEffect(userAddress, isHomeDelivery) {
        if (isHomeDelivery && userAddress != null) {
            deliveryAddress = userAddress!!
        } else if (!isHomeDelivery) {
            deliveryAddress = ""
        }
    }

    LaunchedEffect(orderState) {
        if (orderState is OrderProcessingState.Success) {
            navController.navigate("confirmation") {
                popUpTo(navController.graph.startDestinationId)
            }
        }
    }

    val finalAddress = if (isHomeDelivery) deliveryAddress else selectedStore
    val isReadyToOrder = (isHomeDelivery && deliveryAddress.isNotBlank() && isDateValid) || (!isHomeDelivery && selectedStore != null && isDateValid)

    val totalAmount = cartState.total + shippingCost

    if (orderState is OrderProcessingState.Processing) {
        ProcessingOrderScreen()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Resumen del Pedido", style = MaterialTheme.typography.headlineSmall) }

            items(cartState.items) { 
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${it.qty}x ${it.product.nombre}")
                    Text("$${(it.qty * it.product.precio).toInt()} CLP")
                }
             }

            item { DeliverySummary(cartState.total, shippingCost, isHomeDelivery) }

            item {
                Text("Detalles de Entrega", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Envío a domicilio")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = isHomeDelivery,
                        onCheckedChange = { locationViewModel.setLocationEnabled(it, context) } // <-- Conectado al ViewModel
                    )
                }
            }

            if (isHomeDelivery) {
                item { HomeDeliverySection(deliveryAddress, { deliveryAddress = it }, locationViewModel) }
            } else {
                item { StorePickupSection(stores, selectedStore, { selectedStore = it }) }
            }

            item {
                OutlinedTextField(
                    value = deliveryDate,
                    onValueChange = { deliveryDate = it },
                    label = { Text("Fecha de entrega (dd/mm/aaaa)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = deliveryDate.isNotBlank() && !isDateValid,
                    supportingText = { if (deliveryDate.isNotBlank() && !isDateValid) Text("La fecha no puede ser anterior a hoy.") }
                )
            }

            item {
                Button(
                    onClick = { 
                        cartViewModel.confirmOrder(
                            deliveryAddress = finalAddress!!,
                            deliveryDate = deliveryDate,
                            finalTotal = totalAmount,
                            region = if (isHomeDelivery) "Región Metropolitana" else null,
                            comuna = null,
                            comentarios = null
                        )
                    },
                    enabled = isReadyToOrder,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("Finalizar Pedido")
                }

                if (orderState is OrderProcessingState.Error) {
                    Text((orderState as OrderProcessingState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = { cartViewModel.dismissError() }) { Text("Reintentar") }
                }
            }
        }
    }
}

@Composable
private fun DeliverySummary(subtotal: Double, shippingCost: Double, isHomeDelivery: Boolean) {
    Divider(modifier = Modifier.padding(vertical = 8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Subtotal", fontWeight = FontWeight.SemiBold)
        Text("$${subtotal.toInt()} CLP", fontWeight = FontWeight.SemiBold)
    }
    if (isHomeDelivery) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Costo de envío")
            Text("$${shippingCost.toInt()} CLP")
        }
    }
    Divider(modifier = Modifier.padding(vertical = 8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val totalAmount = subtotal + if(isHomeDelivery) shippingCost else 0.0
        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("$${totalAmount.toInt()} CLP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun HomeDeliverySection(address: String, onAddressChange: (String) -> Unit, locationViewModel: LocationViewModel) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) onAddressChange("Lat: ${location.latitude}, Lon: ${location.longitude}")
            }
        }
    }

    OutlinedTextField(
        value = address,
        onValueChange = onAddressChange,
        label = { Text("Dirección de entrega") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = {
                if (locationViewModel.hasLocationPermission(context)) {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) onAddressChange("Lat: ${location.latitude}, Lon: ${location.longitude}")
                    }
                } else {
                    locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            }) {
                Icon(Icons.Default.MyLocation, contentDescription = "Usar mi ubicación")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorePickupSection(stores: List<String>, selectedStore: String?, onStoreSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedStore ?: "Selecciona una tienda",
            onValueChange = {},
            readOnly = true,
            label = { Text("Tienda para retirar") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            stores.forEach { store ->
                DropdownMenuItem(text = { Text(store) }, onClick = { onStoreSelected(store); expanded = false })
            }
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
