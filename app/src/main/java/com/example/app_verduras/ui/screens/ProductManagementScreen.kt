package com.example.app_verduras.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.ui.components.*
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.viewmodel.ProductManagementViewModel
import com.example.app_verduras.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    viewModel: ProductManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToUsers: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToDocuments: () -> Unit = {},
    onLogout: () -> Unit
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    var productToEdit by remember { mutableStateOf<Producto?>(null) }
    val toastState = rememberToastState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    toastState.showSuccess(event.message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Gestionar Productos",
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver al menú")
                        }
                    },
                    actions = {
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
                    currentSection = AdminSection.PRODUCTS,
                    onNavigateToMenu = onNavigateBack,
                    onNavigateToProducts = { /* Ya estamos aquí */ },
                    onNavigateToUsers = onNavigateToUsers,
                    onNavigateToOrders = onNavigateToOrders,
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
                // Contador de productos
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = HuertoHogarColors.Primary
                            )
                            Text(
                                "Total de productos",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = HuertoHogarColors.Primary
                        ) {
                            Text(
                                text = "${products.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = HuertoHogarColors.TextSecondary
                            )
                            Text(
                                "No hay productos para mostrar",
                                color = HuertoHogarColors.TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            ProductManagementItem(
                                product = product,
                                onEditClick = { productToEdit = product }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
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
}

@Composable
fun ProductManagementItem(
    product: Producto,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de producto
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = HuertoHogarColors.Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocalFlorist,
                        contentDescription = null,
                        tint = HuertoHogarColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "$${product.precio.toInt()} CLP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HuertoHogarColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Stock: ${product.stock}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (product.stock < 10) Color(0xFFE53935) else HuertoHogarColors.TextSecondary
                    )
                }
            }
            
            FilledTonalIconButton(
                onClick = onEditClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = HuertoHogarColors.Secondary.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar Producto",
                    tint = HuertoHogarColors.Secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var isSaving by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header con icono
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = HuertoHogarColors.Primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = HuertoHogarColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        "Editar Producto",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Label, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuertoHogarColors.Primary,
                        focusedLabelColor = HuertoHogarColors.Primary
                    )
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio (CLP)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuertoHogarColors.Primary,
                        focusedLabelColor = HuertoHogarColors.Primary
                    )
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock disponible") },
                    leadingIcon = { Icon(Icons.Default.Inventory2, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuertoHogarColors.Primary,
                        focusedLabelColor = HuertoHogarColors.Primary
                    )
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuertoHogarColors.Primary,
                        focusedLabelColor = HuertoHogarColors.Primary
                    )
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isSaving = true
                            val updatedProduct = product.copy(
                                nombre = nombre,
                                precio = precio.toDoubleOrNull() ?: product.precio,
                                stock = stock.toIntOrNull() ?: product.stock,
                                descripcion = descripcion
                            )
                            onSave(updatedProduct)
                        },
                        enabled = !isSaving,
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
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
