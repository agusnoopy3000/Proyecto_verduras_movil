package com.example.app_verduras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.app_verduras.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Correctly collect the state using the lifecycle-aware collector
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // This effect will react to user messages from the ViewModel
    LaunchedEffect(authState.userMessage) {
        authState.userMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            // Notify the ViewModel that the message has been shown to prevent it from re-appearing
            authViewModel.onUserMessageShown()
        }
    }

    // Reset state when the screen is first composed or recomposed with a new viewModel
    LaunchedEffect(Unit) {
        authViewModel.resetAuthState()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Recuperar Contraseña",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    // Error state is now handled by the Snackbar
                    isError = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        authViewModel.forgotPassword(email)
                    },
                    enabled = !authState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Enviar Instrucciones")
                    }
                }

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Volver al Login")
                }
            }
        }
    }
}
