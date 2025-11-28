package com.example.app_verduras.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.ui.components.EnhancedSnackbarHost
import com.example.app_verduras.ui.components.ProductGridShimmer
import com.example.app_verduras.ui.components.rememberEnhancedSnackbarState
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.viewmodel.CartItem
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.CatalogViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(viewModel: CatalogViewModel, cartViewModel: CartViewModel) {
    val state by viewModel.uiState.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()
    val cartItemsMap = cartState.items.associateBy { it.product.id }
    
    // Snackbar mejorado para mensajes del carrito
    val enhancedSnackbarState = rememberEnhancedSnackbarState()
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                HuertoHogarColors.Primary.copy(alpha = 0.05f),
                                HuertoHogarColors.Background,
                                HuertoHogarColors.Accent.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
            // Header mejorado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Título y contador
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "🛒 Catálogo",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = HuertoHogarColors.Primary
                            )
                            Text(
                                "${state.filteredProducts.size} productos disponibles",
                                style = MaterialTheme.typography.bodySmall,
                                color = HuertoHogarColors.TextSecondary
                            )
                        }
                        
                        // Badge del carrito
                        if (cartState.items.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HuertoHogarColors.Secondary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = HuertoHogarColors.Secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "${cartState.items.sumOf { it.qty }}",
                                        fontWeight = FontWeight.Bold,
                                        color = HuertoHogarColors.Secondary
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Barra de búsqueda mejorada
                    OutlinedTextField(
                        value = state.search,
                        onValueChange = { viewModel.updateSearch(it) },
                        placeholder = { 
                            Text(
                                "🔍 Buscar productos, frutas, verduras...",
                                color = HuertoHogarColors.TextSecondary
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = HuertoHogarColors.Primary
                            )
                        },
                        trailingIcon = {
                            if (state.search.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = HuertoHogarColors.TextSecondary
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuertoHogarColors.Primary,
                            unfocusedBorderColor = HuertoHogarColors.Accent,
                            focusedContainerColor = HuertoHogarColors.Primary.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    
                    // Búsquedas recientes
                    if (state.recentSearches.isNotEmpty() && state.search.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Búsquedas recientes:",
                            style = MaterialTheme.typography.labelSmall,
                            color = HuertoHogarColors.TextSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.recentSearches.forEach { search ->
                                AssistChip(
                                    onClick = { viewModel.selectRecentSearch(search) },
                                    label = { Text(search, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = HuertoHogarColors.Accent.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // Filtros de categoría en chips horizontales
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategory == null,
                        onClick = { viewModel.updateCategory(null) },
                        label = { Text("Todos") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.GridView,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HuertoHogarColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(state.categories) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.updateCategory(category) },
                        label = { Text(category) },
                        leadingIcon = {
                            Text(
                                getCategoryEmoji(category),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getCategoryColor(category),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Mostrar shimmer mientras carga
            if (state.isLoading) {
                ProductGridShimmer(
                    columns = 2,
                    itemCount = 6,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            } else {
                // Grid de productos con sección de sugeridos
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 165.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                // Sección de productos sugeridos (si no está buscando)
                if (state.suggestedProducts.isNotEmpty() && !state.isSearching) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = HuertoHogarColors.Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (state.selectedCategory != null) 
                                        "También te puede interesar" 
                                    else 
                                        "Sugeridos para ti",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HuertoHogarColors.OnBackground
                                )
                            }
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.suggestedProducts) { product ->
                                    SuggestedProductCard(
                                        product = product,
                                        onAdd = {
                                            cartViewModel.addToCart(product.id)
                                            scope.launch {
                                                enhancedSnackbarState.showCartAdd(product.nombre)
                                            }
                                        }
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Divider(
                                color = HuertoHogarColors.Accent,
                                thickness = 1.dp
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Text(
                                if (state.selectedCategory != null)
                                    "📦 ${state.selectedCategory}"
                                else
                                    "📦 Todos los productos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = HuertoHogarColors.OnBackground
                            )
                        }
                    }
                }
                
                // Mensaje si no hay resultados
                if (state.filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = HuertoHogarColors.TextSecondary
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No encontramos productos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = HuertoHogarColors.OnBackground
                            )
                            Text(
                                "Intenta con otra búsqueda o categoría",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HuertoHogarColors.TextSecondary
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { 
                                    viewModel.clearSearch()
                                    viewModel.updateCategory(null)
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ver todos los productos")
                            }
                        }
                    }
                }
                
                // Grid de productos
                items(
                    items = state.filteredProducts,
                    key = { it.id }
                ) { product ->
                    ProductCard(
                        product = product,
                        cartItem = cartItemsMap[product.id],
                        onAdd = { 
                            cartViewModel.addToCart(product.id)
                            if (state.search.isNotEmpty()) {
                                viewModel.addToRecentSearches(state.search)
                            }
                            scope.launch {
                                enhancedSnackbarState.showCartAdd(product.nombre)
                            }
                        },
                        onIncrease = { 
                            cartViewModel.increase(product.id)
                            scope.launch {
                                enhancedSnackbarState.showInfo(
                                    text = "${product.nombre} (${(cartItemsMap[product.id]?.qty ?: 0) + 1} unidades)",
                                    emoji = "➕"
                                )
                            }
                        },
                        onDecrease = { 
                            val currentQty = cartItemsMap[product.id]?.qty ?: 0
                            cartViewModel.decrease(product.id)
                            scope.launch {
                                if (currentQty <= 1) {
                                    enhancedSnackbarState.showCartRemove(product.nombre)
                                } else {
                                    enhancedSnackbarState.showInfo(
                                        text = "${product.nombre} (${currentQty - 1} unidades)",
                                        emoji = "➖"
                                    )
                                }
                            }
                        }
                    )
                }
                } // Fin LazyVerticalGrid
            } // Fin del else (loading)
            } // Fin Column
            
            // Snackbar mejorado
            EnhancedSnackbarHost(
                state = enhancedSnackbarState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        } // Fin Box
    } // Fin Scaffold
} // Fin CatalogScreen

/**
 * Tarjeta de producto sugerido (versión compacta horizontal)
 */
@Composable
fun SuggestedProductCard(
    product: Producto,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
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
                        .padding(6.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(6.dp),
                    color = getCategoryColor(product.categoria).copy(alpha = 0.9f)
                ) {
                    Text(
                        text = getCategoryEmoji(product.categoria ?: ""),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    product.nombre,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$${product.precio.toInt()}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = HuertoHogarColors.Primary
                    )
                    
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                HuertoHogarColors.Primary.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar ${product.nombre}",
                            tint = HuertoHogarColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
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
            )
            .semantics {
                contentDescription = "${product.nombre}, ${product.categoria ?: "producto"}, precio ${product.precio.toInt()} pesos${if (hasItems) ", ${cartItem?.qty} en el carrito" else ""}"
            },
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
                    contentDescription = "Imagen de ${product.nombre}",
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
                                interactionSource = interactionSource,
                                modifier = Modifier.semantics {
                                    contentDescription = "Agregar ${product.nombre} al carrito"
                                }
                            ) {
                                Icon(
                                    Icons.Default.AddShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            // Control de cantidad con animaciones mejoradas
                            QuantityControlsAnimated(
                                quantity = cartItem?.qty ?: 0,
                                onIncrease = onIncrease,
                                onDecrease = onDecrease
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Controles de cantidad con animaciones mejoradas
 */
@Composable
private fun QuantityControlsAnimated(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    // Estados de animación para cada botón
    var isAddPressed by remember { mutableStateOf(false) }
    var isRemovePressed by remember { mutableStateOf(false) }
    
    // Animaciones de escala para los botones
    val addScale by animateFloatAsState(
        targetValue = if (isAddPressed) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "addScale",
        finishedListener = { isAddPressed = false }
    )
    
    val removeScale by animateFloatAsState(
        targetValue = if (isRemovePressed) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "removeScale",
        finishedListener = { isRemovePressed = false }
    )
    
    // Animación del número con rebote
    val quantityScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "quantityScale"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = HuertoHogarColors.Primary.copy(alpha = 0.1f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            // Botón QUITAR (Rojo) con animación
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .scale(removeScale)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, color = Color(0xFFE53935))
                    ) {
                        isRemovePressed = true
                        onDecrease()
                    },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE53935).copy(alpha = 0.15f),
                shadowElevation = if (isRemovePressed) 4.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Quitar una unidad",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Cantidad con animación
            AnimatedContent(
                targetState = quantity,
                transitionSpec = {
                    if (targetState > initialState) {
                        // Incrementando: desliza hacia arriba
                        slideInVertically { -it } + fadeIn() togetherWith 
                        slideOutVertically { it } + fadeOut()
                    } else {
                        // Decrementando: desliza hacia abajo
                        slideInVertically { it } + fadeIn() togetherWith 
                        slideOutVertically { -it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "quantity_animation"
            ) { targetQuantity ->
                Text(
                    text = "$targetQuantity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HuertoHogarColors.Primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .scale(quantityScale)
                )
            }
            
            // Botón AGREGAR (Verde) con animación
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .scale(addScale)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, color = Color(0xFF43A047))
                    ) {
                        isAddPressed = true
                        onIncrease()
                    },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF43A047).copy(alpha = 0.15f),
                shadowElevation = if (isAddPressed) 4.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Añadir una unidad",
                        tint = Color(0xFF43A047),
                        modifier = Modifier.size(20.dp)
                    )
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
