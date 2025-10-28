package com.example.app_verduras.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.app_verduras.R
import com.example.app_verduras.Screen
import com.example.app_verduras.util.SessionManager
import com.example.app_verduras.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Reset state when the screen is first composed
    LaunchedEffect(Unit) {
        authViewModel.resetAuthState()
    }

    LaunchedEffect(authState.userMessage) {
        authState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            authViewModel.onUserMessageShown()
        }
    }

    val loginAnimationComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.login_interactive)
    )
    val footerAnimationComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.footer_app_movil)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LottieAnimation(
                        composition = loginAnimationComposition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Text(
                        text = "Inicio Sesión",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        singleLine = true
                    )

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.End)
                            .clickable { navController.navigate(Screen.ForgotPassword.route) },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { authViewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !authState.isLoading
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Ingresar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¿No tienes una cuenta? Regístrate aquí",
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { navController.navigate(Screen.Register.route) }
                    )
                }

                LottieAnimation(
                    composition = footerAnimationComposition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            authState.user?.let { user ->
                SessionManager.login(user)
            }
            val destination = if (authState.user?.email?.equals("admin@test.com", ignoreCase = true) == true) {
                Screen.WelcomeAdmin.route
            } else {
                Screen.WelcomeUser.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
}
