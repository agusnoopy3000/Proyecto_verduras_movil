package com.example.app_verduras.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.example.app_verduras.R
import com.example.app_verduras.Screen
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.util.SessionManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.inicio_aplicacion_interactive)
    )
    
    // Animaciones
    var startAnimation by remember { mutableStateOf(false) }
    
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "logoAlpha"
    )
    
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400),
        label = "textAlpha"
    )

    // Animación de pulso para el indicador de carga
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

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000) // Esperar 3 segundos
        
        // Verificar si hay sesión activa
        val destination = if (SessionManager.isLoggedIn()) {
            if (SessionManager.isAdmin()) {
                Screen.AdminPanel.route
            } else {
                Screen.Home.route
            }
        } else {
            Screen.Login.route
        }
        
        navController.navigate(destination) {
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
                        MaterialTheme.colorScheme.background,
                        HuertoHogarColors.AccentWarm
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo con animación de escala
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = "file:///android_asset/img/huerto_hogar.jpeg")
                        .crossfade(true)
                        .build()
                )

                Image(
                    painter = painter,
                    contentDescription = "Logo de la aplicación",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            // Animación Lottie
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .size(200.dp)
                    .alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Indicador de carga
            Text(
                text = "Cargando...",
                style = MaterialTheme.typography.bodyMedium,
                color = HuertoHogarColors.TextSecondary,
                modifier = Modifier.alpha(loadingAlpha)
            )
        }
        
        // Versión de la app en la parte inferior
        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = HuertoHogarColors.TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
