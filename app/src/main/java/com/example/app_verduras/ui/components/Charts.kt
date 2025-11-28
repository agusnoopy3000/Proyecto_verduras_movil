package com.example.app_verduras.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Datos para gráficos.
 */
data class ChartDataPoint(
    val label: String,
    val value: Float,
    val color: Color = Color.Unspecified
)

// ==================== GRÁFICO DE BARRAS ====================

/**
 * Gráfico de barras interactivo con animación.
 */
@Composable
fun AnimatedBarChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    showValues: Boolean = true,
    maxValue: Float? = null,
    onBarClick: ((ChartDataPoint) -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "bar_animation"
    )
    
    var selectedBar by remember { mutableStateOf<Int?>(null) }
    val maxDataValue = maxValue ?: (data.maxOfOrNull { it.value } ?: 1f)
    
    Column(modifier = modifier) {
        // Tooltip
        selectedBar?.let { index ->
            if (index in data.indices) {
                TooltipCard(
                    label = data[index].label,
                    value = data[index].value
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, point ->
                val isSelected = selectedBar == index
                val barHeight = (point.value / maxDataValue) * animatedProgress
                val color = if (point.color != Color.Unspecified) point.color else barColor
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(48.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    selectedBar = if (selectedBar == index) null else index
                                    onBarClick?.invoke(point)
                                }
                            )
                        }
                ) {
                    // Valor
                    if (showValues) {
                        Text(
                            text = point.value.toInt().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // Barra
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 40.dp else 32.dp)
                            .fillMaxHeight(barHeight)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                if (isSelected)
                                    color
                                else
                                    color.copy(alpha = 0.7f)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Label
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ==================== GRÁFICO DE LÍNEAS ====================

/**
 * Gráfico de líneas con animación y puntos interactivos.
 */
@Composable
fun AnimatedLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillGradient: Boolean = true,
    showPoints: Boolean = true,
    onPointClick: ((ChartDataPoint) -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "line_animation"
    )
    
    var selectedPoint by remember { mutableStateOf<Int?>(null) }
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val minValue = data.minOfOrNull { it.value } ?: 0f
    val range = maxValue - minValue
    
    Column(modifier = modifier) {
        // Tooltip
        selectedPoint?.let { index ->
            if (index in data.indices) {
                TooltipCard(
                    label = data[index].label,
                    value = data[index].value
                )
            }
        }
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val pointWidth = size.width / (data.size - 1).coerceAtLeast(1)
                        val clickedIndex = (offset.x / pointWidth).toInt().coerceIn(0, data.lastIndex)
                        selectedPoint = if (selectedPoint == clickedIndex) null else clickedIndex
                        onPointClick?.invoke(data[clickedIndex])
                    }
                }
        ) {
            if (data.isEmpty()) return@Canvas
            
            val width = size.width
            val height = size.height
            val pointSpacing = width / (data.size - 1).coerceAtLeast(1)
            
            val points = data.mapIndexed { index, point ->
                val x = index * pointSpacing
                val normalizedY = if (range == 0f) 0.5f else (point.value - minValue) / range
                val y = height - (normalizedY * height * animatedProgress)
                Offset(x, y)
            }
            
            // Área con gradiente
            if (fillGradient && points.size > 1) {
                val path = Path().apply {
                    moveTo(points.first().x, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height)
                    close()
                }
                
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.3f),
                            lineColor.copy(alpha = 0.0f)
                        )
                    )
                )
            }
            
            // Línea
            if (points.size > 1) {
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        // Curva bezier suave
                        val controlX = (prev.x + curr.x) / 2
                        cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                    }
                }
                
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // Puntos
            if (showPoints) {
                points.forEachIndexed { index, point ->
                    val isSelected = selectedPoint == index
                    val radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx()
                    
                    // Círculo exterior (blanco)
                    drawCircle(
                        color = Color.White,
                        radius = radius + 2.dp.toPx(),
                        center = point
                    )
                    
                    // Círculo interior
                    drawCircle(
                        color = lineColor,
                        radius = radius,
                        center = point
                    )
                }
            }
        }
        
        // Labels en el eje X
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ==================== GRÁFICO CIRCULAR (PIE/DONUT) ====================

/**
 * Gráfico circular (pie/donut) animado.
 */
@Composable
fun AnimatedPieChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    isDonut: Boolean = true,
    donutThickness: Float = 60f,
    showLabels: Boolean = true,
    onSliceClick: ((ChartDataPoint) -> Unit)? = null
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "pie_animation"
    )
    
    var selectedSlice by remember { mutableStateOf<Int?>(null) }
    
    // Colores predefinidos si no se especifican
    val defaultColors = listOf(
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFFFFC107),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFFFF5722),
        Color(0xFF00BCD4),
        Color(0xFF8BC34A)
    )
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gráfico
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        // Detectar slice clickeado (simplificado)
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        val normalizedAngle = (angle + 360) % 360
                        
                        var currentAngle = -90f
                        data.forEachIndexed { index, point ->
                            val sweepAngle = (point.value / total) * 360f
                            val startAngle = (currentAngle + 360) % 360
                            val endAngle = (startAngle + sweepAngle) % 360
                            
                            if (normalizedAngle >= startAngle && normalizedAngle <= startAngle + sweepAngle) {
                                selectedSlice = if (selectedSlice == index) null else index
                                onSliceClick?.invoke(point)
                                return@detectTapGestures
                            }
                            currentAngle += sweepAngle
                        }
                    }
                }
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            var startAngle = -90f
            
            data.forEachIndexed { index, point ->
                val sweepAngle = (point.value / total) * 360f * animatedProgress
                val color = if (point.color != Color.Unspecified) 
                    point.color 
                else 
                    defaultColors[index % defaultColors.size]
                
                val isSelected = selectedSlice == index
                val arcRadius = if (isSelected) radius * 1.05f else radius
                val arcSize = Size(arcRadius * 2, arcRadius * 2)
                val topLeft = Offset(
                    center.x - arcRadius,
                    center.y - arcRadius
                )
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = !isDonut,
                    topLeft = topLeft,
                    size = arcSize,
                    style = if (isDonut) Stroke(width = donutThickness) else androidx.compose.ui.graphics.drawscope.Fill
                )
                
                startAngle += sweepAngle
            }
            
            // Centro blanco para donut
            if (isDonut) {
                drawCircle(
                    color = Color.White,
                    radius = radius - donutThickness - 5,
                    center = center
                )
            }
        }
        
        // Leyenda
        if (showLabels) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.forEachIndexed { index, point ->
                    val color = if (point.color != Color.Unspecified)
                        point.color
                    else
                        defaultColors[index % defaultColors.size]
                    
                    val percentage = (point.value / total * 100).toInt()
                    val isSelected = selectedSlice == index
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        
                        Text(
                            text = "${point.label} ($percentage%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ==================== COMPONENTES AUXILIARES ====================

@Composable
private fun TooltipCard(
    label: String,
    value: Float
) {
    Card(
        modifier = Modifier.padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
            Text(
                text = "•",
                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.5f)
            )
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
    }
}

/**
 * Selector de tipo de gráfico.
 */
@Composable
fun ChartTypeSelector(
    selectedType: ChartType,
    onTypeChange: (ChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        ChartType.entries.forEach { type ->
            val isSelected = type == selectedType
            
            Surface(
                onClick = { onTypeChange(type) },
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = type.displayName,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

enum class ChartType(val displayName: String) {
    BAR("Barras"),
    LINE("Líneas"),
    PIE("Circular")
}

/**
 * Dashboard card con gráfico y estadísticas.
 */
@Composable
fun StatsDashboardCard(
    title: String,
    value: String,
    change: Float, // Porcentaje de cambio (positivo/negativo)
    chartData: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Cambio porcentual
                val changeColor = if (change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                val changeIcon = if (change >= 0) "↑" else "↓"
                
                Surface(
                    color = changeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$changeIcon ${kotlin.math.abs(change).toInt()}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = changeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mini gráfico de líneas
            AnimatedLineChart(
                data = chartData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                showPoints = false,
                fillGradient = true
            )
        }
    }
}
