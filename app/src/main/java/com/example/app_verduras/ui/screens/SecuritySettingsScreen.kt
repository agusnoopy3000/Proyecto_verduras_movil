package com.example.app_verduras.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.app_verduras.Screen
import com.example.app_verduras.auth.FirebaseMFAManager
import com.example.app_verduras.viewmodel.AuthViewModel

/**
 * Pantalla de configuración de seguridad.
 * Permite al usuario:
 * - Habilitar/deshabilitar la verificación de dos pasos (MFA con SMS)
 * - Ver los números de teléfono registrados para MFA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Verificar si MFA está habilitado
    val isMFAEnabled = remember { mutableStateOf(authViewModel.isUserEnrolledInMFA()) }
    val enrolledFactors = remember { mutableStateOf(authViewModel.getEnrolledMFAFactors()) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estado para el diálogo de confirmación
    var showDisableDialog by remember { mutableStateOf(false) }
    var showEnableDialog by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    
    // Setear activity para MFA
    LaunchedEffect(Unit) {
        (context as? Activity)?.let { authViewModel.setActivity(it) }
    }
    
    // Manejar mensajes
    LaunchedEffect(authState.userMessage) {
        authState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            authViewModel.onUserMessageShown()
            // Actualizar estado después de un cambio
            isMFAEnabled.value = authViewModel.isUserEnrolledInMFA()
            enrolledFactors.value = authViewModel.getEnrolledMFAFactors()
        }
    }
    
    // Gradiente de fondo
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.background
    )
    
    Scaffold(
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Seguridad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header de seguridad
            SecurityHeader()
            
            // Card de verificación de dos pasos
            TwoFactorAuthCard(
                isEnabled = isMFAEnabled.value,
                enrolledPhones = enrolledFactors.value,
                onEnableClick = { showEnableDialog = true },
                onDisableClick = { showDisableDialog = true }
            )
            
            // Tips de seguridad
            SecurityTipsCard()
        }
    }
    
    // Diálogo para habilitar MFA
    if (showEnableDialog) {
        EnableMFADialog(
            phoneNumber = phoneNumber,
            onPhoneNumberChange = { phoneNumber = it },
            onConfirm = {
                showEnableDialog = false
                // Navegar a la pantalla de verificación en modo enrolamiento
                navController.navigate(Screen.PhoneVerification.createRoute("enrollment"))
                // Iniciar el proceso de enrolamiento
                authViewModel.startMFAEnrollment(phoneNumber)
            },
            onDismiss = { showEnableDialog = false }
        )
    }
    
    // Diálogo para deshabilitar MFA
    if (showDisableDialog) {
        DisableMFADialog(
            onConfirm = {
                showDisableDialog = false
                authViewModel.disableMFA()
            },
            onDismiss = { showDisableDialog = false }
        )
    }
}

@Composable
private fun SecurityHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Column {
            Text(
                text = "Protege tu cuenta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configura opciones de seguridad adicionales",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TwoFactorAuthCard(
    isEnabled: Boolean,
    enrolledPhones: List<String>,
    onEnableClick: () -> Unit,
    onDisableClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = if (isEnabled) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Column {
                        Text(
                            text = "Verificación en dos pasos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isEnabled) "Activada" else "Desactivada",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // Indicador de estado
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isEnabled) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                )
            }
            
            Divider()
            
            Text(
                text = if (isEnabled) 
                    "Tu cuenta está protegida con verificación por SMS. Cada vez que inicies sesión, recibirás un código en tu teléfono."
                else 
                    "Añade una capa extra de seguridad a tu cuenta. Recibirás un código SMS cada vez que inicies sesión.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            // Mostrar teléfonos registrados
            if (isEnabled && enrolledPhones.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Teléfonos registrados:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    enrolledPhones.forEach { phone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = phone,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Botón de acción
            Button(
                onClick = if (isEnabled) onDisableClick else onEnableClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.RemoveCircle else Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnabled) "Desactivar verificación" else "Activar verificación",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SecurityTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Consejos de seguridad",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            SecurityTip(
                icon = Icons.Default.Lock,
                text = "Usa contraseñas únicas y seguras"
            )
            SecurityTip(
                icon = Icons.Default.PhoneAndroid,
                text = "Activa la verificación en dos pasos"
            )
            SecurityTip(
                icon = Icons.Default.Visibility,
                text = "No compartas tus credenciales"
            )
            SecurityTip(
                icon = Icons.Default.Wifi,
                text = "Evita redes WiFi públicas"
            )
        }
    }
}

@Composable
private fun SecurityTip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun EnableMFADialog(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Activar verificación") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Ingresa tu número de teléfono para recibir códigos de verificación.")
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Número de teléfono") },
                    placeholder = { Text("+56 9 1234 5678") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Incluye el código de país (ej: +56 para Chile)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = phoneNumber.length >= 10
            ) {
                Text("Continuar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DisableMFADialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("¿Desactivar verificación?") },
        text = {
            Text(
                "Tu cuenta será menos segura sin la verificación en dos pasos. " +
                "¿Estás seguro de que deseas desactivarla?"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Desactivar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
