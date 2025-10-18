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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.app_verduras.api.RetrofitClient
import com.example.app_verduras.dal.AppDatabase
import com.example.app_verduras.repository.ProductoRepository
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

// Clase sellada para las rutas de navegación
sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Registro")
    object WelcomeUser : Screen("welcome_user", "Bienvenido") // Nueva ruta
    object WelcomeAdmin : Screen("welcome_admin", "Bienvenido Admin") // Nueva ruta
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Catalog : Screen("catalog", "Catálogo", Icons.Default.List)
    object Cart : Screen("cart", "Carrito", Icons.Default.ShoppingCart)
    object Confirmation : Screen("confirmation", "Confirmación")
    object AdminPanel : Screen("admin_panel", "Panel de Administrador")
    object ProductManagement : Screen("product_management", "Gestionar Productos")
    object UserManagement : Screen("user_management", "Gestionar Usuarios")
    object OrderManagement : Screen("order_management", "Gestionar Pedidos")
}

@Composable
fun HuertoHogarApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    // --- DAOs y Repositorios ---
    val userDao = db.userDao()
    val productoDao = db.productoDao()
    val pedidoDao = db.pedidoDao()
    val productoRepository = ProductoRepository(productoDao, context.assets, RetrofitClient.apiService)

    // --- ViewModels ---
    val cartViewModel: CartViewModel = viewModel(factory = CartViewModel.Factory(productoRepository, pedidoDao))
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(userDao))
    // Se instancia el nuevo ViewModel para la inicialización de la BD.
    val databaseViewModel: DatabaseViewModel = viewModel(factory = DatabaseViewModel.Factory(productoRepository))

    // Este bloque se ejecuta una sola vez cuando el Composable entra en la composición.
    // Es el lugar perfecto para operaciones de inicialización.
    LaunchedEffect(Unit) {
        databaseViewModel.initializeDatabase()
    }

    // --- Lógica de la Bottom Bar ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomBarItems = listOf(Screen.Home, Screen.Catalog, Screen.Cart)
    val showBottomBar = currentRoute in bottomBarItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarItems.forEach { screen ->
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
                            icon = { screen.icon?.let { Icon(it, contentDescription = screen.label) } },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        // Navega a la pantalla de bienvenida del usuario
                        navController.navigate(Screen.WelcomeUser.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onAdminLoginSuccess = {
                        // Navega a la pantalla de bienvenida del admin
                        navController.navigate(Screen.WelcomeAdmin.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        // Navega a la pantalla de bienvenida del usuario tras el registro
                        navController.navigate(Screen.WelcomeUser.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // Pantalla de bienvenida para el usuario normal
            composable(Screen.WelcomeUser.route) {
                WelcomeUserScreen(
                    onTimeout = {
                        navController.navigate(Screen.Home.route) {
                            // Limpia la pantalla de bienvenida del backstack
                            popUpTo(Screen.WelcomeUser.route) { inclusive = true }
                        }
                    }
                )
            }

            // Pantalla de bienvenida para el administrador
            composable(Screen.WelcomeAdmin.route) {
                WelcomeAdminScreen(
                    onTimeout = {
                        navController.navigate(Screen.AdminPanel.route) {
                            // Limpia la pantalla de bienvenida del backstack
                            popUpTo(Screen.WelcomeAdmin.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeVM: HomeViewModel = viewModel(factory = HomeViewModelFactory(productoRepository))
                HomeScreen(navController = navController, viewModel = homeVM)
            }

            composable(Screen.Catalog.route) {
                val catalogVM: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory(productoRepository))
                CatalogScreen(viewModel = catalogVM, cartViewModel = cartViewModel)
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = cartViewModel,
                    onConfirmOrder = {
                        navController.navigate(Screen.Confirmation.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.Confirmation.route) {
                ConfirmationScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AdminPanel.route) {
                AdminPanelScreen(
                    onNavigateToProductManagement = { navController.navigate(Screen.ProductManagement.route) },
                    onNavigateToUserManagement = { navController.navigate(Screen.UserManagement.route) },
                    onNavigateToOrderManagement = { navController.navigate(Screen.OrderManagement.route) }
                )
            }

            composable(Screen.ProductManagement.route) {
                // Instanciamos el ViewModel para la gestión de productos
                val productManagementVM: ProductManagementViewModel = viewModel(factory = ProductManagementViewModel.Factory(productoRepository))
                ProductManagementScreen(
                    viewModel = productManagementVM, // Se lo pasamos a la pantalla
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.UserManagement.route) {
                UserManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.OrderManagement.route) {
                OrderManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
