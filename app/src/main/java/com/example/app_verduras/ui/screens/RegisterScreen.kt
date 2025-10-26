package com.example.app_verduras.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.app_verduras.viewmodel.AuthViewModel
import com.example.app_verduras.viewmodel.AuthState

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    // Resetea el estado si el usuario llega a esta pantalla
    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthState.Authenticated) {
            onRegisterSuccess()
        }
    }

    // Lógica para determinar qué campo tiene error
    val errorState = uiState as? AuthState.Error
    val errorMessage = errorState?.message?.lowercase() ?: ""

    val genericEmptyError = "campos son obligatorios" in errorMessage
    val isNombreError = genericEmptyError && nombre.isBlank()
    val isApellidoError = genericEmptyError && apellido.isBlank()
    val isEmailError = "correo" in errorMessage || (genericEmptyError && email.isBlank())
    val isDireccionError = genericEmptyError && direccion.isBlank()
    val isTelefonoError = "teléfono" in errorMessage || (genericEmptyError && telefono.isBlank())
    val isPasswordError = "contraseña" in errorMessage || (genericEmptyError && password.isBlank())


    // Se envuelve todo en una Surface para que tome el color de fondo del tema
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- CAMPOS CON VALIDACIÓN Y TRAILING ICON ---

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isNombreError,
                trailingIcon = {
                    AnimatedVisibility(visible = isNombreError) {
                        Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isApellidoError,
                trailingIcon = {
                    AnimatedVisibility(visible = isApellidoError) {
                        Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = isEmailError,
                trailingIcon = {
                    AnimatedVisibility(visible = isEmailError) {
                        Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isDireccionError,
                trailingIcon = {
                    AnimatedVisibility(visible = isDireccionError) {
                        Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = isTelefonoError,
                trailingIcon = {
                    AnimatedVisibility(visible = isTelefonoError) {
                        Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = isPasswordError,
                trailingIcon = {
                    AnimatedVisibility(visible = isPasswordError) {
                        Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // --- MENSAJE DE ERROR GENERAL ANIMADO ---

            AnimatedVisibility(
                visible = errorState != null,
                enter = slideInVertically { fullHeight -> -fullHeight } + fadeIn(),
                exit = slideOutVertically { fullHeight -> -fullHeight } + fadeOut()
            ) {
                Text(
                    text = errorState?.message ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    authViewModel.register(nombre, apellido, email, password, direccion, telefono)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthState.Loading
            ) {
                if (uiState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Registrarse")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
