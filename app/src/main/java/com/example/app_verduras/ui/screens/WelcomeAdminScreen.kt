package com.example.app_verduras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.app_verduras.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeAdminScreen(onTimeout: () -> Unit) {
    // Este efecto se lanza una sola vez cuando el composable aparece
    LaunchedEffect(Unit) {
        delay(4000) // Espera 4 segundos para dar tiempo a la animación
        onTimeout() // Ejecuta la acción de navegar a la siguiente pantalla
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Carga la animación desde los recursos raw.
        // Asegúrate de que el archivo se llama "admin.json" en la carpeta res/raw.
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.admin))

        // Muestra la animación
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever, // Repite la animación
            modifier = Modifier.size(250.dp) // Ajusta el tamaño según sea necesario
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bienvenido al Panel\nde Administrador",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
