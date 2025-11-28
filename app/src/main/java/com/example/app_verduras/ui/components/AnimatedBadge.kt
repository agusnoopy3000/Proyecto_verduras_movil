package com.example.app_verduras.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_verduras.ui.theme.HuertoHogarColors
import kotlinx.coroutines.delay

/**
 * Badge animado con efecto bounce cuando cambia el valor.
 * Ideal para mostrar cantidad de items en carrito.
 */
@Composable
fun AnimatedBadge(
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = HuertoHogarColors.Error,
    contentColor: Color = Color.White,
    showZero: Boolean = false
) {
    var previousCount by remember { mutableIntStateOf(count) }
    var isAnimating by remember { mutableStateOf(false) }
    
    // Detectar cambio en el contador
    LaunchedEffect(count) {
        if (count != previousCount) {
            isAnimating = true
            delay(300)
            isAnimating = false
            previousCount = count
        }
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "badge_scale"
    )
    
    if (count > 0 || showZero) {
        Box(
            modifier = modifier
                .scale(scale)
                .size(if (count > 99) 28.dp else if (count > 9) 24.dp else 20.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    ) togetherWith scaleOut(
                        targetScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                },
                label = "badge_count"
            ) { targetCount ->
                Text(
                    text = if (targetCount > 99) "99+" else targetCount.toString(),
                    color = contentColor,
                    fontSize = if (targetCount > 99) 9.sp else if (targetCount > 9) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Icono con badge animado integrado.
 * Perfecto para iconos de navegación con contador.
 */
@Composable
fun IconWithAnimatedBadge(
    icon: ImageVector,
    contentDescription: String?,
    badgeCount: Int,
    modifier: Modifier = Modifier,
    iconTint: Color = LocalContentColor.current,
    badgeColor: Color = HuertoHogarColors.Error,
    badgeContentColor: Color = Color.White
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        AnimatedBadge(
            count = badgeCount,
            containerColor = badgeColor,
            contentColor = badgeContentColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
        )
    }
}

/**
 * Badge animado con pulso continuo para llamar la atención.
 */
@Composable
fun PulsingBadge(
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = HuertoHogarColors.Error,
    contentColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    if (count > 0) {
        Box(
            modifier = modifier
                .scale(pulse)
                .size(if (count > 99) 28.dp else if (count > 9) 24.dp else 20.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = contentColor,
                fontSize = if (count > 99) 9.sp else if (count > 9) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Badge con animación de entrada/salida suave.
 */
@Composable
fun AnimatedVisibilityBadge(
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = HuertoHogarColors.Error,
    contentColor: Color = Color.White
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = count > 0,
        enter = scaleIn(
            initialScale = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        ),
        exit = scaleOut(
            targetScale = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        )
    ) {
        Box(
            modifier = modifier
                .size(if (count > 99) 28.dp else if (count > 9) 24.dp else 20.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = contentColor,
                fontSize = if (count > 99) 9.sp else if (count > 9) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
