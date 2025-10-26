package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app_verduras.R
import com.example.app_verduras.ui.components.AppFooter
import com.example.app_verduras.viewmodel.HomeViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val categories by viewModel.categories.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // This column takes all available space, pushing the footer down
        Column(modifier = Modifier.weight(1f)) {
            // Original screen content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- Hero Section ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Del campo a tu hogar",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Frutas y verduras orgánicas. Productos lácteos y alimentos saludables.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { navController.navigate("catalog") }) {
                                Text("Ver catálogo")
                            }
                            OutlinedButton(onClick = { navController.navigate("register") }) {
                                Text("Crear cuenta")
                            }
                        }
                    }
                    AsyncImage(
                        model = R.drawable.huerto_hogar,
                        contentDescription = "HuertoHogar Logo",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(
                    "Categorías destacadas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories) { cat ->
                        CategoryCard(
                            title = cat,
                            onExplore = { navController.navigate("catalog?cat=$cat") }
                        )
                    }
                }
            }
        }

        // Footer is here, at the bottom of the screen
        AppFooter()
    }
}

@Composable
fun CategoryCard(title: String, onExplore: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Button(onClick = onExplore) { Text("Explorar") }
        }
    }
}