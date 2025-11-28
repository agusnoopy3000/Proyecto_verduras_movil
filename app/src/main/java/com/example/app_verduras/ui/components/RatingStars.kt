package com.example.app_verduras.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Componente de rating con estrellas animadas.
 * Incluye animación de escala al seleccionar y feedback háptico.
 */
@Composable
fun AnimatedRatingStars(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 40.dp,
    activeColor: Color = Color(0xFFFFD700), // Gold
    inactiveColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    showLabel: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    
    // Labels para cada rating
    val ratingLabels = listOf(
        "",
        "😞 Muy malo",
        "😕 Malo", 
        "😐 Regular",
        "😊 Bueno",
        "🤩 Excelente"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..maxRating) {
                AnimatedStar(
                    isSelected = i <= rating,
                    starSize = starSize,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRatingChange(i)
                    }
                )
            }
        }
        
        // Label animado
        if (showLabel && rating > 0) {
            AnimatedVisibility(
                visible = rating > 0,
                label = ratingLabels.getOrElse(rating) { "" }
            )
        }
    }
}

@Composable
private fun AnimatedStar(
    isSelected: Boolean,
    starSize: Dp,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "star_scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) 360f else 0f,
        animationSpec = tween(300),
        label = "star_rotation"
    )
    
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(300)
            isAnimating = false
        }
    }
    
    Icon(
        imageVector = if (isSelected) Icons.Filled.Star else Icons.Filled.StarBorder,
        contentDescription = null,
        modifier = Modifier
            .size(starSize)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isAnimating = true
                onClick()
            },
        tint = if (isSelected) activeColor else inactiveColor
    )
}

@Composable
private fun AnimatedVisibility(
    visible: Boolean,
    label: String
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "label_alpha"
    )
    
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    )
}

/**
 * Rating con estrellas de solo lectura (para mostrar promedio).
 */
@Composable
fun ReadOnlyRating(
    rating: Float,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 20.dp,
    activeColor: Color = Color(0xFFFFD700),
    inactiveColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    showValue: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 1..maxRating) {
            val icon = when {
                i <= rating.toInt() -> Icons.Filled.Star
                i - 0.5f <= rating -> Icons.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = if (i <= rating.toInt() || i - 0.5f <= rating) 
                    activeColor else inactiveColor
            )
        }
        
        if (showValue) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f", rating),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Encuesta rápida interactiva con emojis.
 */
@Composable
fun QuickSurvey(
    question: String,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        "😞" to "Muy insatisfecho",
        "😕" to "Insatisfecho",
        "😐" to "Neutral",
        "😊" to "Satisfecho",
        "🤩" to "Muy satisfecho"
    )
    
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEachIndexed { index, (emoji, label) ->
                    EmojiOption(
                        emoji = emoji,
                        label = label,
                        isSelected = selectedIndex == index,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedIndex = index
                            onAnswerSelected(index + 1)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmojiOption(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "emoji_scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.scale(scale)
        )
        
        if (isSelected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
