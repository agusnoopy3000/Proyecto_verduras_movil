package com.example.app_verduras.ui.screens

// Import necesarios (Compose Material3 + Foundation + Coil)
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.viewmodel.CatalogViewModel
import com.example.app_verduras.viewmodel.CartViewModel

@Composable
fun CatalogScreen(viewModel: CatalogViewModel, cartViewModel: CartViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

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

            // Usa componente Dropdown (implementado abajo)
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
                    onAdd = { cartViewModel.addToCart(p.codigo) } // conecta con CartViewModel
                )
            }
        }
    }
}

/** CategoryDropdown: implementado con Material3 ExposedDropdownMenuBox para compatibilidad */
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
            value = selected ?: "Todas las categorías",
            onValueChange = { /* readOnly */ },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.widthIn(min = 160.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Opción "Todas"
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = { onSelect(null); expanded = false }
            )

            // Opciones de categoría
            categories.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c) },
                    onClick = { onSelect(c); expanded = false }
                )
            }
        }
    }
}

/** ProductCard: tarjeta de producto con imagen y botón agregar */
@Composable
fun ProductCard(product: Producto, onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = product.img ?: "file:///android_asset/default-fruta.png",
                contentDescription = product.nombre,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(product.nombre, style = MaterialTheme.typography.titleSmall)
            Text(
                product.categoria,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "${product.precio.toInt()} CLP",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar")
            }
        }
    }
}