package com.example.app_verduras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.app_verduras.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Este efecto se lanza una sola vez cuando el composable aparece
    LaunchedEffect(Unit) {
        delay(4000) // Espera 4 segundos
        onTimeout() // Llama a la función para navegar a la siguiente pantalla
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Carga la animación desde los recursos raw.
        // Asegúrate de que el archivo se llama "inicio_aplicacion_interactive.json" en res/raw.
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.inicio_aplicacion_interactive))

        // Muestra la animación a pantalla completa
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever, // Repite la animación
            modifier = Modifier.fillMaxSize()
        )
    }
}
