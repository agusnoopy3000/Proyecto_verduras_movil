package com.example.app_verduras.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.app_verduras.R
import com.example.app_verduras.ui.theme.HuertoHogarColors

/**
 * Tipos de estados vacíos predefinidos
 */
enum class EmptyStateType {
    EMPTY_CART,
    NO_ORDERS,
    NO_PRODUCTS,
    NO_RESULTS,
    ERROR,
    NO_CONNECTION,
    NO_NOTIFICATIONS,
    NO_FAVORITES
}

/**
 * Vista de estado vacío con ilustración animada.
 * Proporciona feedback visual atractivo cuando no hay contenido.
 */
@Composable
fun EmptyStateView(
    type: EmptyStateType,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val (defaultTitle, defaultMessage, icon, lottieRes, gradientColors) = when (type) {
        EmptyStateType.EMPTY_CART -> EmptyStateData(
            title = "Tu carrito está vacío",
            message = "Explora nuestro catálogo y agrega productos frescos a tu carrito",
            icon = Icons.Outlined.ShoppingCart,
            lottieRes = R.raw.empty_cart,
            gradientColors = listOf(
                HuertoHogarColors.Secondary.copy(alpha = 0.1f),
                HuertoHogarColors.Secondary.copy(alpha = 0.05f)
            )
        )
        EmptyStateType.NO_ORDERS -> EmptyStateData(
            title = "Sin pedidos aún",
            message = "Cuando realices tu primera compra, aparecerá aquí",
            icon = Icons.Outlined.Receipt,
            lottieRes = R.raw.empty_cart, // Reutilizamos empty_cart
            gradientColors = listOf(
                HuertoHogarColors.Primary.copy(alpha = 0.1f),
                HuertoHogarColors.Primary.copy(alpha = 0.05f)
            )
        )
        EmptyStateType.NO_PRODUCTS -> EmptyStateData(
            title = "No hay productos",
            message = "Pronto tendremos más productos disponibles para ti",
            icon = Icons.Outlined.Inventory2,
            lottieRes = R.raw.orange_skating, // Animación divertida
            gradientColors = listOf(
                HuertoHogarColors.Primary.copy(alpha = 0.1f),
                HuertoHogarColors.Accent.copy(alpha = 0.3f)
            )
        )
        EmptyStateType.NO_RESULTS -> EmptyStateData(
            title = "Sin resultados",
            message = "No encontramos lo que buscas. Intenta con otros términos",
            icon = Icons.Outlined.SearchOff,
            lottieRes = R.raw.orange_skating,
            gradientColors = listOf(
                HuertoHogarColors.Info.copy(alpha = 0.1f),
                HuertoHogarColors.Info.copy(alpha = 0.05f)
            )
        )
        EmptyStateType.ERROR -> EmptyStateData(
            title = "¡Ups! Algo salió mal",
            message = "Ocurrió un error inesperado. Por favor intenta de nuevo",
            icon = Icons.Outlined.ErrorOutline,
            lottieRes = R.raw.empty_cart,
            gradientColors = listOf(
                HuertoHogarColors.Error.copy(alpha = 0.1f),
                HuertoHogarColors.Error.copy(alpha = 0.05f)
            )
        )
        EmptyStateType.NO_CONNECTION -> EmptyStateData(
            title = "Sin conexión",
            message = "Verifica tu conexión a internet e intenta de nuevo",
            icon = Icons.Outlined.WifiOff,
            lottieRes = R.raw.saving_cloud,
            gradientColors = listOf(
                HuertoHogarColors.Warning.copy(alpha = 0.1f),
                HuertoHogarColors.Warning.copy(alpha = 0.05f)
            )
        )
        EmptyStateType.NO_NOTIFICATIONS -> EmptyStateData(
            title = "Sin notificaciones",
            message = "Cuando tengas novedades, te avisaremos aquí",
            icon = Icons.Outlined.NotificationsOff,
            lottieRes = R.raw.confetti,
            gradientColors = listOf(
                HuertoHogarColors.Info.copy(alpha = 0.1f),
                HuertoHogarColors.Info.copy(alpha = 0.05f)
            )
        )
        EmptyStateType.NO_FAVORITES -> EmptyStateData(
            title = "Sin favoritos",
            message = "Guarda tus productos favoritos para encontrarlos fácilmente",
            icon = Icons.Outlined.FavoriteBorder,
            lottieRes = R.raw.orange_skating,
            gradientColors = listOf(
                HuertoHogarColors.Error.copy(alpha = 0.1f),
                HuertoHogarColors.Secondary.copy(alpha = 0.1f)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(gradientColors)
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Intentar mostrar Lottie, si falla mostrar icono animado
        AnimatedEmptyIcon(
            icon = icon,
            lottieRes = lottieRes
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title ?: defaultTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = HuertoHogarColors.OnBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = message ?: defaultMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = HuertoHogarColors.TextSecondary,
            lineHeight = 22.sp
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HuertoHogarColors.Primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AnimatedEmptyIcon(
    icon: ImageVector,
    lottieRes: Int
) {
    // Intentar cargar animación Lottie
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    if (composition != null) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(180.dp)
        )
    } else {
        // Fallback: icono animado
        AnimatedIconFallback(icon = icon)
    }
}

@Composable
private fun AnimatedIconFallback(icon: ImageVector) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_icon")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_alpha"
    )
    
    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(HuertoHogarColors.Primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(70.dp),
            tint = HuertoHogarColors.Primary.copy(alpha = alpha)
        )
    }
}

/**
 * Data class para configuración de estados vacíos
 */
private data class EmptyStateData(
    val title: String,
    val message: String,
    val icon: ImageVector,
    val lottieRes: Int,
    val gradientColors: List<Color>
)

/**
 * Versión compacta del estado vacío para espacios reducidos
 */
@Composable
fun EmptyStateCompact(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = HuertoHogarColors.TextSecondary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = HuertoHogarColors.TextSecondary
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    color = HuertoHogarColors.Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
