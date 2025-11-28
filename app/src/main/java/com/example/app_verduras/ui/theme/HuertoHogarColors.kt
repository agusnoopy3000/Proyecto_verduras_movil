package com.example.app_verduras.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores personalizada para Huerto Hogar.
 * 
 * Inspirada en la naturaleza, productos orgánicos y frescura.
 */
object HuertoHogarColors {
    // Colores principales
    val Primary = Color(0xFF2E7D32)         // Verde bosque
    val PrimaryVariant = Color(0xFF1B5E20)  // Verde oscuro
    val PrimaryLight = Color(0xFF81C784)    // Verde claro
    
    // Colores secundarios
    val Secondary = Color(0xFFFF8F00)       // Naranja/Ámbar (frutas maduras)
    val SecondaryVariant = Color(0xFFFF6F00)
    val SecondaryLight = Color(0xFFFFCA28)
    
    // Colores de acento
    val Accent = Color(0xFFE8F5E9)          // Verde muy claro (fondo)
    val AccentWarm = Color(0xFFFFF8E1)      // Crema cálido
    
    // Colores de estado
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFC107)
    val Error = Color(0xFFE53935)
    val Info = Color(0xFF2196F3)
    
    // Colores de fondo
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF5F5F5)
    
    // Colores de texto
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFF000000)
    val OnBackground = Color(0xFF1C1B1F)
    val OnSurface = Color(0xFF1C1B1F)
    val TextSecondary = Color(0xFF757575)
    
    // Gradientes predefinidos
    val GradientPrimary = listOf(Primary, PrimaryLight)
    val GradientSecondary = listOf(Secondary, SecondaryLight)
    val GradientFresh = listOf(Color(0xFF43A047), Color(0xFF66BB6A), Color(0xFFA5D6A7))
    val GradientSunset = listOf(Color(0xFFFF8A65), Color(0xFFFFB74D), Color(0xFFFFE082))
    val GradientNature = listOf(Color(0xFF2E7D32), Color(0xFF558B2F), Color(0xFF8BC34A))
    
    // Colores para categorías de productos
    val CategoryVerduras = Color(0xFF43A047)
    val CategoryFrutas = Color(0xFFFF7043)
    val CategoryHortalizas = Color(0xFF7CB342)
    val CategoryOrganicos = Color(0xFF00897B)
    
    // Colores para estados de pedido
    val OrderPending = Color(0xFFFFA726)
    val OrderConfirmed = Color(0xFF42A5F5)
    val OrderShipped = Color(0xFFAB47BC)
    val OrderDelivered = Color(0xFF66BB6A)
    val OrderCancelled = Color(0xFFEF5350)
}

/**
 * Paleta de colores para Dark Mode - Colores más armónicos
 * Optimizados para legibilidad y comodidad visual en entornos oscuros.
 */
object HuertoHogarDarkColors {
    // Colores principales - Más suaves para dark mode
    val Primary = Color(0xFF81C784)         // Verde más claro
    val PrimaryVariant = Color(0xFF4CAF50)  // Verde medio
    val PrimaryLight = Color(0xFFA5D6A7)    // Verde pastel
    
    // Colores secundarios
    val Secondary = Color(0xFFFFB74D)       // Naranja más suave
    val SecondaryVariant = Color(0xFFFF9800)
    val SecondaryLight = Color(0xFFFFE082)
    
    // Colores de acento
    val Accent = Color(0xFF1B3B1D)          // Verde muy oscuro (fondo cards)
    val AccentWarm = Color(0xFF2D2518)      // Cálido oscuro
    
    // Colores de estado - Más vibrantes para destacar en oscuro
    val Success = Color(0xFF69F0AE)
    val Warning = Color(0xFFFFD54F)
    val Error = Color(0xFFFF5252)
    val Info = Color(0xFF40C4FF)
    
    // Colores de fondo
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceVariant = Color(0xFF2C2C2C)
    
    // Colores de texto
    val OnPrimary = Color(0xFF1B3B1D)
    val OnSecondary = Color(0xFF1C1B1F)
    val OnBackground = Color(0xFFE8E8E8)
    val OnSurface = Color(0xFFE8E8E8)
    val TextSecondary = Color(0xFFB0B0B0)
    
    // Gradientes predefinidos - Para dark mode
    val GradientPrimary = listOf(Color(0xFF2E7D32), Color(0xFF81C784))
    val GradientSecondary = listOf(Color(0xFFFF8F00), Color(0xFFFFCA28))
    val GradientFresh = listOf(Color(0xFF2E7D32), Color(0xFF43A047), Color(0xFF66BB6A))
    val GradientSunset = listOf(Color(0xFFD84315), Color(0xFFFF8A65), Color(0xFFFFAB91))
    val GradientNature = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF4CAF50))
    
    // Colores para categorías de productos
    val CategoryVerduras = Color(0xFF66BB6A)
    val CategoryFrutas = Color(0xFFFF8A65)
    val CategoryHortalizas = Color(0xFF9CCC65)
    val CategoryOrganicos = Color(0xFF4DB6AC)
    
    // Colores para estados de pedido
    val OrderPending = Color(0xFFFFB74D)
    val OrderConfirmed = Color(0xFF64B5F6)
    val OrderShipped = Color(0xFFBA68C8)
    val OrderDelivered = Color(0xFF81C784)
    val OrderCancelled = Color(0xFFEF5350)
}

/**
 * Helper para obtener colores según el tema actual
 */
object HuertoHogarTheme {
    @Composable
    fun primary(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.Primary else HuertoHogarColors.Primary
    
    @Composable
    fun primaryVariant(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.PrimaryVariant else HuertoHogarColors.PrimaryVariant
    
    @Composable
    fun secondary(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.Secondary else HuertoHogarColors.Secondary
    
    @Composable
    fun background(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.Background else HuertoHogarColors.Background
    
    @Composable
    fun surface(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.Surface else HuertoHogarColors.Surface
    
    @Composable
    fun onBackground(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.OnBackground else HuertoHogarColors.OnBackground
    
    @Composable
    fun onSurface(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.OnSurface else HuertoHogarColors.OnSurface
    
    @Composable
    fun textSecondary(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.TextSecondary else HuertoHogarColors.TextSecondary
    
    @Composable
    fun accent(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.Accent else HuertoHogarColors.Accent
    
    @Composable
    fun success(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.Success else HuertoHogarColors.Success
    
    @Composable
    fun error(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.Error else HuertoHogarColors.Error
    
    @Composable
    fun warning(): Color = if (isSystemInDarkTheme()) HuertoHogarDarkColors.Warning else HuertoHogarColors.Warning
}
