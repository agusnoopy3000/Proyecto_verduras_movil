package com.example.app_verduras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.app_verduras.Screen
import com.example.app_verduras.ui.components.EnhancedSnackbarHost
import com.example.app_verduras.ui.components.MessageType
import com.example.app_verduras.ui.components.EnhancedMessage
import com.example.app_verduras.ui.components.rememberEnhancedSnackbarState
import com.example.app_verduras.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var run by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    // Estados de validación en tiempo real
    val isRunValid by remember(run) { 
        derivedStateOf { 
            run.isEmpty() || run.matches(Regex("^\\d{1,2}\\.?\\d{3}\\.?\\d{3}-?[\\dkK]$"))
        }
    }
    val isEmailValid by remember(email) { 
        derivedStateOf { 
            email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }
    val isPasswordValid by remember(password) { 
        derivedStateOf { 
            password.isEmpty() || (password.length >= 8 && password.any { !it.isLetterOrDigit() })
        }
    }
    val isNombreValid by remember(nombre) { 
        derivedStateOf { nombre.isEmpty() || nombre.length >= 2 }
    }
    val isApellidosValid by remember(apellidos) { 
        derivedStateOf { apellidos.isEmpty() || apellidos.length >= 2 }
    }
    val isTelefonoValid by remember(telefono) {
        derivedStateOf { 
            // Acepta vacío, o formato +56XXXXXXXXX o 9XXXXXXXX (chileno)
            telefono.isEmpty() || 
            telefono.matches(Regex("^\\+56\\d{9}$")) || // +56912345678
            telefono.matches(Regex("^9\\d{8}$")) ||      // 912345678
            telefono.matches(Regex("^\\d{8,9}$"))        // 12345678 o 912345678
        }
    }
    
    // Función para formatear el teléfono antes de enviar
    fun formatTelefono(tel: String): String {
        if (tel.isEmpty()) return ""
        val cleaned = tel.replace(Regex("[^\\d]"), "")
        return when {
            tel.startsWith("+56") -> tel
            cleaned.startsWith("56") && cleaned.length == 11 -> "+$cleaned"
            cleaned.length == 9 && cleaned.startsWith("9") -> "+56$cleaned"
            cleaned.length == 8 -> "+569$cleaned"
            else -> "+56$cleaned"
        }
    }

    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val enhancedSnackbarState = rememberEnhancedSnackbarState()

    LaunchedEffect(authState.userMessage) {
        authState.userMessage?.let { message ->
            val isError = message.contains("error", ignoreCase = true) || 
                         message.contains("existe", ignoreCase = true) ||
                         message.contains("inválido", ignoreCase = true)
            val isSuccess = message.contains("éxito", ignoreCase = true) || 
                           message.contains("creada", ignoreCase = true)
            enhancedSnackbarState.show(
                EnhancedMessage(
                    text = message,
                    type = when {
                        isError -> MessageType.ERROR
                        isSuccess -> MessageType.SUCCESS
                        else -> MessageType.INFO
                    },
                    emoji = when {
                        isError -> "❌"
                        isSuccess -> "🎉"
                        else -> "ℹ️"
                    }
                )
            )
            authViewModel.onUserMessageShown()
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate(Screen.WelcomeUser.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
    )

    Scaffold(
        snackbarHost = { }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Header con icono
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Crear Cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Únete a Huerto Hogar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Card del formulario
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sección: Datos Personales
                        SectionHeader(icon = Icons.Default.Person, title = "Datos Personales")

                        StyledTextField(
                            value = run,
                            onValueChange = { run = it },
                            label = "RUT",
                            placeholder = "19.011.022-K",
                            leadingIcon = Icons.Default.Badge,
                            supportingText = "Formato: XX.XXX.XXX-X",
                            isError = run.isNotEmpty() && !isRunValid,
                            errorText = "Formato de RUT inválido"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StyledTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = "Nombre",
                                placeholder = "Juan",
                                leadingIcon = Icons.Default.Person,
                                modifier = Modifier.weight(1f),
                                isError = nombre.isNotEmpty() && !isNombreValid,
                                errorText = "Mínimo 2 caracteres"
                            )
                            StyledTextField(
                                value = apellidos,
                                onValueChange = { apellidos = it },
                                label = "Apellidos",
                                placeholder = "Pérez",
                                leadingIcon = Icons.Default.People,
                                modifier = Modifier.weight(1f),
                                isError = apellidos.isNotEmpty() && !isApellidosValid,
                                errorText = "Mínimo 2 caracteres"
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Sección: Credenciales
                        SectionHeader(icon = Icons.Default.Security, title = "Credenciales")

                        StyledTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Correo electrónico",
                            placeholder = "ejemplo@email.com",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            isError = email.isNotEmpty() && !isEmailValid,
                            errorText = "Correo electrónico inválido"
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            placeholder = { Text("Mín. 8 caracteres") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock, 
                                    null, 
                                    tint = if (password.isNotEmpty() && !isPasswordValid) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            isError = password.isNotEmpty() && !isPasswordValid,
                            supportingText = {
                                if (password.isNotEmpty() && !isPasswordValid) {
                                    Text(
                                        "Requiere mín. 8 caracteres y 1 especial (!@#\$...)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        "Mín. 8 caracteres + 1 especial (!@#\$...)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (password.isNotEmpty() && !isPasswordValid) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (password.isNotEmpty() && !isPasswordValid) 
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f) 
                                else 
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Sección: Contacto (Opcional)
                        SectionHeader(icon = Icons.Default.ContactPhone, title = "Contacto (Opcional)")

                        StyledTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = "Dirección",
                            placeholder = "Calle 123, Comuna",
                            leadingIcon = Icons.Default.LocationOn
                        )

                        StyledTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = "Teléfono",
                            placeholder = "912345678",
                            leadingIcon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            isError = telefono.isNotEmpty() && !isTelefonoValid,
                            errorText = "Formato: 912345678 o +56912345678",
                            supportingText = "Se añadirá +56 automáticamente"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Términos y condiciones
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            "Acepto los términos y condiciones de uso",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón de registro
                Button(
                    onClick = {
                        authViewModel.register(
                            run = run,
                            nombre = nombre,
                            apellidos = apellidos,
                            email = email,
                            password = password,
                            direccion = direccion.ifBlank { null },
                            telefono = if (telefono.isBlank()) null else formatTelefono(telefono)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !authState.isLoading && termsAccepted && 
                              run.isNotBlank() && nombre.isNotBlank() && 
                              apellidos.isNotBlank() && email.isNotBlank() && password.isNotBlank() &&
                              isRunValid && isEmailValid && isPasswordValid && 
                              isNombreValid && isApellidosValid && 
                              (telefono.isEmpty() || isTelefonoValid),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.HowToReg,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Crear Cuenta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Link a login
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "¿Ya tienes una cuenta? ",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text(
                            "Inicia Sesión",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Snackbar mejorado
            EnhancedSnackbarHost(
                state = enhancedSnackbarState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingText: String? = null,
    isError: Boolean = false,
    errorText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(leadingIcon, null, tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = {
            when {
                isError && errorText != null -> Text(
                    errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                supportingText != null -> Text(
                    supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}
