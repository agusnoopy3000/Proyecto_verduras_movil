package com.example.app_verduras.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.viewmodel.CartItem
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.CatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(viewModel: CatalogViewModel, cartViewModel: CartViewModel) {
    val state by viewModel.uiState.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()

    val cartItemsMap = cartState.items.associateBy { it.product.codigo }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter("file:///android_asset/img/huerto_hogar.jpeg"),
            contentDescription = "Fondo de huerto",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(color = Color.Black.copy(alpha = 0.6f))
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Catálogo de Verduras") },
                    actions = {
                        IconButton(onClick = { /* TODO: Navegar al carrito */ }) {
                            BadgedBox(
                                badge = {
                                    if (cartState.items.isNotEmpty()) {
                                        Badge {
                                            Text("${cartState.items.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Carrito de compras"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.3f),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    OutlinedTextField(
                        value = state.search,
                        onValueChange = { viewModel.updateSearch(it) },
                        label = { Text("Buscar producto...") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    CategoryDropdown(
                        categories = state.categories,
                        selected = state.selectedCategory,
                        onSelect = { viewModel.updateCategory(it) }
                    )
                }
                Spacer(Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.filteredProducts) { p ->
                        ProductCard(
                            product = p,
                            cartItem = cartItemsMap[p.codigo],
                            onAdd = { cartViewModel.addToCart(p.codigo) },
                            onIncrease = { cartViewModel.increase(p.codigo) },
                            onDecrease = { cartViewModel.decrease(p.codigo) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(categories: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.wrapContentWidth()
    ) {
        OutlinedTextField(
            value = selected ?: "Todas",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .widthIn(min = 160.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                // These are for the container, we want it to blend
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = { onSelect(null); expanded = false }
            )
            categories.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c) },
                    onClick = { onSelect(c); expanded = false }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Producto,
    cartItem: CartItem?,
    onAdd: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), // Increased height for better spacing
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = product.img ?: "file:///android_asset/img/default.png",
                contentDescription = product.nombre,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$${product.precio} CLP",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 3, // Allow a bit more text
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(50.dp) // Give description a fixed height
            )

            Spacer(modifier = Modifier.weight(1f)) // This pushes the button to the bottom

            // Button or counter
            if (cartItem == null || cartItem.qty == 0) {
                Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                    Text("Agregar")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onDecrease) {
                        Icon(Icons.Filled.Remove, "Quitar uno")
                    }
                    Text(
                        text = "${cartItem.qty}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onIncrease) {
                        Icon(Icons.Filled.Add, "Añadir uno")
                    }
                }
            }
        }
    }
}
