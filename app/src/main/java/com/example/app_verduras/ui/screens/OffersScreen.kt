package com.example.app_verduras.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

/**
 * Pantalla de Ofertas - Accesible via Deep Link
 * Deep Links soportados:
 * - huertohogar://offers
 * - https://huertohogar.cl/offers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(
    onNavigateBack: () -> Unit,
    onProductClick: (String) -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300)
        isLoaded = true
    }
    
    // Datos de ejemplo para las ofertas
    val featuredOffers = remember {
        listOf(
            OfferItem(
                id = "1",
                name = "Tomates Orgánicos",
                description = "Tomates frescos de huerto local",
                originalPrice = 2500,
                discountedPrice = 1750,
                discountPercentage = 30,
                imageUrl = "https://images.unsplash.com/photo-1546470427-227c7369a9b9?w=400",
                category = "Verduras",
                isFlashSale = true
            ),
            OfferItem(
                id = "2",
                name = "Lechugas Hidropónicas",
                description = "Pack de 3 lechugas frescas",
                originalPrice = 3000,
                discountedPrice = 1800,
                discountPercentage = 40,
                imageUrl = "https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=400",
                category = "Verduras",
                isFlashSale = true
            ),
            OfferItem(
                id = "3",
                name = "Zanahorias Baby",
                description = "500g de zanahorias tiernas",
                originalPrice = 1800,
                discountedPrice = 1200,
                discountPercentage = 33,
                imageUrl = "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400",
                category = "Verduras",
                isFlashSale = false
            )
        )
    }
    
    val weeklyOffers = remember {
        listOf(
            OfferItem(
                id = "4",
                name = "Manzanas Fuji",
                description = "1kg de manzanas premium",
                originalPrice = 2800,
                discountedPrice = 2100,
                discountPercentage = 25,
                imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400",
                category = "Frutas",
                isFlashSale = false
            ),
            OfferItem(
                id = "5",
                name = "Espinacas Frescas",
                description = "Bolsa de 250g",
                originalPrice = 1500,
                discountedPrice = 990,
                discountPercentage = 34,
                imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400",
                category = "Verduras",
                isFlashSale = false
            ),
            OfferItem(
                id = "6",
                name = "Pimientos Mixtos",
                description = "Pack tricolor 500g",
                originalPrice = 3200,
                discountedPrice = 2400,
                discountPercentage = 25,
                imageUrl = "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=400",
                category = "Verduras",
                isFlashSale = false
            ),
            OfferItem(
                id = "7",
                name = "Naranjas de Jugo",
                description = "Malla de 2kg",
                originalPrice = 3500,
                discountedPrice = 2450,
                discountPercentage = 30,
                imageUrl = "https://images.unsplash.com/photo-1547514701-42782101795e?w=400",
                category = "Frutas",
                isFlashSale = false
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "🔥 Ofertas Especiales",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Volver atrás"
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Banner de bienvenida
            item {
                AnimatedVisibility(
                    visible = isLoaded,
                    enter = fadeIn() + slideInVertically()
                ) {
                    OffersBanner()
                }
            }
            
            // Sección de ofertas flash
            item {
                AnimatedVisibility(
                    visible = isLoaded,
                    enter = fadeIn(animationSpec = tween(delayMillis = 200))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Ofertas Flash",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            FlashSaleCountdown()
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featuredOffers) { offer ->
                                FlashOfferCard(
                                    offer = offer,
                                    onClick = { onProductClick(offer.id) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Sección de ofertas de la semana
            item {
                AnimatedVisibility(
                    visible = isLoaded,
                    enter = fadeIn(animationSpec = tween(delayMillis = 400))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ofertas de la Semana",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            
            // Grid de ofertas semanales
            itemsIndexed(weeklyOffers) { index, offer ->
                AnimatedVisibility(
                    visible = isLoaded,
                    enter = fadeIn(animationSpec = tween(delayMillis = 500 + (index * 100)))
                ) {
                    WeeklyOfferCard(
                        offer = offer,
                        onClick = { onProductClick(offer.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
            
            // Mensaje de deep link
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "¡Comparte estas ofertas!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Envía huertohogar://offers a tus amigos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OffersBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF8BC34A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "🌱 Huerto Hogar",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "¡Ofertas Exclusivas!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Hasta 40% de descuento en productos seleccionados",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun FlashSaleCountdown() {
    var timeLeft by remember { mutableIntStateOf(7200) } // 2 horas en segundos
    
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }
    
    val hours = timeLeft / 3600
    val minutes = (timeLeft % 3600) / 60
    val seconds = timeLeft % 60
    
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TimeUnit(value = hours)
            Text(":", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            TimeUnit(value = minutes)
            Text(":", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            TimeUnit(value = seconds)
        }
    }
}

@Composable
private fun TimeUnit(value: Int) {
    Text(
        text = value.toString().padStart(2, '0'),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun FlashOfferCard(
    offer: OfferItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "${offer.name}, ${offer.discountPercentage}% de descuento, antes ${offer.originalPrice} pesos, ahora ${offer.discountedPrice} pesos"
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(offer.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = offer.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                
                // Badge de descuento
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "-${offer.discountPercentage}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Badge flash
                if (offer.isFlashSale) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = Color(0xFFFF9800),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "FLASH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    offer.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "$${offer.discountedPrice}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "$${offer.originalPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyOfferCard(
    offer: OfferItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "${offer.name}, ${offer.discountPercentage}% de descuento"
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(offer.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = offer.name,
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
                
                // Badge de descuento
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "-${offer.discountPercentage}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        offer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        offer.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "$${offer.discountedPrice}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "$${offer.originalPrice}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar al carrito",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Data class para representar una oferta
 */
data class OfferItem(
    val id: String,
    val name: String,
    val description: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val discountPercentage: Int,
    val imageUrl: String,
    val category: String,
    val isFlashSale: Boolean = false
)
