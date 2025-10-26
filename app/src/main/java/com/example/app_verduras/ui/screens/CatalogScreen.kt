package com.example.app_verduras.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.viewmodel.CartItem
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.CatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(viewModel: CatalogViewModel, cartViewModel: CartViewModel) {
    val state by viewModel.uiState.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()
    val cartItemsMap = cartState.items.associateBy { it.product.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // --- Título y Filtros ---
        Text("Nuestro Catálogo", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.search,
                onValueChange = { viewModel.updateSearch(it) },
                label = { Text("Buscar...") },
                modifier = Modifier.weight(1f)
            )
            CategoryDropdown(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelect = { viewModel.updateCategory(it) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Grid de productos ---
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.filteredProducts) { p ->
                ProductCard(
                    product = p,
                    cartItem = cartItemsMap[p.id],
                    onAdd = { cartViewModel.addToCart(p.id) },
                    onIncrease = { cartViewModel.increase(p.id) },
                    onDecrease = { cartViewModel.decrease(p.id) }
                )
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
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().width(150.dp)
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
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            AsyncImage(
                model = product.imagen ?: "file:///android_asset/img/default.png",
                contentDescription = product.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = product.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.descripcion ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 40.dp)
                )

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "$${product.precio} CLP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (cartItem == null || cartItem.qty == 0) {
                            Button(
                                onClick = onAdd,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50) // Green
                                )
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = "Agregar al carrito")
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedIconButton(
                                    onClick = onDecrease,
                                    modifier = Modifier.size(28.dp),
                                    border = BorderStroke(1.dp, Color.Red)
                                ) {
                                    Icon(Icons.Filled.Remove, "Quitar uno", tint = Color.Red)
                                }
                                Text(
                                    text = "${cartItem.qty}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = onIncrease,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFF4CAF50), shape = CircleShape),
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Filled.Add, "Añadir uno")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
