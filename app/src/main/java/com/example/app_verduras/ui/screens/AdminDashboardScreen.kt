package com.example.app_verduras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_verduras.ui.components.*

/**
 * Dashboard de administrador con estadísticas visuales e interactivas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChartType by remember { mutableStateOf(ChartType.BAR) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.WEEK) }
    
    // Datos de ejemplo (en producción vendrían del ViewModel)
    val salesData = remember {
        listOf(
            ChartDataPoint("Lun", 45000f),
            ChartDataPoint("Mar", 52000f),
            ChartDataPoint("Mié", 38000f),
            ChartDataPoint("Jue", 61000f),
            ChartDataPoint("Vie", 55000f),
            ChartDataPoint("Sáb", 72000f),
            ChartDataPoint("Dom", 48000f)
        )
    }
    
    val categoryData = remember {
        listOf(
            ChartDataPoint("Verduras", 35f, Color(0xFF4CAF50)),
            ChartDataPoint("Frutas", 28f, Color(0xFFFF9800)),
            ChartDataPoint("Lácteos", 20f, Color(0xFF2196F3)),
            ChartDataPoint("Otros", 17f, Color(0xFF9C27B0))
        )
    }
    
    val orderStatusData = remember {
        listOf(
            ChartDataPoint("Pendientes", 12f, Color(0xFFFFC107)),
            ChartDataPoint("En proceso", 8f, Color(0xFF2196F3)),
            ChartDataPoint("Enviados", 25f, Color(0xFF9C27B0)),
            ChartDataPoint("Entregados", 45f, Color(0xFF4CAF50))
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Selector de rango de tiempo
                    TimeRangeChip(
                        selectedRange = selectedTimeRange,
                        onRangeChange = { selectedTimeRange = it }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // KPIs principales
            Text(
                text = "Resumen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPICard(
                    title = "Ventas del día",
                    value = "$125.400",
                    change = 12.5f,
                    icon = Icons.Default.AttachMoney,
                    iconColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                KPICard(
                    title = "Pedidos",
                    value = "34",
                    change = 8.2f,
                    icon = Icons.Default.ShoppingBag,
                    iconColor = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPICard(
                    title = "Usuarios nuevos",
                    value = "12",
                    change = 25.0f,
                    icon = Icons.Default.PersonAdd,
                    iconColor = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
                KPICard(
                    title = "Productos bajos",
                    value = "5",
                    change = -15.0f,
                    icon = Icons.Default.Inventory,
                    iconColor = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Gráfico de ventas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ventas por día",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        ChartTypeSelector(
                            selectedType = selectedChartType,
                            onTypeChange = { selectedChartType = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    when (selectedChartType) {
                        ChartType.BAR -> AnimatedBarChart(
                            data = salesData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                        ChartType.LINE -> AnimatedLineChart(
                            data = salesData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            showPoints = true
                        )
                        ChartType.PIE -> AnimatedPieChart(
                            data = categoryData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }
            
            // Distribución por categorías
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Ventas por categoría",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    AnimatedPieChart(
                        data = categoryData,
                        modifier = Modifier.fillMaxWidth(),
                        isDonut = true
                    )
                }
            }
            
            // Estado de pedidos
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Estado de pedidos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    AnimatedBarChart(
                        data = orderStatusData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        showValues = true
                    )
                }
            }
            
            // Actividad reciente
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Actividad reciente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lista de actividades recientes
                    RecentActivityList()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun KPICard(
    title: String,
    value: String,
    change: Float,
    icon: ImageVector,
    iconColor: Color,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Badge de cambio
                val changeColor = if (change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                val changeIcon = if (change >= 0) "↑" else "↓"
                
                Text(
                    text = "$changeIcon${kotlin.math.abs(change).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = changeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun TimeRangeChip(
    selectedRange: TimeRange,
    onRangeChange: (TimeRange) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text(selectedRange.displayName) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TimeRange.entries.forEach { range ->
                DropdownMenuItem(
                    text = { Text(range.displayName) },
                    onClick = {
                        onRangeChange(range)
                        expanded = false
                    },
                    leadingIcon = if (range == selectedRange) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun RecentActivityList() {
    val activities = listOf(
        ActivityItem(
            icon = Icons.Default.ShoppingCart,
            iconColor = Color(0xFF4CAF50),
            title = "Nuevo pedido #1234",
            subtitle = "Juan Pérez - $25.500",
            time = "Hace 5 min"
        ),
        ActivityItem(
            icon = Icons.Default.PersonAdd,
            iconColor = Color(0xFF2196F3),
            title = "Usuario registrado",
            subtitle = "maria@email.com",
            time = "Hace 12 min"
        ),
        ActivityItem(
            icon = Icons.Default.LocalShipping,
            iconColor = Color(0xFF9C27B0),
            title = "Pedido enviado #1232",
            subtitle = "En ruta de entrega",
            time = "Hace 25 min"
        ),
        ActivityItem(
            icon = Icons.Default.Warning,
            iconColor = Color(0xFFFF5722),
            title = "Stock bajo",
            subtitle = "Tomates: quedan 5 unidades",
            time = "Hace 1 hora"
        )
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        activities.forEach { activity ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(activity.iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = activity.icon,
                        contentDescription = null,
                        tint = activity.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = activity.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = activity.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private data class ActivityItem(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val subtitle: String,
    val time: String
)

enum class TimeRange(val displayName: String) {
    TODAY("Hoy"),
    WEEK("Semana"),
    MONTH("Mes"),
    YEAR("Año")
}
