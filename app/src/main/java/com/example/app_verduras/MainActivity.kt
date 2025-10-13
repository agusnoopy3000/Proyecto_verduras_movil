package com.example.app_verduras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.app_verduras.ui.screens.CartScreen
import com.example.app_verduras.ui.theme.screens.CatalogScreen
import com.example.app_verduras.ui.theme.screens.HomeScreen
import com.example.app_verduras.ui.theme.screens.LoginScreen
import com.example.app_verduras.ui.theme.screens.RegisterScreen
import com.example.app_verduras.viewmodel.AuthViewModel
import com.example.app_verduras.viewmodel.CartViewModel
import com.example.app_verduras.viewmodel.CatalogViewModel
import com.example.app_verduras.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HuertoHogarApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuertoHogarApp() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Home,
        Screen.Catalog,
        Screen.Cart
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore('?')
    val showBottomBar = currentRoute in items.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login", // Empezamos en la pantalla de login
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    navController = navController,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeVM: HomeViewModel = viewModel()
                HomeScreen(navController = navController, viewModel = homeVM)
            }

            composable(
                route = "${Screen.Catalog.route}?cat={cat}",
                arguments = listOf(navArgument("cat") {
                    nullable = true
                    defaultValue = null
                })
            ) {
                val catalogVM: CatalogViewModel = viewModel()
                val cartVM: CartViewModel = viewModel()
                CatalogScreen(viewModel = catalogVM, cartViewModel = cartVM)
            }

            composable(Screen.Cart.route) {
                val cartVM: CartViewModel = viewModel()
                CartScreen(navController = navController, viewModel = cartVM)
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Catalog : Screen("catalog", "Catálogo", Icons.Default.List)
    object Cart : Screen("cart", "Carrito", Icons.Default.ShoppingCart)
}