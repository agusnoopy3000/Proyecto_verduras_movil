package com.example.app_verduras.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Tipos de mensajes con estilos predefinidos
 */
enum class MessageType {
    SUCCESS,    // ✓ Operación exitosa
    ERROR,      // ✗ Error
    WARNING,    // ⚠ Advertencia
    INFO,       // ℹ Información
    CART_ADD,   // 🛒 Producto agregado
    CART_REMOVE,// 🗑 Producto eliminado
    ORDER,      // 📦 Estado de pedido
    AUTH,       // 🔐 Autenticación
    SYNC        // ☁ Sincronización
}

/**
 * Datos del mensaje mejorado
 */
data class EnhancedMessage(
    val text: String,
    val type: MessageType = MessageType.INFO,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val duration: Long = 3000L,
    val emoji: String? = null
)

/**
 * Estado para manejar mensajes mejorados
 */
class EnhancedSnackbarState {
    var currentMessage by mutableStateOf<EnhancedMessage?>(null)
        private set
    
    var isVisible by mutableStateOf(false)
        private set
    
    fun show(message: EnhancedMessage) {
        currentMessage = message
        isVisible = true
    }
    
    fun dismiss() {
        isVisible = false
    }
    
    // Métodos de conveniencia
    fun showSuccess(text: String, emoji: String = "✓") = show(
        EnhancedMessage(text, MessageType.SUCCESS, emoji = emoji)
    )
    
    fun showError(text: String, emoji: String = "✗") = show(
        EnhancedMessage(text, MessageType.ERROR, emoji = emoji, duration = 4000L)
    )
    
    fun showWarning(text: String, emoji: String = "⚠") = show(
        EnhancedMessage(text, MessageType.WARNING, emoji = emoji)
    )
    
    fun showInfo(text: String, emoji: String = "ℹ") = show(
        EnhancedMessage(text, MessageType.INFO, emoji = emoji)
    )
    
    fun showCartAdd(productName: String) = show(
        EnhancedMessage(
            text = "$productName agregado al carrito",
            type = MessageType.CART_ADD,
            emoji = "🛒",
            actionLabel = "Ver carrito"
        )
    )
    
    fun showCartRemove(productName: String) = show(
        EnhancedMessage(
            text = "$productName eliminado",
            type = MessageType.CART_REMOVE,
            emoji = "🗑"
        )
    )
    
    fun showOrderStatus(status: String, orderId: String) = show(
        EnhancedMessage(
            text = "Pedido #$orderId: $status",
            type = MessageType.ORDER,
            emoji = "📦"
        )
    )
    
    fun showAuthMessage(text: String, isError: Boolean = false) = show(
        EnhancedMessage(
            text = text,
            type = if (isError) MessageType.ERROR else MessageType.AUTH,
            emoji = if (isError) "🔒" else "🔓"
        )
    )
    
    fun showSyncStatus(isSynced: Boolean) = show(
        EnhancedMessage(
            text = if (isSynced) "Sincronizado con la nube" else "Error de sincronización",
            type = if (isSynced) MessageType.SYNC else MessageType.ERROR,
            emoji = if (isSynced) "☁️" else "⚠️"
        )
    )
}

@Composable
fun rememberEnhancedSnackbarState(): EnhancedSnackbarState {
    return remember { EnhancedSnackbarState() }
}

/**
 * Snackbar mejorado con animaciones y estilos por tipo
 */
@Composable
fun EnhancedSnackbarHost(
    state: EnhancedSnackbarState,
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(state.isVisible, state.currentMessage) {
        if (state.isVisible && state.currentMessage != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(state.currentMessage!!.duration)
            state.dismiss()
        }
    }
    
    AnimatedVisibility(
        visible = state.isVisible && state.currentMessage != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut(),
        modifier = modifier
    ) {
        state.currentMessage?.let { message ->
            EnhancedSnackbarContent(
                message = message,
                onDismiss = { state.dismiss() },
                onAction = {
                    message.onAction?.invoke()
                    onActionClick?.invoke()
                    state.dismiss()
                }
            )
        }
    }
}

@Composable
private fun EnhancedSnackbarContent(
    message: EnhancedMessage,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    val colors = getMessageColors(message.type)
    val icon = getMessageIcon(message.type)
    
    // Animación de entrada del icono
    var iconVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        iconVisible = true
    }
    
    val iconScale by animateFloatAsState(
        targetValue = if (iconVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = colors.container.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            colors.container,
                            colors.container.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono con animación
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.onContainer.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (message.emoji != null) {
                    Text(
                        text = message.emoji,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.animateContentSize()
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.onContainer,
                        modifier = Modifier
                            .size(24.dp)
                            .animateContentSize()
                    )
                }
            }
            
            // Texto del mensaje
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = getMessageTitle(message.type),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onContainer.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onContainer,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Botón de acción (si existe)
            if (message.actionLabel != null) {
                TextButton(
                    onClick = onAction,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colors.onContainer
                    )
                ) {
                    Text(
                        text = message.actionLabel,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            // Botón de cerrar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = colors.onContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Colores según el tipo de mensaje
 */
@Composable
private fun getMessageColors(type: MessageType): ColorPair {
    return when (type) {
        MessageType.SUCCESS -> ColorPair(
            container = Color(0xFF1B5E20).copy(alpha = 0.95f), // Verde oscuro
            onContainer = Color.White
        )
        MessageType.ERROR -> ColorPair(
            container = Color(0xFFB71C1C).copy(alpha = 0.95f), // Rojo oscuro
            onContainer = Color.White
        )
        MessageType.WARNING -> ColorPair(
            container = Color(0xFFF57F17).copy(alpha = 0.95f), // Ámbar
            onContainer = Color(0xFF1A1A1A)
        )
        MessageType.INFO -> ColorPair(
            container = Color(0xFF0D47A1).copy(alpha = 0.95f), // Azul
            onContainer = Color.White
        )
        MessageType.CART_ADD -> ColorPair(
            container = Color(0xFF2E7D32).copy(alpha = 0.95f), // Verde
            onContainer = Color.White
        )
        MessageType.CART_REMOVE -> ColorPair(
            container = Color(0xFF455A64).copy(alpha = 0.95f), // Gris azulado
            onContainer = Color.White
        )
        MessageType.ORDER -> ColorPair(
            container = Color(0xFF6A1B9A).copy(alpha = 0.95f), // Púrpura
            onContainer = Color.White
        )
        MessageType.AUTH -> ColorPair(
            container = Color(0xFF00695C).copy(alpha = 0.95f), // Teal
            onContainer = Color.White
        )
        MessageType.SYNC -> ColorPair(
            container = Color(0xFF1565C0).copy(alpha = 0.95f), // Azul
            onContainer = Color.White
        )
    }
}

private data class ColorPair(
    val container: Color,
    val onContainer: Color
)

/**
 * Icono según el tipo de mensaje
 */
private fun getMessageIcon(type: MessageType): ImageVector {
    return when (type) {
        MessageType.SUCCESS -> Icons.Default.CheckCircle
        MessageType.ERROR -> Icons.Default.Error
        MessageType.WARNING -> Icons.Default.Warning
        MessageType.INFO -> Icons.Outlined.Info
        MessageType.CART_ADD -> Icons.Default.AddShoppingCart
        MessageType.CART_REMOVE -> Icons.Default.RemoveShoppingCart
        MessageType.ORDER -> Icons.Default.LocalShipping
        MessageType.AUTH -> Icons.Default.Security
        MessageType.SYNC -> Icons.Default.CloudDone
    }
}

/**
 * Título según el tipo de mensaje
 */
private fun getMessageTitle(type: MessageType): String {
    return when (type) {
        MessageType.SUCCESS -> "¡Éxito!"
        MessageType.ERROR -> "Error"
        MessageType.WARNING -> "Atención"
        MessageType.INFO -> "Información"
        MessageType.CART_ADD -> "Carrito"
        MessageType.CART_REMOVE -> "Carrito"
        MessageType.ORDER -> "Pedido"
        MessageType.AUTH -> "Seguridad"
        MessageType.SYNC -> "Sincronización"
    }
}

// ============================================
// DIÁLOGOS MEJORADOS
// ============================================

/**
 * Diálogo de confirmación con estilo mejorado
 */
@Composable
fun EnhancedConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar",
    icon: ImageVector = Icons.Default.Help,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isDestructive) Color(0xFFFFEBEE)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDestructive) Color(0xFFD32F2F) else iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDestructive) 
                            Color(0xFFD32F2F) 
                        else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = confirmText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = dismissText,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}

/**
 * Diálogo de éxito con animación
 */
@Composable
fun SuccessDialog(
    visible: Boolean,
    title: String = "¡Operación exitosa!",
    message: String,
    buttonText: String = "Aceptar",
    onDismiss: () -> Unit
) {
    if (visible) {
        var showCheckmark by remember { mutableStateOf(false) }
        
        LaunchedEffect(visible) {
            delay(200)
            showCheckmark = true
        }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = showCheckmark,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Diálogo de error con estilo
 */
@Composable
fun ErrorDialog(
    visible: Boolean,
    title: String = "Ha ocurrido un error",
    message: String,
    buttonText: String = "Entendido",
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(48.dp)
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C)
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onRetry != null) {
                        OutlinedButton(
                            onClick = onRetry,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reintentar")
                        }
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = buttonText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Diálogo de carga con mensaje
 */
@Composable
fun LoadingDialog(
    visible: Boolean,
    message: String = "Cargando..."
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = { /* No se puede cerrar */ },
            confirmButton = { },
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Diálogo de selección de opciones
 */
@Composable
fun <T> SelectionDialog(
    visible: Boolean,
    title: String,
    options: List<T>,
    selectedOption: T?,
    optionLabel: (T) -> String,
    optionIcon: ((T) -> ImageVector)? = null,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { option ->
                        val isSelected = option == selectedOption
                        Surface(
                            onClick = { onSelect(option) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                optionIcon?.let { getIcon ->
                                    Icon(
                                        imageVector = getIcon(option),
                                        contentDescription = null,
                                        tint = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = optionLabel(option),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Toast flotante simple (aparece y desaparece)
 */
@Composable
fun FloatingToast(
    visible: Boolean,
    message: String,
    emoji: String? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(200)
        ) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.inverseSurface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emoji?.let {
                    Text(text = it, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
