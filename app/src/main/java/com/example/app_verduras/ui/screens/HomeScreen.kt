package com.example.app_verduras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app_verduras.R
import com.example.app_verduras.ui.components.AppFooter
import com.example.app_verduras.ui.components.WeatherWidget
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

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget de Clima (API EXTERNA)
            WeatherWidget(
                weatherState = weatherState,
                onRetry = { weatherViewModel.fetchWeatherByCity("Santiago,CL") }
            )

            // --- Hero Section ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Del campo a tu hogar",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Frutas y verduras orgánicas frescas cada día.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { navController.navigate("catalog") },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Ver catálogo")
                            }
                            OutlinedButton(
                                onClick = { navController.navigate("register") },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Crear cuenta")
                            }
                        }
                    }
                    AsyncImage(
                        model = R.drawable.huerto_hogar,
                        contentDescription = "HuertoHogar Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Título de categorías
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Categorías destacadas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid de categorías (no scrollable porque ya tenemos scroll padre)
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
                        // Si hay número impar, rellenar con Spacer
                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Footer
        AppFooter()
    }
}

@Composable
fun CategoryCard(
    title: String, 
    onExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de categoría
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    getCategoryEmoji(title),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                title, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onExplore,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text("Explorar") 
            }
        }
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