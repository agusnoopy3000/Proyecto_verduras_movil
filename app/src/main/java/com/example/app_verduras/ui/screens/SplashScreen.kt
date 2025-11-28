package com.example.app_verduras.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.app_verduras.BuildConfig
import com.example.app_verduras.R
import com.example.app_verduras.Screen
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(navController: NavController) {
    // Estado de la composición Lottie
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.inicio_aplicacion_interactive)
    )
    
    // Control de progreso de la animación Lottie
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 1f,
        restartOnPlay = true
    )
    
    // Estados de carga
    var startAnimation by remember { mutableStateOf(false) }
    var isSessionChecked by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("Iniciando...") }
    var destination by remember { mutableStateOf<String?>(null) }
    
    // Animación de escala con rebote
    val animationScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animationScale"
    )
    
    // Animación de aparición
    val animationAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "animationAlpha"
    )
    
    // Animación del texto con delay
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400),
        label = "textAlpha"
    )

    // Animación de pulso infinita para el indicador de carga
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val loadingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadingAlpha"
    )

    // Efecto para verificar sesión de forma asíncrona
    LaunchedEffect(Unit) {
        startAnimation = true
        loadingMessage = "Verificando sesión..."
        
        // Verificar sesión de forma asíncrona
        val sessionDestination = withContext(Dispatchers.IO) {
            // Simular pequeño delay para mostrar la animación
            delay(500)
            
            when {
                SessionManager.isLoggedIn() && SessionManager.isAdmin() -> Screen.AdminPanel.route
                SessionManager.isLoggedIn() -> Screen.Home.route
                else -> Screen.Login.route
            }
        }
        
        destination = sessionDestination
        isSessionChecked = true
        
        // Mensaje dinámico según destino
        loadingMessage = when (sessionDestination) {
            Screen.AdminPanel.route -> "Cargando panel de admin..."
            Screen.Home.route -> "¡Bienvenido de vuelta!"
            else -> "Preparando todo..."
        }
        
        // Delay dinámico: más corto si ya está logueado
        val remainingDelay = if (SessionManager.isLoggedIn()) 1500L else 2000L
        delay(remainingDelay)
        
        loadingMessage = "¡Listo!"
        delay(300) // Pequeña pausa antes de navegar
        
        navController.navigate(sessionDestination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HuertoHogarColors.Primary.copy(alpha = 0.1f),
                        HuertoHogarColors.Background,
                        HuertoHogarColors.AccentWarm.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animación Lottie únicamente (sin fallback de imagen)
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(animationScale)
                    .alpha(animationAlpha),
                contentAlignment = Alignment.Center
            ) {
                // Solo animación Lottie, sin icono fallback
                LottieAnimation(
                    composition = composition,
                    progress = { lottieProgress },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Texto del nombre de la app
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha)
            ) {
                Text(
                    text = "Huerto Hogar",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = HuertoHogarColors.Primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Del campo a tu hogar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = HuertoHogarColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Indicador de carga dinámico
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(loadingAlpha)
            ) {
                Text(
                    text = loadingMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HuertoHogarColors.TextSecondary
                )
                
                // Barra de progreso visual
                if (!isSessionChecked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LoadingDots()
                }
            }
        }
        
        // Versión dinámica de la app en la parte inferior
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = HuertoHogarColors.TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

/**
 * Componente de puntos de carga animados
 */
@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot$index"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(
                        color = HuertoHogarColors.Primary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}
