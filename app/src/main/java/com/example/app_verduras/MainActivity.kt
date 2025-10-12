package com.example.app_verduras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.app_verduras.ui.screens.*
import com.example.app_verduras.ui.theme.screens.HomeScreen
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

    // ViewModels persistentes
    val homeVM = remember { HomeViewModel() }
    val catalogVM = remember { CatalogViewModel() }
    val cartVM = remember { CartViewModel() }

    val items = listOf(
        Screen.Home,
        Screen.Catalog,
        Screen.Cart
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = currentBackStackEntryAsState(navController)
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute.value?.destination?.route == screen.route,
                        onClick = {
                            if (currentRoute.value?.destination?.route != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController, viewModel = homeVM)
            }
            composable(Screen.Catalog.route) {
                CatalogScreen(viewModel = catalogVM, cartViewModel = cartVM)
            }
            composable("catalog?cat={cat}",
                arguments = listOf(navArgument("cat") { nullable = true })
            ) { backStackEntry ->
                val cat = backStackEntry.arguments?.getString("cat")
                CatalogScreen(viewModel = catalogVM, cartViewModel = cartVM)
                cat?.let { catalogVM.updateCategory(it) } // Aplica filtro si viene desde Home
            }
        }
    }
}

private fun RowScope.currentBackStackEntryAsState(navController: NavHostController) {
    TODO("Not yet implemented")
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Catalog : Screen("catalog", "Catálogo", Icons.Default.List)
    object Cart : Screen("cart", "Carrito", Icons.Default.ShoppingCart)
}
