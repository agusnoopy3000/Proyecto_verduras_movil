package com.example.app_verduras.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.ui.theme.HuertoHogarColors
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HuertoHogarColors.Accent,
                        HuertoHogarColors.Background
                    )
                )
            )
    ) {
        // Header con contador del carrito
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${state.filteredProducts.size} productos disponibles",
                style = MaterialTheme.typography.bodyMedium,
                color = HuertoHogarColors.TextSecondary
            )
            
            // Badge del carrito
            if (cartState.items.isNotEmpty()) {
                Badge(
                    containerColor = HuertoHogarColors.Secondary,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        "${cartState.items.sumOf { it.qty }}",
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }

        // Filtros mejorados
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.search,
                    onValueChange = { viewModel.updateSearch(it) },
                    placeholder = { Text("Buscar productos...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = HuertoHogarColors.Primary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuertoHogarColors.Primary,
                        unfocusedBorderColor = HuertoHogarColors.Accent
                    ),
                    singleLine = true
                )
                
                CategoryDropdown(
                    categories = state.categories,
                    selected = state.selectedCategory,
                    onSelect = { viewModel.updateCategory(it) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Grid de productos con animaciones
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 170.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(
                items = state.filteredProducts,
                key = { it.id }
            ) { product ->
                ProductCard(
                    product = product,
                    cartItem = cartItemsMap[product.id],
                    onAdd = { cartViewModel.addToCart(product.id) },
                    onIncrease = { cartViewModel.increase(product.id) },
                    onDecrease = { cartViewModel.decrease(product.id) }
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
        onExpandedChange = { expanded = it }
    ) {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { 
                Text(
                    selected ?: "Categoría",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) 
            },
            leadingIcon = {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.menuAnchor(),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = HuertoHogarColors.Primary.copy(alpha = 0.1f),
                selectedLabelColor = HuertoHogarColors.Primary
            )
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas las categorías") },
                onClick = { onSelect(null); expanded = false },
                leadingIcon = {
                    Icon(Icons.Default.GridView, contentDescription = null)
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = { onSelect(category); expanded = false },
                    leadingIcon = {
                        Text(getCategoryEmoji(category))
                    }
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val hasItems = cartItem != null && cartItem.qty > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (hasItems) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (hasItems) HuertoHogarColors.Primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            // Imagen con overlay de categoría
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = product.imagen ?: "file:///android_asset/img/default.png",
                    contentDescription = product.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                
                // Badge de categoría
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = getCategoryColor(product.categoria).copy(alpha = 0.9f)
                ) {
                    Text(
                        text = getCategoryEmoji(product.categoria ?: ""),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                // Indicador si está en el carrito
                if (hasItems) {
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd),
                        shape = CircleShape,
                        color = HuertoHogarColors.Primary
                    ) {
                        Text(
                            text = "${cartItem!!.qty}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

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
                    overflow = TextOverflow.Ellipsis,
                    color = HuertoHogarColors.OnBackground
                )
                
                Text(
                    text = product.descripcion ?: "Producto fresco y de calidad",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 32.dp),
                    color = HuertoHogarColors.TextSecondary
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$${product.precio.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = HuertoHogarColors.Primary
                        )
                        Text(
                            text = "CLP",
                            style = MaterialTheme.typography.labelSmall,
                            color = HuertoHogarColors.TextSecondary
                        )
                    }

                    AnimatedContent(
                        targetState = hasItems,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(200))
                        },
                        label = "cart_button"
                    ) { showQuantityControls ->
                        if (!showQuantityControls) {
                            FilledTonalButton(
                                onClick = onAdd,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = HuertoHogarColors.Primary.copy(alpha = 0.1f),
                                    contentColor = HuertoHogarColors.Primary
                                ),
                                interactionSource = interactionSource
                            ) {
                                Icon(
                                    Icons.Default.AddShoppingCart,
                                    contentDescription = "Agregar al carrito",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HuertoHogarColors.Primary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    IconButton(
                                        onClick = onDecrease,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Quitar uno",
                                            tint = HuertoHogarColors.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "${cartItem?.qty ?: 0}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = HuertoHogarColors.Primary,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = onIncrease,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Añadir uno",
                                            tint = HuertoHogarColors.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Obtiene el color asociado a cada categoría
 */
private fun getCategoryColor(category: String?): Color {
    return when (category?.lowercase()) {
        "verduras" -> HuertoHogarColors.CategoryVerduras
        "frutas" -> HuertoHogarColors.CategoryFrutas
        "hortalizas" -> HuertoHogarColors.CategoryHortalizas
        "orgánicos", "organicos" -> HuertoHogarColors.CategoryOrganicos
        "lácteos", "lacteos" -> Color(0xFF5C6BC0)
        "carnes" -> Color(0xFFEF5350)
        "bebidas" -> Color(0xFF26C6DA)
        "panadería", "panaderia" -> Color(0xFFFFB74D)
        else -> HuertoHogarColors.Primary
    }
}

/**
 * Retorna un emoji según la categoría
 */
private fun getCategoryEmoji(category: String?): String {
    return when (category?.lowercase()) {
        "verduras" -> "🥬"
        "frutas" -> "🍎"
        "hortalizas" -> "🥕"
        "lácteos", "lacteos" -> "🧀"
        "carnes" -> "🥩"
        "bebidas" -> "🥤"
        "panadería", "panaderia" -> "🍞"
        "snacks" -> "🍿"
        "orgánicos", "organicos" -> "🌿"
        else -> "🛒"
    }
}
