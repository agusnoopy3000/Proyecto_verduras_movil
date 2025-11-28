package com.example.app_verduras.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.app_verduras.R
import com.example.app_verduras.auth.FirebaseMFAManager
import com.example.app_verduras.viewmodel.AuthViewModel
import com.example.app_verduras.viewmodel.MFAMode
import kotlinx.coroutines.delay

/**
 * Pantalla de verificación por SMS para MFA.
 * 
 * Soporta dos modos:
 * - ENROLLMENT: Cuando el usuario está habilitando MFA por primera vez
 * - VERIFICATION: Cuando el usuario está verificando su segundo factor durante login
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneVerificationScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    mode: MFAMode = MFAMode.VERIFICATION
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val mfaState by authViewModel.mfaState.collectAsStateWithLifecycle()
    
    var verificationCode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var canResend by remember { mutableStateOf(false) }
    var resendCountdown by remember { mutableIntStateOf(60) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    
    // Animación Lottie para verificación
    val securityAnimation by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.login_interactive)
    )
    
    // Countdown para reenvío
    LaunchedEffect(Unit) {
        while (resendCountdown > 0) {
            delay(1000)
            resendCountdown--
        }
        canResend = true
    }
    
    // Manejar mensajes de estado
    LaunchedEffect(authState.userMessage) {
        authState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            authViewModel.onUserMessageShown()
        }
    }
    
    // Manejar éxito de MFA
    LaunchedEffect(mfaState) {
        if (mfaState is FirebaseMFAManager.MFAState.Success) {
            when (mode) {
                MFAMode.ENROLLMENT -> {
                    snackbarHostState.showSnackbar("✅ Verificación de dos pasos activada")
                    delay(1500)
                    navController.popBackStack()
                }
                MFAMode.VERIFICATION -> {
                    // El login ya fue completado, navegar según rol
                    authState.user?.let { user ->
                        if (user.rol.equals("admin", ignoreCase = true)) {
                            navController.navigate("welcome_admin") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("welcome_user") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Gradiente de fondo
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.background,
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
                title = { 
                    Text(
                        text = if (mode == MFAMode.ENROLLMENT) 
                            "Activar verificación" else "Verificación requerida"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Icono/Animación
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(60.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Título y descripción
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (mode == MFAMode.ENROLLMENT) 
                            "Verificación de dos pasos" else "Ingresa el código",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Text(
                        text = when (mfaState) {
                            is FirebaseMFAManager.MFAState.CodeSent -> {
                                val phone = (mfaState as FirebaseMFAManager.MFAState.CodeSent).phoneNumber
                                "Enviamos un código SMS a\n$phone"
                            }
                            is FirebaseMFAManager.MFAState.MFARequired -> {
                                "Ingresa el código enviado a tu teléfono"
                            }
                            else -> {
                                if (mode == MFAMode.ENROLLMENT) 
                                    "Ingresa tu número de teléfono para recibir el código"
                                else 
                                    "Necesitamos verificar tu identidad"
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contenido según el estado
                when (mfaState) {
                    is FirebaseMFAManager.MFAState.Idle,
                    is FirebaseMFAManager.MFAState.MFARequired -> {
                        if (mode == MFAMode.ENROLLMENT) {
                            // Mostrar input de teléfono para enrolamiento
                            PhoneNumberInput(
                                phoneNumber = phoneNumber,
                                onPhoneNumberChange = { phoneNumber = it },
                                onSendCode = {
                                    // Llamar al ViewModel para enviar código
                                    // authViewModel.startMFAEnrollment(phoneNumber, activity)
                                }
                            )
                        } else {
                            // Mostrar opciones de verificación
                            val hints = (mfaState as? FirebaseMFAManager.MFAState.MFARequired)?.hints ?: emptyList()
                            MFAOptionsCard(
                                hints = hints,
                                onOptionSelected = { index ->
                                    // Enviar código al número seleccionado
                                    // authViewModel.sendMFAVerificationCode(index, activity)
                                }
                            )
                        }
                    }
                    
                    is FirebaseMFAManager.MFAState.SendingCode -> {
                        // Indicador de carga
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Enviando código SMS...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    
                    is FirebaseMFAManager.MFAState.CodeSent -> {
                        // Input del código de verificación
                        OTPInputField(
                            code = verificationCode,
                            onCodeChange = { 
                                if (it.length <= 6) {
                                    verificationCode = it
                                }
                            },
                            focusRequester = focusRequester
                        )
                        
                        // Auto-verificar cuando se ingresan 6 dígitos
                        LaunchedEffect(verificationCode) {
                            if (verificationCode.length == 6) {
                                authViewModel.verifyMFACode(verificationCode)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Botón verificar
                        Button(
                            onClick = {
                                authViewModel.verifyMFACode(verificationCode)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = verificationCode.length == 6 && 
                                      mfaState !is FirebaseMFAManager.MFAState.VerifyingCode,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (mfaState is FirebaseMFAManager.MFAState.VerifyingCode) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verificar código",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        // Reenviar código
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (canResend) {
                            TextButton(
                                onClick = {
                                    canResend = false
                                    resendCountdown = 60
                                    verificationCode = ""
                                    authViewModel.resendMFACode()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reenviar código")
                            }
                        } else {
                            Text(
                                text = "Reenviar código en ${resendCountdown}s",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    is FirebaseMFAManager.MFAState.VerifyingCode -> {
                        OTPInputField(
                            code = verificationCode,
                            onCodeChange = { },
                            focusRequester = focusRequester,
                            enabled = false
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "Verificando...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    
                    is FirebaseMFAManager.MFAState.Success -> {
                        // Animación de éxito
                        SuccessAnimation()
                    }
                    
                    is FirebaseMFAManager.MFAState.Error -> {
                        val error = (mfaState as FirebaseMFAManager.MFAState.Error).message
                        
                        OTPInputField(
                            code = verificationCode,
                            onCodeChange = { 
                                if (it.length <= 6) {
                                    verificationCode = it
                                }
                            },
                            focusRequester = focusRequester,
                            isError = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = {
                                verificationCode = ""
                                authViewModel.resetMFAState()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Intentar de nuevo")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Campo de entrada para el código OTP de 6 dígitos.
 */
@Composable
private fun OTPInputField(
    code: String,
    onCodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        code.length == 6 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    // Focus automático
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    BasicTextField(
        value = code,
        onValueChange = { value ->
            if (value.length <= 6 && value.all { it.isDigit() }) {
                onCodeChange(value)
            }
        },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier.focusRequester(focusRequester),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val char = code.getOrNull(index)?.toString() ?: ""
                    val isFocused = code.length == index
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (char.isNotEmpty())
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    borderColor,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        // Cursor parpadeante
                        if (isFocused && enabled) {
                            val infiniteTransition = rememberInfiniteTransition(label = "cursor")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "cursor_alpha"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(28.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                                    )
                            )
                        }
                    }
                }
            }
        }
    )
}

/**
 * Input para número de teléfono (usado en enrolamiento).
 */
@Composable
private fun PhoneNumberInput(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onSendCode: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Número de teléfono") },
            placeholder = { Text("+56 9 1234 5678") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Text(
            text = "Incluye el código de país (ej: +56 para Chile)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        Button(
            onClick = onSendCode,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = phoneNumber.length >= 10,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enviar código SMS")
        }
    }
}

/**
 * Card con opciones de verificación MFA.
 */
@Composable
private fun MFAOptionsCard(
    hints: List<String>,
    onOptionSelected: (Int) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Selecciona un método de verificación:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            hints.forEachIndexed { index, hint ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOptionSelected(index) }
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SMS",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Animación de éxito.
 */
@Composable
private fun SuccessAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "success")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .clip(RoundedCornerShape(40.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = "¡Verificación exitosa!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Redirigiendo...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}
