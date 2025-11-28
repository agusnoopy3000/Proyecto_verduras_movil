package com.example.app_verduras.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Tipos de notificación para el Toast
 */
enum class ToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    ORDER_UPDATE,
    SYNC_SUCCESS,
    SYNC_ERROR
}

/**
 * Datos para mostrar en el Toast
 */
data class ToastData(
    val message: String,
    val type: ToastType = ToastType.SUCCESS,
    val icon: ImageVector? = null,
    val durationMs: Long = 2500L
)

/**
 * Composable de Toast animado que aparece en la parte superior
 * y desaparece automáticamente después de unos segundos.
 */
@Composable
fun AnimatedSuccessToast(
    toastData: ToastData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(toastData) {
        if (toastData != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isVisible = true
            delay(toastData.durationMs)
            isVisible = false
            delay(300) // Esperar a que termine la animación de salida
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible && toastData != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        toastData?.let { data ->
            ToastContent(data = data)
        }
    }
}

@Composable
private fun ToastContent(data: ToastData) {
    val (backgroundColor, iconColor, icon, title) = when (data.type) {
        ToastType.SUCCESS -> ToastStyle(
            background = Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF43A047))),
            iconColor = Color.White,
            icon = data.icon ?: Icons.Default.CheckCircle,
            title = "¡Éxito!"
        )
        ToastType.ERROR -> ToastStyle(
            background = Brush.horizontalGradient(listOf(Color(0xFFC62828), Color(0xFFE53935))),
            iconColor = Color.White,
            icon = data.icon ?: Icons.Default.Error,
            title = "Error"
        )
        ToastType.WARNING -> ToastStyle(
            background = Brush.horizontalGradient(listOf(Color(0xFFF57C00), Color(0xFFFF9800))),
            iconColor = Color.White,
            icon = data.icon ?: Icons.Default.Warning,
            title = "Atención"
        )
        ToastType.INFO -> ToastStyle(
            background = Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF1E88E5))),
            iconColor = Color.White,
            icon = data.icon ?: Icons.Default.Info,
            title = "Información"
        )
        ToastType.ORDER_UPDATE -> ToastStyle(
            background = Brush.horizontalGradient(listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA))),
            iconColor = Color.White,
            icon = data.icon ?: Icons.Default.LocalShipping,
            title = "Pedido"
        )
        ToastType.SYNC_SUCCESS -> ToastStyle(
            background = Brush.horizontalGradient(listOf(Color(0xFF00695C), Color(0xFF00897B))),
            iconColor = Color.White,
            icon = data.icon ?: Icons.Default.CloudDone,
            title = "Sincronizado"
        )
        ToastType.SYNC_ERROR -> ToastStyle(
            background = Brush.horizontalGradient(listOf(Color(0xFF455A64), Color(0xFF607D8B))),
            iconColor = Color.White,
            icon = data.icon ?: Icons.Default.CloudOff,
            title = "Sin conexión"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Texto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = data.message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class ToastStyle(
    val background: Brush,
    val iconColor: Color,
    val icon: ImageVector,
    val title: String
)

/**
 * Estado para manejar el Toast
 */
class ToastState {
    var currentToast by mutableStateOf<ToastData?>(null)
        private set
    
    fun show(message: String, type: ToastType = ToastType.SUCCESS, icon: ImageVector? = null) {
        currentToast = ToastData(message, type, icon)
    }
    
    fun showSuccess(message: String) {
        currentToast = ToastData(message, ToastType.SUCCESS, Icons.Default.CheckCircle)
    }
    
    fun showError(message: String) {
        currentToast = ToastData(message, ToastType.ERROR, Icons.Default.Error)
    }
    
    fun showWarning(message: String) {
        currentToast = ToastData(message, ToastType.WARNING, Icons.Default.Warning)
    }
    
    fun showInfo(message: String) {
        currentToast = ToastData(message, ToastType.INFO, Icons.Default.Info)
    }
    
    fun showOrderUpdate(message: String) {
        currentToast = ToastData(message, ToastType.ORDER_UPDATE, Icons.Default.LocalShipping)
    }
    
    fun showSyncSuccess(message: String = "Sincronizado correctamente") {
        currentToast = ToastData(message, ToastType.SYNC_SUCCESS, Icons.Default.CloudDone)
    }
    
    fun showSyncError(message: String = "Error de sincronización") {
        currentToast = ToastData(message, ToastType.SYNC_ERROR, Icons.Default.CloudOff)
    }
    
    fun dismiss() {
        currentToast = null
    }
}

@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}
