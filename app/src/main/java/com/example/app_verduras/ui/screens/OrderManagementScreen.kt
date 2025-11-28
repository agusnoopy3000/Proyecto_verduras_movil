package com.example.app_verduras.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.repository.PedidoRepository
import com.example.app_verduras.ui.components.*
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.viewmodel.OrderManagementViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    viewModel: OrderManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProducts: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToDocuments: () -> Unit = {},
    onLogout: () -> Unit
) {
    val pedidos by viewModel.pedidos.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    var pedidoToEdit by remember { mutableStateOf<Pedido?>(null) }
    val toastState = rememberToastState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is OrderManagementViewModel.UiEvent.ShowSnackbar -> {
                    toastState.showSuccess(event.message)
                }
                is OrderManagementViewModel.UiEvent.DismissEditModal -> {
                    pedidoToEdit = null
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                "Gestionar Pedidos",
                                fontWeight = FontWeight.Bold
                            )
                            // Mostrar estado de sincronización
                            Text(
                                text = when (syncStatus) {
                                    is com.example.app_verduras.repository.PedidoRepository.SyncStatus.Syncing -> "Sincronizando..."
                                    is com.example.app_verduras.repository.PedidoRepository.SyncStatus.Success -> "✓ Sincronizado"
                                    is com.example.app_verduras.repository.PedidoRepository.SyncStatus.Error -> "⚠ Error de sync"
                                    else -> "Firebase en tiempo real"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver al menú"
                            )
                        }
                    },
                    actions = {
                        // Botón de refresh
                        IconButton(
                            onClick = { viewModel.refreshPedidos() },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, "Actualizar pedidos")
                            }
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, "Cerrar Sesión")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = HuertoHogarColors.Primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                AdminBottomNavBar(
                    currentSection = AdminSection.ORDERS,
                    onNavigateToMenu = onNavigateBack,
                    onNavigateToProducts = onNavigateToProducts,
                    onNavigateToUsers = onNavigateToUsers,
                    onNavigateToOrders = { /* Ya estamos aquí */ },
                    onNavigateToDocuments = onNavigateToDocuments
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                HuertoHogarColors.Primary.copy(alpha = 0.08f),
                                HuertoHogarColors.Accent.copy(alpha = 0.5f),
                                HuertoHogarColors.Background
                            )
                        )
                    )
            ) {
                // Resumen de pedidos por estado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = HuertoHogarColors.Primary
                            )
                            Text(
                                "Resumen de Pedidos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val pendientes = pedidos.count { it.estado == "Pendiente" }
                            val enPreparacion = pedidos.count { it.estado == "En preparación" }
                            val enviados = pedidos.count { it.estado == "Enviado" }
                            val entregados = pedidos.count { it.estado == "Entregado" }
                            
                            StatusBadge("Pendiente", pendientes, Color(0xFFFF9800))
                            StatusBadge("Preparando", enPreparacion, Color(0xFF2196F3))
                            StatusBadge("Enviado", enviados, Color(0xFF03A9F4))
                            StatusBadge("Entregado", entregados, Color(0xFF4CAF50))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pedidos) { pedido ->
                        OrderItem(pedido = pedido, onEditClick = { pedidoToEdit = pedido })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }

        // Toast animado en la parte superior
        AnimatedSuccessToast(
            toastData = toastState.currentToast,
            onDismiss = { toastState.dismiss() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )
    }

    pedidoToEdit?.let { pedido ->
        EditOrderModal(
            pedido = pedido,
            onDismiss = { pedidoToEdit = null },
            onSave = {
                viewModel.actualizarPedido(it)
            }
        )
    }
}

@Composable
private fun StatusBadge(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = HuertoHogarColors.TextSecondary
        )
    }
}

@Composable
fun OrderItem(pedido: Pedido, onEditClick: () -> Unit) {
    val statusColor = when (pedido.estado) {
        "Entregado" -> Color(0xFF4CAF50)
        "Enviado" -> Color(0xFF03A9F4)
        "En preparación" -> Color(0xFF2196F3)
        "Pendiente" -> Color(0xFFFF9800)
        "Cancelado" -> Color.Gray
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val statusIcon = when (pedido.estado) {
        "Entregado" -> Icons.Default.CheckCircle
        "Enviado" -> Icons.Default.LocalShipping
        "En preparación" -> Icons.Default.Restaurant
        "Pendiente" -> Icons.Default.Schedule
        "Cancelado" -> Icons.Default.Cancel
        else -> Icons.Default.Receipt
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono de estado
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pedido #${pedido.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = pedido.estado,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = pedido.userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HuertoHogarColors.TextSecondary
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = HuertoHogarColors.TextSecondary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = pedido.fechaEntrega ?: "Sin fecha",
                            style = MaterialTheme.typography.bodySmall,
                            color = HuertoHogarColors.TextSecondary
                        )
                    }
                    Text(
                        text = "$${String.format("%.0f", pedido.total)} CLP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HuertoHogarColors.Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FilledTonalIconButton(
                onClick = onEditClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = HuertoHogarColors.Secondary.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Cambiar estado",
                    tint = HuertoHogarColors.Secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderModal(pedido: Pedido, onDismiss: () -> Unit, onSave: (Pedido) -> Unit) {
    var estado by remember { mutableStateOf(pedido.estado) }
    var expanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val estadosPosibles = listOf("Pendiente", "En preparación", "Enviado", "Entregado", "Cancelado")
    
    val statusColor = when (estado) {
        "Entregado" -> Color(0xFF4CAF50)
        "Enviado" -> Color(0xFF03A9F4)
        "En preparación" -> Color(0xFF2196F3)
        "Pendiente" -> Color(0xFFFF9800)
        "Cancelado" -> Color.Gray
        else -> HuertoHogarColors.Primary
    }
    
    val statusIcon = when (estado) {
        "Entregado" -> Icons.Default.CheckCircle
        "Enviado" -> Icons.Default.LocalShipping
        "En preparación" -> Icons.Default.Restaurant
        "Pendiente" -> Icons.Default.Schedule
        "Cancelado" -> Icons.Default.Cancel
        else -> Icons.Default.Receipt
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = { 
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Pedido #${pedido.id}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    pedido.userEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = HuertoHogarColors.TextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info del pedido
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = HuertoHogarColors.Accent.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Total del pedido",
                                style = MaterialTheme.typography.labelSmall,
                                color = HuertoHogarColors.TextSecondary
                            )
                            Text(
                                "$${String.format("%.0f", pedido.total)} CLP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HuertoHogarColors.Primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Fecha entrega",
                                style = MaterialTheme.typography.labelSmall,
                                color = HuertoHogarColors.TextSecondary
                            )
                            Text(
                                pedido.fechaEntrega ?: "Sin fecha",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Text(
                    "Cambiar estado a:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(
                                statusIcon,
                                contentDescription = null,
                                tint = statusColor
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = statusColor,
                            unfocusedBorderColor = statusColor.copy(alpha = 0.5f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true)
                    ) {
                        estadosPosibles.forEach { seleccion ->
                            val itemColor = when (seleccion) {
                                "Entregado" -> Color(0xFF4CAF50)
                                "Enviado" -> Color(0xFF03A9F4)
                                "En preparación" -> Color(0xFF2196F3)
                                "Pendiente" -> Color(0xFFFF9800)
                                "Cancelado" -> Color.Gray
                                else -> Color.Black
                            }
                            val itemIcon = when (seleccion) {
                                "Entregado" -> Icons.Default.CheckCircle
                                "Enviado" -> Icons.Default.LocalShipping
                                "En preparación" -> Icons.Default.Restaurant
                                "Pendiente" -> Icons.Default.Schedule
                                "Cancelado" -> Icons.Default.Cancel
                                else -> Icons.Default.Receipt
                            }
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            itemIcon,
                                            contentDescription = null,
                                            tint = itemColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            seleccion,
                                            color = if (seleccion == estado) itemColor else Color.Unspecified,
                                            fontWeight = if (seleccion == estado) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    estado = seleccion
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    isSaving = true
                    onSave(pedido.copy(estado = estado)) 
                },
                enabled = !isSaving && estado != pedido.estado,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HuertoHogarColors.Primary
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
