package com.tuorg.huertohogar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.example.app_verduras.ui.screens.*
import com.example.app_verduras.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HuertoHogarApp()
        }
    }
}

@Composable
fun HuertoHogarApp() {
    val navController = rememberNavController()
    val homeVM = remember { HomeViewModel() }
    val catalogVM = remember { CatalogViewModel() }
    val cartVM = remember { CartViewModel() }

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(navController, homeVM)
            }
            composable("catalog") {
                CatalogScreen(catalogVM)
            }
            composable("cart") {
                CartScreen(cartVM)
            }
        }
    }
}
