package com.example.app_verduras.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_verduras.ui.theme.HuertoHogarColors

// ==================== BOTONES ACCESIBLES ====================

/**
 * Botón con área táctil ampliada (mínimo 48dp según WCAG).
 * Incluye feedback háptico y soporte para lectores de pantalla.
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minTouchSize: Dp = 48.dp,
    contentDescription: String = text,
    icon: @Composable (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .defaultMinSize(minWidth = minTouchSize, minHeight = minTouchSize)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            },
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * IconButton con área táctil ampliada.
 */
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minTouchSize: Dp = 48.dp
) {
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minTouchSize, minHeight = minTouchSize)
            .clip(RoundedCornerShape(minTouchSize / 2))
            .clickable(
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

// ==================== TEXTO AJUSTABLE ====================

/**
 * Contenedor de texto con zoom (pinch-to-zoom).
 */
@Composable
fun ZoomableTextContainer(
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 3f,
    content: @Composable (Float) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    scale = (scale * zoom).coerceIn(minScale, maxScale)
                }
            }
    ) {
        content(scale)
    }
}

/**
 * Texto con tamaño dinámico basado en preferencias de accesibilidad.
 */
@Composable
fun ScalableText(
    text: String,
    modifier: Modifier = Modifier,
    baseSize: TextUnit = 16.sp,
    scaleFactor: Float = 1f,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontSize = baseSize * scaleFactor),
        fontWeight = fontWeight,
        color = color
    )
}

/**
 * Control deslizante para ajustar tamaño de texto.
 */
@Composable
fun TextSizeSlider(
    currentScale: Float,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minScale: Float = 0.8f,
    maxScale: Float = 2f
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tamaño de texto",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(currentScale * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "A",
                style = MaterialTheme.typography.bodySmall
            )
            
            Slider(
                value = currentScale,
                onValueChange = onScaleChange,
                valueRange = minScale..maxScale,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "A",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        
        // Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Vista previa del texto",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp * currentScale
                )
            )
        }
    }
}

// ==================== ALTO CONTRASTE ====================

/**
 * Paletas de colores optimizadas para daltonismo.
 */
object HighContrastColors {
    // Protanopia (dificultad con rojo)
    val ProtanopiaColors = HighContrastPalette(
        primary = Color(0xFF0077BB),      // Azul
        secondary = Color(0xFFEE7733),    // Naranja
        success = Color(0xFF009988),      // Turquesa
        error = Color(0xFFCC3311),        // Rojo oscuro
        warning = Color(0xFFEECC66),      // Amarillo
        background = Color(0xFFF5F5F5),
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = Color(0xFF1A1A1A)
    )
    
    // Deuteranopia (dificultad con verde)
    val DeuteranopiaColors = HighContrastPalette(
        primary = Color(0xFF004488),      // Azul oscuro
        secondary = Color(0xFFDDAA33),    // Dorado
        success = Color(0xFF117733),      // Verde oscuro
        error = Color(0xFFBB5566),        // Rosa rojizo
        warning = Color(0xFFEEDD88),      // Amarillo claro
        background = Color(0xFFF5F5F5),
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = Color(0xFF1A1A1A)
    )
    
    // Alto contraste general
    val HighContrastPalette = HighContrastPalette(
        primary = Color(0xFF000000),
        secondary = Color(0xFF0000FF),
        success = Color(0xFF006400),
        error = Color(0xFFFF0000),
        warning = Color(0xFFFFD700),
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = Color.Black
    )
}

data class HighContrastPalette(
    val primary: Color,
    val secondary: Color,
    val success: Color,
    val error: Color,
    val warning: Color,
    val background: Color,
    val surface: Color,
    val onPrimary: Color,
    val onBackground: Color
)

/**
 * Selector de modo de accesibilidad.
 */
@Composable
fun AccessibilityModeSelector(
    currentMode: AccessibilityMode,
    onModeChange: (AccessibilityMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Modo de visualización",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            AccessibilityMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onModeChange(mode) }
                        .background(
                            if (currentMode == mode)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color.Transparent
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = currentMode == mode,
                        onClick = { onModeChange(mode) }
                    )
                    
                    Column {
                        Text(
                            text = mode.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (currentMode == mode) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Text(
                            text = mode.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

enum class AccessibilityMode(
    val displayName: String,
    val description: String
) {
    NORMAL("Normal", "Colores estándar de la aplicación"),
    HIGH_CONTRAST("Alto contraste", "Mayor contraste para mejor visibilidad"),
    PROTANOPIA("Protanopia", "Optimizado para dificultad con rojos"),
    DEUTERANOPIA("Deuteranopia", "Optimizado para dificultad con verdes")
}

// ==================== INDICADORES DE ESTADO ACCESIBLES ====================

/**
 * Indicador de estado con múltiples señales (color + icono + texto).
 * Cumple con WCAG al no depender solo del color.
 */
@Composable
fun AccessibleStatusIndicator(
    status: StatusType,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(status.backgroundColor.copy(alpha = 0.2f))
            .border(1.dp, status.backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics {
                contentDescription = "${status.label}: ${status.description}"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = status.icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = status.backgroundColor
        )
        
        if (showLabel) {
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelMedium,
                color = status.backgroundColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

enum class StatusType(
    val label: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val backgroundColor: Color
) {
    SUCCESS(
        "Completado",
        "La operación se completó exitosamente",
        Icons.Default.CheckCircle,
        Color(0xFF4CAF50)
    ),
    WARNING(
        "Pendiente",
        "Requiere atención",
        Icons.Default.Warning,
        Color(0xFFFFC107)
    ),
    ERROR(
        "Error",
        "Ocurrió un problema",
        Icons.Default.Error,
        Color(0xFFF44336)
    ),
    INFO(
        "Información",
        "Información adicional",
        Icons.Default.Info,
        Color(0xFF2196F3)
    ),
    LOADING(
        "Cargando",
        "Procesando la solicitud",
        Icons.Default.Refresh,
        Color(0xFF9E9E9E)
    )
}

// ==================== TOUCH TARGET ====================

/**
 * Wrapper que asegura un área táctil mínima de 48dp.
 */
@Composable
fun TouchTargetWrapper(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String,
    minSize: Dp = 48.dp,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
