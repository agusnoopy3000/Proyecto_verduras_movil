package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.viewmodel.CartItem
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.CatalogViewModel

@Composable
fun CatalogScreen(viewModel: CatalogViewModel, cartViewModel: CartViewModel) {
    val state by viewModel.uiState.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()

    // Un mapa para acceder rápidamente a la cantidad de cada producto en el carrito
    val cartItemsMap = cartState.items.associateBy { it.product.codigo }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Filtros ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.search,
                onValueChange = { viewModel.updateSearch(it) },
                label = { Text("Buscar producto...") },
                modifier = Modifier.weight(1f)
            )
            CategoryDropdown(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelect = { viewModel.updateCategory(it) }
            )
        }
        Spacer(Modifier.height(12.dp))

        // --- Grid de productos ---
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.filteredProducts) { p ->
                ProductCard(
                    product = p,
                    cartItem = cartItemsMap[p.codigo], // Pasa el item del carrito si existe
                    onAdd = { cartViewModel.addToCart(p.codigo) },
                    onIncrease = { cartViewModel.increase(p.codigo) },
                    onDecrease = { cartViewModel.decrease(p.codigo) }
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
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().widthIn(min = 160.dp)
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

/**
 * ProductCard: Tarjeta de producto mejorada.
 * Ahora muestra un botón de "Agregar" o un control de cantidad (+/-)
 * si el producto ya está en el carrito.
 */
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
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = product.img ?: "file:///android_asset/img/default.png",
                contentDescription = product.nombre,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(product.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "S/.${product.precio}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                product.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Lógica de botones ---
            if (cartItem == null || cartItem.qty == 0) {
                // Si no está en el carrito, muestra el botón de agregar
                Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                    Text("Agregar")
                }
            } else {
                // Si ya está, muestra el control de cantidad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onDecrease) {
                        Icon(Icons.Default.Remove, "Quitar uno")
                    }
                    Text(
                        "${cartItem.qty}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onIncrease) {
                        Icon(Icons.Default.Add, "Añadir uno")
                    }
                }
            }
        }
    }
}