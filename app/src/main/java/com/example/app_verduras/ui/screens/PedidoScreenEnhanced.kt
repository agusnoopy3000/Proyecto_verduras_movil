// filepath: /Users/agustingarridosnoopy/Proyecto_verduras_movil/app/src/main/java/com/example/app_verduras/ui/screens/PedidoScreenNew.kt
package com.example.app_verduras.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.app_verduras.R
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.viewmodel.CartItem
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.LocationViewModel
import com.example.app_verduras.viewmodel.OrderProcessingState
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

// Constantes para cálculo de envío
private const val BASE_DELIVERY_COST = 2500.0
private const val COST_PER_KM = 350.0
private const val STORE_LAT = -33.4489 // Santiago centro
private const val STORE_LON = -70.6693

/**
 * Pantalla mejorada de Resumen de Pedido
 * - Calendario interactivo para fecha de entrega
 * - Integración con Google Maps para cálculo de envío
 * - UI moderna con resumen detallado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreenEnhanced(
    navController: NavController,
    cartViewModel: CartViewModel,
    locationViewModel: LocationViewModel
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val orderState by cartViewModel.orderProcessingState.collectAsState()
    val userAddress by cartViewModel.userAddress.collectAsState()
    val isHomeDelivery by locationViewModel.locationEnabled

    var deliveryAddress by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedStore by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var calculatedDistance by remember { mutableStateOf<Double?>(null) }
    var shippingCost by remember { mutableStateOf(0.0) }
    var userCoordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isCalculatingShipping by remember { mutableStateOf(false) }
    var deliveryNotes by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")) }

    val stores = listOf(
        StoreLocation("Tienda Principal", "Av. Providencia 1234", -33.4280, -70.6100),
        StoreLocation("Sucursal Las Condes", "Av. Apoquindo 5678", -33.4180, -70.5800),
        StoreLocation("Tienda Ñuñoa", "Av. Irarrázaval 3456", -33.4530, -70.5990)
    )

    // Calcular costo de envío cuando cambie la ubicación
    LaunchedEffect(userCoordinates, isHomeDelivery) {
        if (isHomeDelivery && userCoordinates != null) {
            isCalculatingShipping = true
            val (lat, lon) = userCoordinates!!
            val distance = calculateDistance(lat, lon, STORE_LAT, STORE_LON)
            calculatedDistance = distance
            shippingCost = calculateShippingCost(distance)
            isCalculatingShipping = false
        } else {
            shippingCost = 0.0
            calculatedDistance = null
        }
    }

    // Actualizar dirección desde la ubicación
    LaunchedEffect(userAddress, isHomeDelivery) {
        if (isHomeDelivery && userAddress != null) {
            deliveryAddress = userAddress!!
        }
    }

    // Navegar a confirmación cuando el pedido sea exitoso
    LaunchedEffect(orderState) {
        if (orderState is OrderProcessingState.Success) {
            navController.navigate("confirmation") {
                popUpTo(navController.graph.startDestinationId)
            }
        }
    }

    val selectedDateString = selectedDate?.let { dateFormatter.format(Date(it)) } ?: ""
    val isDateValid = selectedDate != null && selectedDate!! >= System.currentTimeMillis() - 86400000

    val finalAddress = if (isHomeDelivery) deliveryAddress else selectedStore
    val isReadyToOrder = (isHomeDelivery && deliveryAddress.isNotBlank() && isDateValid) || 
                         (!isHomeDelivery && selectedStore != null && isDateValid)

    val totalAmount = cartState.total + shippingCost

    // Mostrar pantalla de procesamiento
    if (orderState is OrderProcessingState.Processing) {
        ProcessingOrderScreen()
        return
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Solo permitir fechas desde hoy en adelante
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    return utcTimeMillis >= today
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        "Seleccionar fecha de entrega",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                headline = {
                    Text(
                        "¿Cuándo deseas recibir tu pedido?",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = HuertoHogarColors.TextSecondary
                    )
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HuertoHogarColors.Accent.copy(alpha = 0.3f),
                        HuertoHogarColors.Background
                    )
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            OrderHeader(itemCount = cartState.items.size)
        }

        // Lista de productos
        item {
            OrderItemsCard(items = cartState.items)
        }

        // Resumen de costos
        item {
            OrderSummaryCard(
                subtotal = cartState.total,
                shippingCost = shippingCost,
                distance = calculatedDistance,
                isHomeDelivery = isHomeDelivery,
                isCalculating = isCalculatingShipping
            )
        }

        // Tipo de entrega
        item {
            DeliveryTypeCard(
                isHomeDelivery = isHomeDelivery,
                onTypeChange = { enabled ->
                    locationViewModel.setLocationEnabled(enabled, context)
                    if (!enabled) {
                        shippingCost = 0.0
                        calculatedDistance = null
                    }
                }
            )
        }

        // Dirección o tienda
        if (isHomeDelivery) {
            item {
                DeliveryAddressCard(
                    address = deliveryAddress,
                    onAddressChange = { deliveryAddress = it },
                    onLocationClick = {
                        getDeviceLocationAndCalculate(context, locationViewModel) { lat, lon, address ->
                            userCoordinates = Pair(lat, lon)
                            deliveryAddress = address
                        }
                    },
                    distance = calculatedDistance,
                    locationViewModel = locationViewModel
                )
            }
        } else {
            item {
                StoreSelectionCard(
                    stores = stores,
                    selectedStore = selectedStore,
                    onStoreSelected = { selectedStore = it }
                )
            }
        }

        // Fecha de entrega con calendario
        item {
            DeliveryDateCard(
                selectedDate = selectedDateString,
                isValid = isDateValid || selectedDate == null,
                onClick = { showDatePicker = true }
            )
        }

        // Notas adicionales
        item {
            NotesCard(
                notes = deliveryNotes,
                onNotesChange = { deliveryNotes = it }
            )
        }

        // Botón de confirmar
        item {
            ConfirmOrderButton(
                enabled = isReadyToOrder,
                total = totalAmount,
                onClick = {
                    cartViewModel.confirmOrder(
                        deliveryAddress = finalAddress!!,
                        deliveryDate = selectedDateString,
                        finalTotal = totalAmount,
                        region = if (isHomeDelivery) "Región Metropolitana" else null,
                        comuna = null,
                        comentarios = deliveryNotes.ifBlank { null }
                    )
                }
            )

            if (orderState is OrderProcessingState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                ErrorCard(
                    message = (orderState as OrderProcessingState.Error).message,
                    onRetry = { cartViewModel.dismissError() }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun OrderHeader(itemCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = HuertoHogarColors.Primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                "Resumen del Pedido",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$itemCount producto${if (itemCount != 1) "s" else ""} en tu carrito",
                style = MaterialTheme.typography.bodyMedium,
                color = HuertoHogarColors.TextSecondary
            )
        }
    }
}

@Composable
private fun OrderItemsCard(items: List<CartItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Inventory,
                    contentDescription = null,
                    tint = HuertoHogarColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Productos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen del producto
                    AsyncImage(
                        model = item.product.imagen,
                        contentDescription = item.product.nombre,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HuertoHogarColors.Accent)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.product.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${item.qty} x $${item.product.precio.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HuertoHogarColors.TextSecondary
                        )
                    }

                    Text(
                        "$${(item.qty * item.product.precio).toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = HuertoHogarColors.Primary
                    )
                }

                if (item != items.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = HuertoHogarColors.Accent.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    subtotal: Double,
    shippingCost: Double,
    distance: Double?,
    isHomeDelivery: Boolean,
    isCalculating: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "$${subtotal.toInt()} CLP",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isHomeDelivery) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Costo de envío", style = MaterialTheme.typography.bodyMedium)
                        if (distance != null) {
                            Text(
                                "📍 ${String.format("%.1f", distance)} km desde la tienda",
                                style = MaterialTheme.typography.labelSmall,
                                color = HuertoHogarColors.TextSecondary
                            )
                        }
                    }
                    if (isCalculating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "$${shippingCost.toInt()} CLP",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$${(subtotal + if (isHomeDelivery) shippingCost else 0.0).toInt()} CLP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HuertoHogarColors.Primary
                )
            }
        }
    }
}

@Composable
private fun DeliveryTypeCard(
    isHomeDelivery: Boolean,
    onTypeChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = HuertoHogarColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Tipo de Entrega",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Opción: Envío a domicilio
                DeliveryOptionChip(
                    selected = isHomeDelivery,
                    icon = Icons.Default.Home,
                    title = "A domicilio",
                    subtitle = "Recibe en tu casa",
                    onClick = { onTypeChange(true) },
                    modifier = Modifier.weight(1f)
                )

                // Opción: Retiro en tienda
                DeliveryOptionChip(
                    selected = !isHomeDelivery,
                    icon = Icons.Default.Store,
                    title = "Retiro en tienda",
                    subtitle = "Sin costo de envío",
                    onClick = { onTypeChange(false) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DeliveryOptionChip(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.border(
                    2.dp,
                    HuertoHogarColors.Primary,
                    RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                HuertoHogarColors.Primary.copy(alpha = 0.1f) 
            else 
                HuertoHogarColors.Background
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) HuertoHogarColors.Primary else HuertoHogarColors.TextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) HuertoHogarColors.Primary else HuertoHogarColors.OnBackground
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = HuertoHogarColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DeliveryAddressCard(
    address: String,
    onAddressChange: (String) -> Unit,
    onLocationClick: () -> Unit,
    distance: Double?,
    locationViewModel: LocationViewModel
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            onLocationClick()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = HuertoHogarColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Dirección de Entrega",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("Dirección completa") },
                placeholder = { Text("Ej: Av. Providencia 1234, Santiago") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (locationViewModel.hasLocationPermission(context)) {
                                onLocationClick()
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Usar mi ubicación actual",
                            tint = HuertoHogarColors.Primary
                        )
                    }
                }
            )

            if (distance != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            HuertoHogarColors.Accent.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        tint = HuertoHogarColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Distancia calculada: ${String.format("%.1f", distance)} km",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Tiempo estimado: ${(distance * 3).toInt()} - ${(distance * 5).toInt()} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = HuertoHogarColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreSelectionCard(
    stores: List<StoreLocation>,
    selectedStore: String?,
    onStoreSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Store,
                    contentDescription = null,
                    tint = HuertoHogarColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Seleccionar Tienda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedStore ?: "Selecciona una tienda",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tienda para retiro") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    stores.forEach { store ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(store.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        store.address,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HuertoHogarColors.TextSecondary
                                    )
                                }
                            },
                            onClick = {
                                onStoreSelected(store.address)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    tint = HuertoHogarColors.Primary
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "💡 El retiro en tienda no tiene costo adicional",
                style = MaterialTheme.typography.labelSmall,
                color = HuertoHogarColors.TextSecondary
            )
        }
    }
}

@Composable
private fun DeliveryDateCard(
    selectedDate: String,
    isValid: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = HuertoHogarColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Fecha de Entrega",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedCard(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (selectedDate.isNotBlank()) 
                        HuertoHogarColors.Primary.copy(alpha = 0.05f) 
                    else 
                        Color.Transparent
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        colors = if (!isValid && selectedDate.isNotBlank())
                            listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error)
                        else
                            listOf(HuertoHogarColors.Primary.copy(alpha = 0.5f), HuertoHogarColors.Primary)
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = HuertoHogarColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (selectedDate.isNotBlank()) selectedDate else "Seleccionar fecha",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedDate.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                            color = if (selectedDate.isNotBlank()) 
                                HuertoHogarColors.OnBackground 
                            else 
                                HuertoHogarColors.TextSecondary
                        )
                        Text(
                            "Toca para abrir el calendario",
                            style = MaterialTheme.typography.labelSmall,
                            color = HuertoHogarColors.TextSecondary
                        )
                    }
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = HuertoHogarColors.TextSecondary
                    )
                }
            }

            if (!isValid && selectedDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "⚠️ La fecha debe ser hoy o posterior",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun NotesCard(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Notes,
                    contentDescription = null,
                    tint = HuertoHogarColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Notas adicionales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    " (opcional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = HuertoHogarColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Ej: Dejar en conserjería, timbre no funciona...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun ConfirmOrderButton(
    enabled: Boolean,
    total: Double,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HuertoHogarColors.Primary,
            disabledContainerColor = HuertoHogarColors.Primary.copy(alpha = 0.4f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Confirmar Pedido • $${total.toInt()} CLP",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Error al procesar",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

// Data class para tiendas
data class StoreLocation(
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double
)

// Función para calcular distancia usando fórmula de Haversine
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}

// Función para calcular costo de envío
private fun calculateShippingCost(distanceKm: Double): Double {
    return BASE_DELIVERY_COST + (distanceKm * COST_PER_KM)
}

// Función para obtener ubicación y calcular
@SuppressLint("MissingPermission")
private fun getDeviceLocationAndCalculate(
    context: Context,
    locationViewModel: LocationViewModel,
    onResult: (Double, Double, String) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val lat = location.latitude
            val lon = location.longitude
            
            // Geocodificación inversa para obtener dirección
            try {
                val geocoder = Geocoder(context, Locale("es", "CL"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val address = addresses?.firstOrNull()?.let { addr ->
                    buildString {
                        addr.thoroughfare?.let { append(it) }
                        addr.subThoroughfare?.let { append(" $it") }
                        addr.locality?.let { append(", $it") }
                    }
                } ?: "Lat: $lat, Lon: $lon"
                
                onResult(lat, lon, address)
            } catch (e: Exception) {
                Log.e("Geocoder", "Error: ${e.message}")
                onResult(lat, lon, "Lat: $lat, Lon: $lon")
            }
        }
    }
}
