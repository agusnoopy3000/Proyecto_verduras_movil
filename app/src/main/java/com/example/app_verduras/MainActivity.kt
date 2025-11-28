package com.example.app_verduras

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.app_verduras.api.RetrofitClient
import com.example.app_verduras.api.TokenManager
import com.example.app_verduras.dal.AppDatabase
import com.example.app_verduras.repository.DocumentoRepository
import com.example.app_verduras.repository.PedidoRepository
import com.example.app_verduras.repository.ProductoRepository
import com.example.app_verduras.repository.UserRepository
import com.example.app_verduras.ui.screens.*
import com.example.app_verduras.util.AnalyticsManager
import com.example.app_verduras.util.AppCheckManager
import com.example.app_verduras.util.CrashlyticsManager
import com.example.app_verduras.util.PerformanceManager
import com.example.app_verduras.util.SessionManager
import com.example.app_verduras.viewmodel.*
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // Launcher para solicitar permiso de notificaciones
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permiso de notificaciones concedido")
            AnalyticsManager.logCustomEvent("notification_permission_granted")
        } else {
            Log.d(TAG, "Permiso de notificaciones denegado")
            AnalyticsManager.logCustomEvent("notification_permission_denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar TokenManager y SessionManager
        TokenManager.init(this)
        SessionManager.init(this)
        
        // Inicializar Firebase Analytics y Crashlytics
        initializeFirebaseServices()
        
        // Solicitar permiso de notificaciones (Android 13+)
        requestNotificationPermission()
        
        // Obtener y loguear el token FCM
        getFCMToken()
        
        setContent {
            HuertoHogarApp()
        }
    }
    
    /**
     * Inicializa los servicios de Firebase (App Check, Analytics, Crashlytics y Performance)
     */
    private fun initializeFirebaseServices() {
        // IMPORTANTE: Inicializar App Check PRIMERO, antes de cualquier otro servicio
        // Esto protege Vertex AI y otros servicios de uso no autorizado
        val isDebugBuild = 0 != applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
        AppCheckManager.initialize(this, isDebugBuild)
        
        // Inicializar Analytics
        AnalyticsManager.initialize(this)
        
        // Inicializar Crashlytics
        CrashlyticsManager.initialize()
        
        // Inicializar Performance Monitoring
        PerformanceManager.initialize()
        
        // Registrar inicio de la app
        AnalyticsManager.logCustomEvent("app_started")
        CrashlyticsManager.log("App iniciada")
        
        Log.d(TAG, "Firebase services inicializados")
    }
    
    /**
     * Solicita el permiso de notificaciones en Android 13+
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Permiso de notificaciones ya concedido")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Aquí podrías mostrar un diálogo explicando por qué necesitas el permiso
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    /**
     * Obtiene el token FCM para notificaciones push
     */
    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Error al obtener token FCM", task.exception)
                CrashlyticsManager.recordException(
                    task.exception ?: Exception("Error desconocido al obtener token FCM"),
                    "Error al obtener token FCM"
                )
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d(TAG, "Token FCM: $token")
            
            // Aquí podrías enviar el token al backend
            // cuando implementes el endpoint correspondiente
        }
    }
}

// Clase sellada para las rutas de navegación
sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Registro")
    object ForgotPassword : Screen("forgot_password", "Recuperar Contraseña")
    object PhoneVerification : Screen("phone_verification/{mode}", "Verificación") {
        fun createRoute(mode: String) = "phone_verification/$mode"
    }
    object WelcomeUser : Screen("welcome_user", "Bienvenido")
    object WelcomeAdmin : Screen("welcome_admin", "Bienvenido Admin")
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Catalog : Screen("catalog", "Catálogo", Icons.AutoMirrored.Filled.List)
    object Cart : Screen("cart", "Carrito", Icons.Default.ShoppingCart)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
    object Pedido : Screen("pedido", "Resumen del Pedido")
    object MyOrders : Screen("my_orders", "Pedidos", Icons.Default.Receipt)
    object QRScanner : Screen("qr_scanner", "QR", Icons.Filled.QrCodeScanner)
    object Offers : Screen("offers", "Ofertas") // Deep link para ofertas
    object Confirmation : Screen("confirmation", "Confirmación")
    object AdminPanel : Screen("admin_panel", "Panel de Administrador")
    object ProductManagement : Screen("product_management", "Gestionar Productos")
    object UserManagement : Screen("user_management", "Gestionar Usuarios")
    object OrderManagement : Screen("order_management", "Gestionar Pedidos")
    object DocumentManagement : Screen("document_management", "Gestionar Documentos")
    object AIAssistant : Screen("ai_assistant", "Asistente IA", Icons.Default.SmartToy)
    object SecuritySettings : Screen("security_settings", "Seguridad")
    object AdminDashboard : Screen("admin_dashboard", "Dashboard", Icons.Default.Dashboard)
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

    // Pantallas sin barra de navegación
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
        Screen.AdminDashboard.route,
        Screen.Confirmation.route,
        Screen.Pedido.route,
        Screen.AIAssistant.route
    )
    val showBars = currentRoute !in screensWithoutBars

    // Menú del usuario: Home, Catálogo, Carrito, Pedidos, Perfil
    val bottomBarItems = listOf(Screen.Home, Screen.Catalog, Screen.Cart, Screen.MyOrders, Screen.Profile)

    Scaffold(
        topBar = {
            if (showBars) {
                val currentScreen = bottomBarItems.find { it.route == currentRoute }
                TopAppBar(
                    title = { 
                        Text(
                            currentScreen?.label ?: "Huerto Hogar",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier.semantics {
                                contentDescription = "Pantalla actual: ${currentScreen?.label ?: "Huerto Hogar"}"
                            }
                        ) 
                    },
                    actions = {
                        // Botón de cerrar sesión mejorado con texto
                        Surface(
                            onClick = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .semantics {
                                    contentDescription = "Cerrar sesión"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "Salir",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (showBars) {
                val cartState by cartViewModel.cartState.collectAsState()
                val cartItemCount = cartState.items.sumOf { it.qty }
                
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    bottomBarItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        
                        NavigationBarItem(
                            selected = isSelected,
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
                            icon = { 
                                // Badge animado para el carrito
                                if (screen == Screen.Cart) {
                                    Box(contentAlignment = Alignment.Center) {
                                        screen.icon?.let { 
                                            Icon(
                                                it, 
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            ) 
                                        }
                                        // Badge animado con bounce
                                        com.example.app_verduras.ui.components.AnimatedBadge(
                                            count = cartItemCount,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 10.dp, y = (-6).dp)
                                        )
                                    }
                                } else {
                                    screen.icon?.let { 
                                        Icon(
                                            it, 
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        ) 
                                    }
                                }
                            },
                            label = { 
                                Text(
                                    screen.label,
                                    maxLines = 1
                                ) 
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "${screen.label}${if (isSelected) ", seleccionado" else ""}${if (screen == Screen.Cart && cartItemCount > 0) ", $cartItemCount productos" else ""}"
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                indicatorColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        },
        // FAB para el Asistente de IA - visible en pantallas principales
        floatingActionButton = {
            if (showBars && currentRoute != Screen.AIAssistant.route) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AIAssistant.route) },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.semantics {
                        contentDescription = "Abrir asistente de inteligencia artificial"
                    }
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = "Asistente IA"
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            // Animaciones de transición suaves
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    initialOffsetX = { 100 },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    targetOffsetX = { -100 },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    initialOffsetX = { -100 },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    targetOffsetX = { 100 },
                    animationSpec = tween(300)
                )
            }
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
            
            // Pantalla de verificación MFA (SMS)
            composable(
                route = Screen.PhoneVerification.route,
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val modeArg = backStackEntry.arguments?.getString("mode") ?: "verification"
                val mode = when (modeArg.lowercase()) {
                    "enrollment" -> com.example.app_verduras.viewmodel.MFAMode.ENROLLMENT
                    else -> com.example.app_verduras.viewmodel.MFAMode.VERIFICATION
                }
                PhoneVerificationScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mode = mode
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
                PedidoScreenEnhanced(
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
            
            composable(Screen.QRScanner.route) {
                QRScannerScreen(
                    onQrCodeScanned = { qrCode ->
                        // Cuando se escanea un QR, navegar al catálogo o mostrar el producto
                        navController.navigate(Screen.Catalog.route)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.MyOrders.route) {
                MyOrdersScreen(
                    navController = navController,
                    cartViewModel = cartViewModel
                )
            }

            // Pantalla de Perfil - Usa datos reales del usuario autenticado
            composable(Screen.Profile.route) {
                val currentUser = SessionManager.currentUser
                ProfileScreen(
                    userName = "${currentUser?.nombre ?: "Usuario"} ${currentUser?.apellido ?: ""}".trim(),
                    userEmail = currentUser?.email ?: "Sin email",
                    userPhone = currentUser?.telefono ?: "No registrado",
                    userAddress = currentUser?.direccion ?: "No registrada",
                    onEditProfile = { /* TODO: Navegar a editar perfil */ },
                    onAddPaymentMethod = { /* Dialog de pago */ },
                    onViewOrders = { navController.navigate(Screen.MyOrders.route) },
                    onScanQR = { navController.navigate(Screen.QRScanner.route) },
                    onSecuritySettings = { navController.navigate(Screen.SecuritySettings.route) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Deep Link para Ofertas (visual por ahora)
            composable(
                route = Screen.Offers.route,
                deepLinks = listOf(
                    androidx.navigation.navDeepLink {
                        uriPattern = "huertohogar://offers"
                    },
                    androidx.navigation.navDeepLink {
                        uriPattern = "https://huertohogar.cl/offers"
                    }
                )
            ) {
                OffersScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate(Screen.Catalog.route)
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
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onLogout = onLogout
                )
            }

            composable(Screen.ProductManagement.route) {
                val productManagementVM: ProductManagementViewModel = viewModel(factory = ProductManagementViewModel.Factory(productoRepository))
                ProductManagementScreen(
                    viewModel = productManagementVM,
                    onNavigateBack = { navController.navigate(Screen.AdminPanel.route) { popUpTo(Screen.AdminPanel.route) { inclusive = true } } },
                    onNavigateToUsers = { navController.navigate(Screen.UserManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onNavigateToOrders = { navController.navigate(Screen.OrderManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onNavigateToDocuments = { navController.navigate(Screen.DocumentManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onLogout = onLogout
                )
            }

            composable(Screen.UserManagement.route) {
                val userManagementVM: UserManagementViewModel = viewModel(factory = UserManagementViewModel.Factory(userRepository))
                UserManagementScreen(
                    viewModel = userManagementVM,
                    onNavigateBack = { navController.navigate(Screen.AdminPanel.route) { popUpTo(Screen.AdminPanel.route) { inclusive = true } } },
                    onNavigateToProducts = { navController.navigate(Screen.ProductManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onNavigateToOrders = { navController.navigate(Screen.OrderManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onNavigateToDocuments = { navController.navigate(Screen.DocumentManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onLogout = onLogout
                )
            }

            composable(Screen.OrderManagement.route) {
                val orderManagementVM: OrderManagementViewModel = viewModel(factory = OrderManagementViewModel.Factory(pedidoRepository))
                OrderManagementScreen(
                    viewModel = orderManagementVM,
                    onNavigateBack = { navController.navigate(Screen.AdminPanel.route) { popUpTo(Screen.AdminPanel.route) { inclusive = true } } },
                    onNavigateToProducts = { navController.navigate(Screen.ProductManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onNavigateToUsers = { navController.navigate(Screen.UserManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onNavigateToDocuments = { navController.navigate(Screen.DocumentManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onLogout = onLogout
                )
            }

            composable(Screen.DocumentManagement.route) {
                val app = context.applicationContext as Application
                val documentoVM: DocumentoViewModel = viewModel(factory = DocumentoViewModel.Factory(app, documentoRepository))
                DocumentosScreen(
                    viewModel = documentoVM,
                    onNavigateBack = { navController.navigate(Screen.AdminPanel.route) { popUpTo(Screen.AdminPanel.route) { inclusive = true } } },
                    onNavigateToProducts = { navController.navigate(Screen.ProductManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onNavigateToUsers = { navController.navigate(Screen.UserManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onNavigateToOrders = { navController.navigate(Screen.OrderManagement.route) { popUpTo(Screen.AdminPanel.route) } },
                    onLogout = onLogout
                )
            }

            // Pantalla del Asistente de IA
            composable(Screen.AIAssistant.route) {
                val aiAssistantVM: AIAssistantViewModel = viewModel(factory = AIAssistantViewModel.Factory())
                val cartState by cartViewModel.cartState.collectAsState()
                AIAssistantScreen(
                    viewModel = aiAssistantVM,
                    cartItems = cartState.items,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Pantalla de configuración de seguridad (MFA)
            composable(Screen.SecuritySettings.route) {
                SecuritySettingsScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            
            // Dashboard de administrador con gráficos interactivos
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    onNavigateBack = { 
                        navController.navigate(Screen.AdminPanel.route) {
                            popUpTo(Screen.AdminPanel.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
