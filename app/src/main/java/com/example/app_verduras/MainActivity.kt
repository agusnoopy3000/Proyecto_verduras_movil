package com.example.app_verduras

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.app_verduras.repository.DocumentoRepository
import com.example.app_verduras.repository.PedidoRepository
import com.example.app_verduras.repository.ProductoRepository
import com.example.app_verduras.repository.UserRepository
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
    object Splash : Screen("splash", "Splash")
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Registro")
    object ForgotPassword : Screen("forgot_password", "Recuperar Contraseña")
    object WelcomeUser : Screen("welcome_user", "Bienvenido")
    object WelcomeAdmin : Screen("welcome_admin", "Bienvenido Admin")
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Catalog : Screen("catalog", "Catálogo", Icons.Default.List)
    object Cart : Screen("cart", "Carrito", Icons.Default.ShoppingCart)
    object Pedido : Screen("pedido", "Resumen del Pedido")
    object QRScanner : Screen("qr_scanner", "QR", Icons.Filled.QrCodeScanner)
    object Confirmation : Screen("confirmation", "Confirmación")
    object AdminPanel : Screen("admin_panel", "Panel de Administrador")
    object ProductManagement : Screen("product_management", "Gestionar Productos")
    object UserManagement : Screen("user_management", "Gestionar Usuarios")
    object OrderManagement : Screen("order_management", "Gestionar Pedidos")
    object DocumentManagement : Screen("document_management", "Gestionar Documentos")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuertoHogarApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    val userDao = db.userDao()
    val productoDao = db.productoDao()
    val pedidoDao = db.pedidoDao()
    val documentoDao = db.documentoDao()
    val productoRepository = ProductoRepository(productoDao, context.assets, RetrofitClient.apiService)
    val userRepository = UserRepository(userDao)
    val pedidoRepository = PedidoRepository(pedidoDao)
    val documentoRepository = DocumentoRepository(documentoDao)

    val cartViewModel: CartViewModel = viewModel(factory = CartViewModel.Factory(productoRepository, pedidoDao, userDao))
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(userDao))
    val databaseViewModel: DatabaseViewModel = viewModel(factory = DatabaseViewModel.Factory(productoRepository))
    val locationViewModel: LocationViewModel = viewModel()

    LaunchedEffect(Unit) {
        databaseViewModel.initializeDatabase()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screensWithoutBars = listOf(
        Screen.Splash.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route,
        Screen.WelcomeUser.route,
        Screen.WelcomeAdmin.route,
        Screen.AdminPanel.route,
        Screen.ProductManagement.route,
        Screen.UserManagement.route,
        Screen.OrderManagement.route,
        Screen.DocumentManagement.route,
        Screen.Confirmation.route,
        Screen.Pedido.route
    )
    val showBars = currentRoute !in screensWithoutBars

    val bottomBarItems = listOf(Screen.Home, Screen.Catalog, Screen.Cart, Screen.QRScanner)


    Scaffold(
        topBar = {
            if (showBars) {
                 val currentScreen = bottomBarItems.find { it.route == currentRoute }
                TopAppBar(
                    title = { Text(currentScreen?.label ?: "Huerto Hogar") },
                    actions = {
                        IconButton(onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBars) {
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
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController = navController)
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.WelcomeUser.route) {
                WelcomeUserScreen(
                    onTimeout = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.WelcomeUser.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.WelcomeAdmin.route) {
                WelcomeAdminScreen(
                    onTimeout = {
                        navController.navigate(Screen.AdminPanel.route) {
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
                    cartViewModel = cartViewModel,
                    onConfirmOrder = { navController.navigate(Screen.Pedido.route) }, 
                    onGoToCatalog = { navController.navigate(Screen.Catalog.route) }
                )
            }

            composable(Screen.Pedido.route) {
                PedidoScreen(
                    navController = navController,
                    cartViewModel = cartViewModel,
                    locationViewModel = locationViewModel
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

            val onLogout = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }

            composable(Screen.AdminPanel.route) {
                AdminPanelScreen(
                    onNavigateToProductManagement = { navController.navigate(Screen.ProductManagement.route) },
                    onNavigateToUserManagement = { navController.navigate(Screen.UserManagement.route) },
                    onNavigateToOrderManagement = { navController.navigate(Screen.OrderManagement.route) },
                    onNavigateToDocumentManagement = { navController.navigate(Screen.DocumentManagement.route) },
                    onLogout = onLogout
                )
            }

            composable(Screen.ProductManagement.route) {
                val productManagementVM: ProductManagementViewModel = viewModel(factory = ProductManagementViewModel.Factory(productoRepository))
                ProductManagementScreen(
                    viewModel = productManagementVM,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = onLogout
                )
            }

            composable(Screen.UserManagement.route) {
                val userManagementVM: UserManagementViewModel = viewModel(factory = UserManagementViewModel.Factory(userRepository))
                UserManagementScreen(
                    viewModel = userManagementVM,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = onLogout
                )
            }

            composable(Screen.OrderManagement.route) {
                val orderManagementVM: OrderManagementViewModel = viewModel(factory = OrderManagementViewModel.Factory(pedidoRepository))
                OrderManagementScreen(
                    viewModel = orderManagementVM,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = onLogout
                )
            }

            composable(Screen.QRScanner.route) {
                 QRScannerScreen(onQrCodeScanned = {
                     navController.popBackStack()
                 })
            }

            composable(Screen.DocumentManagement.route) {
                val app = context.applicationContext as Application
                val documentoVM: DocumentoViewModel = viewModel(factory = DocumentoViewModel.Factory(app, documentoRepository))
                DocumentosScreen(
                    viewModel = documentoVM,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = onLogout
                )
            }
        }
    }
}
