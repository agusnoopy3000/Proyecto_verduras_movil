package com.example.app_verduras.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modelo de un paso en el timeline.
 */
data class TimelineStep(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val status: TimelineStatus,
    val timestamp: String? = null
)

enum class TimelineStatus {
    COMPLETED,
    CURRENT,
    PENDING
}

/**
 * Timeline de actividad/seguimiento de pedidos.
 */
@Composable
fun ActivityTimeline(
    steps: List<TimelineStep>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    activeLineColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        steps.forEachIndexed { index, step ->
            val isLast = index == steps.lastIndex
            
            TimelineItem(
                step = step,
                isLast = isLast,
                lineColor = lineColor,
                activeLineColor = activeLineColor
            )
        }
    }
}

@Composable
private fun TimelineItem(
    step: TimelineStep,
    isLast: Boolean,
    lineColor: Color,
    activeLineColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timeline")
    
    // Animación para el estado actual
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (step.status == TimelineStatus.CURRENT) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val iconColor = when (step.status) {
        TimelineStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        TimelineStatus.CURRENT -> MaterialTheme.colorScheme.primary
        TimelineStatus.PENDING -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    val backgroundColor = when (step.status) {
        TimelineStatus.COMPLETED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        TimelineStatus.CURRENT -> MaterialTheme.colorScheme.primaryContainer
        TimelineStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Columna del indicador y línea
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            // Icono circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(if (step.status == TimelineStatus.CURRENT) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                if (step.status == TimelineStatus.COMPLETED) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completado",
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Línea conectora
            if (!isLast) {
                val lineModifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                
                Box(
                    modifier = lineModifier.background(
                        if (step.status == TimelineStatus.COMPLETED)
                            activeLineColor
                        else
                            lineColor
                    )
                )
            }
        }
        
        // Contenido
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (step.status == TimelineStatus.PENDING)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Timestamp
                step.timestamp?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Badge de estado actual
            if (step.status == TimelineStatus.CURRENT) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "En progreso",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Timeline horizontal para flujo de pasos (stepper).
 */
@Composable
fun HorizontalStepper(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier,
    completedColor: Color = MaterialTheme.colorScheme.primary,
    pendingColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentStep
            val isCurrent = index == currentStep
            
            // Step indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Círculo con número
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> completedColor
                                isCurrent -> completedColor
                                else -> pendingColor
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completado",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Label
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isCompleted || isCurrent -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    },
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            
            // Línea conectora
            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (isCompleted) completedColor else pendingColor
                        )
                )
            }
        }
    }
}

/**
 * Componente de flujo por pasos completo (Stepper Flow).
 */
@Composable
fun StepperFlow(
    steps: List<String>,
    currentStep: Int,
    onStepChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (stepIndex: Int) -> Unit
) {
    Column(modifier = modifier) {
        // Stepper horizontal
        HorizontalStepper(
            steps = steps,
            currentStep = currentStep,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
        )
        
        // Contenido del paso actual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content(currentStep)
        }
        
        // Botones de navegación
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón anterior
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { onStepChange(currentStep - 1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Anterior")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            
            // Botón siguiente/finalizar
            Button(
                onClick = { 
                    if (currentStep < steps.lastIndex) {
                        onStepChange(currentStep + 1)
                    }
                }
            ) {
                Text(
                    if (currentStep == steps.lastIndex) "Finalizar" else "Siguiente"
                )
                if (currentStep < steps.lastIndex) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Timeline de seguimiento de pedido predefinido.
 */
@Composable
fun OrderTrackingTimeline(
    orderStatus: OrderTrackingStatus,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        TimelineStep(
            id = "1",
            title = "Pedido recibido",
            description = "Tu pedido ha sido confirmado",
            icon = Icons.Default.ShoppingCart,
            status = if (orderStatus.ordinal >= OrderTrackingStatus.RECEIVED.ordinal) 
                TimelineStatus.COMPLETED else TimelineStatus.PENDING,
            timestamp = "10:30"
        ),
        TimelineStep(
            id = "2",
            title = "En preparación",
            description = "Estamos preparando tus productos",
            icon = Icons.Default.Inventory,
            status = when {
                orderStatus.ordinal > OrderTrackingStatus.PREPARING.ordinal -> TimelineStatus.COMPLETED
                orderStatus == OrderTrackingStatus.PREPARING -> TimelineStatus.CURRENT
                else -> TimelineStatus.PENDING
            },
            timestamp = if (orderStatus.ordinal >= OrderTrackingStatus.PREPARING.ordinal) "11:00" else null
        ),
        TimelineStep(
            id = "3",
            title = "En camino",
            description = "Tu pedido está en ruta de entrega",
            icon = Icons.Default.LocalShipping,
            status = when {
                orderStatus.ordinal > OrderTrackingStatus.IN_TRANSIT.ordinal -> TimelineStatus.COMPLETED
                orderStatus == OrderTrackingStatus.IN_TRANSIT -> TimelineStatus.CURRENT
                else -> TimelineStatus.PENDING
            },
            timestamp = if (orderStatus.ordinal >= OrderTrackingStatus.IN_TRANSIT.ordinal) "12:30" else null
        ),
        TimelineStep(
            id = "4",
            title = "Entregado",
            description = "¡Tu pedido ha sido entregado!",
            icon = Icons.Default.Home,
            status = when {
                orderStatus == OrderTrackingStatus.DELIVERED -> TimelineStatus.COMPLETED
                else -> TimelineStatus.PENDING
            },
            timestamp = if (orderStatus == OrderTrackingStatus.DELIVERED) "13:45" else null
        )
    )
    
    ActivityTimeline(
        steps = steps,
        modifier = modifier
    )
}

enum class OrderTrackingStatus {
    RECEIVED,
    PREPARING,
    IN_TRANSIT,
    DELIVERED
}
