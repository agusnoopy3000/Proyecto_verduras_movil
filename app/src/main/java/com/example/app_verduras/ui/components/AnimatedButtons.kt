package com.example.app_verduras.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.app_verduras.ui.theme.HuertoHogarColors

/**
 * Botón con efecto de escala al presionar (micro-interacción).
 */
@Composable
fun ScaleOnPressButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = HuertoHogarColors.Primary,
    contentColor: Color = Color.White,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_scale"
    )
    
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.scale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        content = content
    )
}

/**
 * Botón con efecto de elevación y sombra al presionar.
 */
@Composable
fun ElevatedPressButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = HuertoHogarColors.Primary,
    contentColor: Color = Color.White,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_elevation"
    )
    
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_offset"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .offset(y = offsetY)
            .shadow(elevation, RoundedCornerShape(12.dp)),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        shape = RoundedCornerShape(12.dp),
        content = content
    )
}

/**
 * Botón con efecto de gradiente animado.
 */
@Composable
fun GradientAnimatedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradientColors: List<Color> = HuertoHogarColors.GradientPrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "gradient_button_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = gradientColors,
                    startX = animatedOffset * 200f,
                    endX = animatedOffset * 200f + 400f
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * IconButton con animación de rotación al presionar.
 */
@Composable
fun RotatingIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = HuertoHogarColors.Primary,
    rotationDegrees: Float = 90f
) {
    var isClicked by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val rotation by animateFloatAsState(
        targetValue = if (isClicked) rotationDegrees else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { isClicked = false },
        label = "icon_rotation"
    )
    
    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isClicked = true
            onClick()
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.graphicsLayer { rotationZ = rotation }
        )
    }
}

/**
 * Botón con animación de pulso para llamar la atención.
 */
@Composable
fun PulsingButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = HuertoHogarColors.Primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

/**
 * FAB animado con efecto de entrada bounce.
 */
@Composable
fun AnimatedFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = HuertoHogarColors.Primary,
    contentColor: Color = Color.White,
    visible: Boolean = true
) {
    var hasAppeared by remember { mutableStateOf(false) }
    
    LaunchedEffect(visible) {
        if (visible) {
            hasAppeared = true
        }
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible && hasAppeared) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_scale"
    )
    
    if (scale > 0f) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            containerColor = containerColor,
            contentColor = contentColor,
            shape = CircleShape
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}

/**
 * Chip animado con cambio de estado suave.
 */
@Composable
fun AnimatedChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) HuertoHogarColors.Primary else HuertoHogarColors.SurfaceVariant,
        animationSpec = tween(300),
        label = "chip_bg"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else HuertoHogarColors.OnBackground,
        animationSpec = tween(300),
        label = "chip_content"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chip_scale"
    )
    
    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            }
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

/**
 * Botón de cantidad con animaciones (para carrito).
 */
@Composable
fun AnimatedQuantityButton(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
    minQuantity: Int = 0,
    maxQuantity: Int = 99
) {
    var lastAction by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current
    
    val quantityScale by animateFloatAsState(
        targetValue = if (lastAction != null) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { lastAction = null },
        label = "quantity_scale"
    )
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(25.dp))
            .background(HuertoHogarColors.SurfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón decrementar
        IconButton(
            onClick = {
                if (quantity > minQuantity) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    lastAction = "decrease"
                    onDecrease()
                }
            },
            enabled = quantity > minQuantity,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Disminuir",
                tint = if (quantity > minQuantity) HuertoHogarColors.Error else HuertoHogarColors.TextSecondary.copy(alpha = 0.4f)
            )
        }
        
        // Cantidad
        Text(
            text = quantity.toString(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .scale(quantityScale)
                .padding(horizontal = 8.dp),
            color = HuertoHogarColors.OnBackground
        )
        
        // Botón incrementar
        IconButton(
            onClick = {
                if (quantity < maxQuantity) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    lastAction = "increase"
                    onIncrease()
                }
            },
            enabled = quantity < maxQuantity,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Aumentar",
                tint = if (quantity < maxQuantity) HuertoHogarColors.Success else HuertoHogarColors.TextSecondary.copy(alpha = 0.4f)
            )
        }
    }
}
