package com.example.app_verduras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app_verduras.R
import com.example.app_verduras.ui.components.WeatherWidget
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.util.SessionManager
import com.example.app_verduras.viewmodel.HomeViewModel
import com.example.app_verduras.viewmodel.WeatherViewModel

@Composable
fun HomeScreen(
    navController: NavController, 
    viewModel: HomeViewModel,
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val weatherState by weatherViewModel.weatherState.collectAsState()

    // Cargar clima de Santiago por defecto al iniciar
    LaunchedEffect(Unit) {
        weatherViewModel.fetchWeatherByCity("Santiago,CL")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Widget de Clima (API EXTERNA)
            WeatherWidget(
                weatherState = weatherState,
                onRetry = { weatherViewModel.fetchWeatherByCity("Santiago,CL") }
            )

            // --- Hero Section Mejorado ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box {
                    // Fondo decorativo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        HuertoHogarColors.Primary.copy(alpha = 0.1f),
                                        HuertoHogarColors.Secondary.copy(alpha = 0.05f)
                                    )
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "🌱 Del campo a tu hogar",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HuertoHogarColors.Primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Frutas y verduras orgánicas frescas cada día. Calidad premium directo del huerto.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = HuertoHogarColors.TextSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                            
                            Card(
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                AsyncImage(
                                    model = R.drawable.huerto_hogar,
                                    contentDescription = "Logo de Huerto Hogar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        
                        // Botones mejorados - condicional según si está logueado
                        val isLoggedIn = SessionManager.isLoggedIn()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate("catalog") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .then(if (isLoggedIn) Modifier.fillMaxWidth() else Modifier.weight(1f))
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HuertoHogarColors.Primary
                                ),
                                elevation = ButtonDefaults.buttonElevation(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Ver Catálogo",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            // Solo mostrar botón Registrarse si NO está logueado
                            if (!isLoggedIn) {
                                OutlinedButton(
                                    onClick = { navController.navigate("register") },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = HuertoHogarColors.Primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Registrarse",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Título de categorías con estilo mejorado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HuertoHogarColors.Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        tint = HuertoHogarColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Categorías Destacadas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = HuertoHogarColors.OnBackground
                    )
                    Text(
                        "Explora nuestros productos frescos",
                        style = MaterialTheme.typography.bodySmall,
                        color = HuertoHogarColors.TextSecondary
                    )
                }
            }

            // Categorías en fila horizontal con scroll
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(categories) { category ->
                    CategoryChipWithIcon(
                        category = category,
                        onClick = { navController.navigate("catalog?cat=$category") }
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))

            // Grid de categorías destacadas (tarjetas grandes)
            Text(
                "Explora por Categoría",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HuertoHogarColors.OnBackground
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.chunked(2).forEach { rowCategories ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowCategories.forEach { cat ->
                            CategoryCard(
                                title = cat,
                                onExplore = { navController.navigate("catalog?cat=$cat") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Chip de categoría con icono y nombre
 */
@Composable
fun CategoryChipWithIcon(
    category: String,
    onClick: () -> Unit
) {
    val categoryInfo = getCategoryInfo(category)
    
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = categoryInfo.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryInfo.icon,
                    contentDescription = category,
                    tint = categoryInfo.iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Nombre de la categoría
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = categoryInfo.textColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Tarjeta de categoría mejorada
 */
@Composable
fun CategoryCard(
    title: String, 
    onExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryInfo = getCategoryInfo(title)
    
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono con fondo de color
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                categoryInfo.backgroundColor,
                                categoryInfo.backgroundColor.copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryInfo.icon,
                    contentDescription = title,
                    tint = categoryInfo.iconColor,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Nombre de categoría
            Text(
                title, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = HuertoHogarColors.OnBackground,
                textAlign = TextAlign.Center
            )
            
            // Emoji decorativo
            Text(
                getCategoryEmoji(title),
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = onExplore,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = categoryInfo.iconColor
                )
            ) { 
                Text(
                    "Explorar",
                    fontWeight = FontWeight.SemiBold
                ) 
            }
        }
    }
}

/**
 * Data class para información de categoría
 */
data class CategoryInfo(
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color,
    val textColor: Color = Color.DarkGray
)

/**
 * Obtiene información visual de la categoría
 */
private fun getCategoryInfo(category: String): CategoryInfo {
    return when (category.lowercase()) {
        "verduras" -> CategoryInfo(
            icon = Icons.Outlined.Grass,
            iconColor = Color(0xFF2E7D32),
            backgroundColor = Color(0xFFE8F5E9)
        )
        "frutas" -> CategoryInfo(
            icon = Icons.Outlined.Eco,
            iconColor = Color(0xFFE65100),
            backgroundColor = Color(0xFFFFF3E0)
        )
        "lácteos", "lacteos" -> CategoryInfo(
            icon = Icons.Outlined.Icecream,
            iconColor = Color(0xFF1565C0),
            backgroundColor = Color(0xFFE3F2FD)
        )
        "carnes" -> CategoryInfo(
            icon = Icons.Outlined.Restaurant,
            iconColor = Color(0xFFC62828),
            backgroundColor = Color(0xFFFFEBEE)
        )
        "bebidas" -> CategoryInfo(
            icon = Icons.Outlined.LocalCafe,
            iconColor = Color(0xFF6A1B9A),
            backgroundColor = Color(0xFFF3E5F5)
        )
        "panadería", "panaderia" -> CategoryInfo(
            icon = Icons.Outlined.BakeryDining,
            iconColor = Color(0xFFFF8F00),
            backgroundColor = Color(0xFFFFF8E1)
        )
        "snacks" -> CategoryInfo(
            icon = Icons.Outlined.Fastfood,
            iconColor = Color(0xFFD84315),
            backgroundColor = Color(0xFFFBE9E7)
        )
        "orgánicos", "organicos" -> CategoryInfo(
            icon = Icons.Outlined.Spa,
            iconColor = Color(0xFF388E3C),
            backgroundColor = Color(0xFFE8F5E9)
        )
        else -> CategoryInfo(
            icon = Icons.Outlined.ShoppingBasket,
            iconColor = HuertoHogarColors.Primary,
            backgroundColor = HuertoHogarColors.Primary.copy(alpha = 0.1f)
        )
    }
}

/**
 * Retorna un emoji según la categoría
 */
private fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "verduras" -> "🥬"
        "frutas" -> "🍎"
        "lácteos", "lacteos" -> "🧀"
        "carnes" -> "🥩"
        "bebidas" -> "🥤"
        "panadería", "panaderia" -> "🍞"
        "snacks" -> "🍿"
        "orgánicos", "organicos" -> "🌿"
        else -> "🛒"
    }
}