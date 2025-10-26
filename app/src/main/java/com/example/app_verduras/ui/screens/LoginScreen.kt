package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.app_verduras.R
import com.example.app_verduras.ui.components.AppFooter
import com.example.app_verduras.viewmodel.AuthViewModel
import com.example.app_verduras.viewmodel.AuthState

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onAdminLoginSuccess: () -> Unit, // Parámetro para el admin
    onNavigateToRegister: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    // Lottie Compositions
    val loginComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.login_interactive))

    LaunchedEffect(uiState) {
        if (uiState is AuthState.Authenticated) {
            onLoginSuccess()
        } else if (uiState is AuthState.Error) {
            showError = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.weight(1f))

        // --- Animación Superior ---
        LottieAnimation(
            composition = loginComposition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Formulario de Inicio de Sesión ---
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (showError && uiState is AuthState.Error) {
            Text(
                text = (uiState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                showError = false
                if (email.trim() == "admin@test.com" && password == "admin") {
                    onAdminLoginSuccess()
                } else {
                    authViewModel.login(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is AuthState.Loading
        ) {
            if (uiState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Ingresar")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate")
        }
        // --- Fin del Formulario ---

        Spacer(modifier = Modifier.weight(1.5f))

        // --- Animación Inferior (Footer) ---
        AppFooter()
        Spacer(modifier = Modifier.height(16.dp))
    }
}
