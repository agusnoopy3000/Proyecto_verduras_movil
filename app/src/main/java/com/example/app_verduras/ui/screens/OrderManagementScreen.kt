package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.viewmodel.OrderManagementViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    viewModel: OrderManagementViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val pedidos by viewModel.pedidos.collectAsStateWithLifecycle(initialValue = emptyList())
    var pedidoToEdit by remember { mutableStateOf<Pedido?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is OrderManagementViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
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
                    title = { Text("Gestionar Pedidos") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar Sesión")
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pedidos) { pedido ->
                    OrderItem(pedido = pedido, onEditClick = { pedidoToEdit = pedido })
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
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
fun OrderItem(pedido: Pedido, onEditClick: () -> Unit) {
    val statusColor = when (pedido.estado) {
        "Entregado" -> Color(0xFF4CAF50) // Green
        "Enviado" -> Color(0xFF03A9F4) // Light Blue
        "En preparación" -> Color(0xFF2196F3) // Blue
        "Pendiente" -> Color(0xFFFF9800) // Orange
        "Cancelado" -> Color.Gray
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text("Pedido #${pedido.id} - ${pedido.userEmail}") },
            supportingContent = {
                Column {
                    Text("Fecha: ${pedido.fechaEntrega}")
                    Text("Total: $${String.format("%.0f", pedido.total)} CLP")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Estado: ")
                        Text(
                            text = pedido.estado,
                            style = MaterialTheme.typography.bodyLarge,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            trailingContent = {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Pedido")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderModal(pedido: Pedido, onDismiss: () -> Unit, onSave: (Pedido) -> Unit) {
    var estado by remember { mutableStateOf(pedido.estado) }
    var expanded by remember { mutableStateOf(false) }
    val estadosPosibles = listOf("Pendiente", "En preparación", "Enviado", "Entregado", "Cancelado")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Estado del Pedido") },
        text = {
            Column(Modifier.fillMaxWidth()) { // Wrap in column for alignment
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth() // Make the box fill the width
                ) {
                    TextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(), // Make the text field fill the width
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true) // Make dropdown match width
                    ) {
                        estadosPosibles.forEach { seleccion ->
                            DropdownMenuItem(
                                text = { Text(seleccion) },
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
                onClick = { onSave(pedido.copy(estado = estado)) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Green color
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar", color = Color.Red)
            }
        }
    )
}
