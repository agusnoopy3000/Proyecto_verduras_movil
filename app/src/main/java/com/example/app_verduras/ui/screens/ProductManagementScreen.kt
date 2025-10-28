package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.viewmodel.ProductManagementViewModel
import com.example.app_verduras.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    viewModel: ProductManagementViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    var productToEdit by remember { mutableStateOf<Producto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = event.message)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gestionar Productos") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, "Cerrar Sesión")
                        }
                    }
                )
            }
        ) { paddingValues ->

            productToEdit?.let { currentProduct ->
                EditProductModal(
                    product = currentProduct,
                    onDismiss = { productToEdit = null },
                    onSave = { updatedProduct ->
                        viewModel.updateProduct(updatedProduct)
                        productToEdit = null
                    }
                )
            }

            if (products.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No hay productos para mostrar.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductManagementItem(
                            product = product,
                            onEditClick = { productToEdit = product }
                        )
                        Divider()
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            snackbar = { snackbarData ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50) // Verde
                    )
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = Color.White
                    )
                }
            }
        )
    }
}

@Composable
fun ProductManagementItem(
    product: Producto,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = product.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = "$${product.precio} CLP - Stock: ${product.stock}", style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, "Editar Producto")
        }
    }
}

@Composable
fun EditProductModal(
    product: Producto,
    onDismiss: () -> Unit,
    onSave: (Producto) -> Unit
) {
    var nombre by remember { mutableStateOf(product.nombre) }
    var precio by remember { mutableStateOf(product.precio.toString()) }
    var stock by remember { mutableStateOf(product.stock.toString()) }
    var descripcion by remember { mutableStateOf(product.descripcion ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Editar Producto", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    TextButton(
                        onClick = {
                            val updatedProduct = product.copy(
                                nombre = nombre,
                                precio = precio.toDoubleOrNull() ?: product.precio,
                                stock = stock.toIntOrNull() ?: product.stock,
                                descripcion = descripcion
                            )
                            onSave(updatedProduct)
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
