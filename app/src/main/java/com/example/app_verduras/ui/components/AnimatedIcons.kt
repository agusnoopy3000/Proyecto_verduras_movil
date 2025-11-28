package com.example.app_verduras.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.app_verduras.ui.theme.HuertoHogarColors
import kotlinx.coroutines.delay

/**
 * Tipos de estado para iconos animados
 */
enum class AnimatedIconState {
    LOADING,
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

/**
 * Icono animado para estados (carga, éxito, error).
 */
@Composable
fun AnimatedStateIcon(
    state: AnimatedIconState,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    when (state) {
        AnimatedIconState.LOADING -> LoadingIcon(modifier = modifier, size = size)
        AnimatedIconState.SUCCESS -> SuccessIcon(modifier = modifier, size = size)
        AnimatedIconState.ERROR -> ErrorIcon(modifier = modifier, size = size)
        AnimatedIconState.WARNING -> WarningIcon(modifier = modifier, size = size)
        AnimatedIconState.INFO -> InfoIcon(modifier = modifier, size = size)
    }
}

/**
 * Icono de carga con rotación continua.
 */
@Composable
fun LoadingIcon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    color: Color = HuertoHogarColors.Primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(size * 0.7f)
                .rotate(rotation),
            color = color,
            strokeWidth = 3.dp
        )
    }
}

/**
 * Icono de éxito con animación de aparición y bounce.
 */
@Composable
fun SuccessIcon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    color: Color = HuertoHogarColors.Success
) {
    var hasAnimated by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        hasAnimated = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (hasAnimated) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "success_scale"
    )
    
    val checkmarkProgress by animateFloatAsState(
        targetValue = if (hasAnimated) 1f else 0f,
        animationSpec = tween(400, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "checkmark_progress"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.75f)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Éxito",
                modifier = Modifier
                    .size(size * 0.45f)
                    .graphicsLayer {
                        scaleX = checkmarkProgress
                        scaleY = checkmarkProgress
                    },
                tint = Color.White
            )
        }
    }
}

/**
 * Icono de error con animación de shake.
 */
@Composable
fun ErrorIcon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    color: Color = HuertoHogarColors.Error
) {
    var hasAnimated by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        hasAnimated = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (hasAnimated) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "error_scale"
    )
    
    val shake by animateFloatAsState(
        targetValue = if (hasAnimated) 0f else 10f,
        animationSpec = repeatable(
            iterations = 4,
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "error_shake"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .graphicsLayer { translationX = shake }
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.75f)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Error",
                modifier = Modifier.size(size * 0.45f),
                tint = Color.White
            )
        }
    }
}

/**
 * Icono de advertencia con animación de pulso.
 */
@Composable
fun WarningIcon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    color: Color = HuertoHogarColors.Warning
) {
    val infiniteTransition = rememberInfiniteTransition(label = "warning")
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warning_pulse"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(pulse)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.75f)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Advertencia",
                modifier = Modifier.size(size * 0.45f),
                tint = Color.White
            )
        }
    }
}

/**
 * Icono de información con animación de entrada.
 */
@Composable
fun InfoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    color: Color = HuertoHogarColors.Info
) {
    var hasAnimated by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        hasAnimated = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (hasAnimated) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "info_scale"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.75f)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Información",
                modifier = Modifier.size(size * 0.45f),
                tint = Color.White
            )
        }
    }
}

/**
 * Icono con animación de rebote al aparecer.
 */
@Composable
fun BouncingIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = HuertoHogarColors.Primary
) {
    var hasAnimated by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        hasAnimated = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (hasAnimated) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounce_scale"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .scale(scale),
        tint = tint
    )
}

/**
 * Icono con efecto de parpadeo.
 */
@Composable
fun BlinkingIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = HuertoHogarColors.Primary,
    blinkDuration: Int = 500
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(blinkDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_alpha"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .graphicsLayer { this.alpha = alpha },
        tint = tint
    )
}

/**
 * Icono con animación de rotación continua.
 */
@Composable
fun RotatingIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = HuertoHogarColors.Primary,
    rotationDuration: Int = 2000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_degrees"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .rotate(rotation),
        tint = tint
    )
}

/**
 * Indicador de sincronización animado.
 */
@Composable
fun SyncIndicator(
    isSyncing: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    if (isSyncing) {
        RotatingIcon(
            icon = Icons.Default.Sync,
            contentDescription = "Sincronizando",
            modifier = modifier,
            size = size,
            tint = HuertoHogarColors.Primary,
            rotationDuration = 1000
        )
    }
}
