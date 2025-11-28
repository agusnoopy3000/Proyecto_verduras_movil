package com.example.app_verduras.ui.theme

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
