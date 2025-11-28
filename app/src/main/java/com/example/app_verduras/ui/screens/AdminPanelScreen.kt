package com.example.app_verduras.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.util.SessionManager

/**
 * Modelo de datos para las opciones del panel de administrador
 */
data class AdminOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateToProductManagement: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToOrderManagement: () -> Unit,
    onNavigateToDocumentManagement: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onLogout: () -> Unit
) {
    val adminName = SessionManager.currentUser?.nombre ?: "Administrador"
    
    val adminOptions = listOf(
        AdminOption(
            title = "Dashboard",
            subtitle = "Estadísticas",
            icon = Icons.Default.Dashboard,
            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
            onClick = onNavigateToDashboard
        ),
        AdminOption(
            title = "Productos",
            subtitle = "Gestionar catálogo",
            icon = Icons.Default.Inventory,
            gradientColors = listOf(Color(0xFF43A047), Color(0xFF66BB6A)),
            onClick = onNavigateToProductManagement
        ),
        AdminOption(
            title = "Usuarios",
            subtitle = "Gestionar cuentas",
            icon = Icons.Default.People,
            gradientColors = listOf(Color(0xFF1E88E5), Color(0xFF42A5F5)),
            onClick = onNavigateToUserManagement
        ),
        AdminOption(
            title = "Pedidos",
            subtitle = "Ver y actualizar",
            icon = Icons.Default.LocalShipping,
            gradientColors = listOf(Color(0xFFFF7043), Color(0xFFFFAB91)),
            onClick = onNavigateToOrderManagement
        ),
        AdminOption(
            title = "Documentos",
            subtitle = "Subir archivos",
            icon = Icons.Default.CloudUpload,
            gradientColors = listOf(Color(0xFF7E57C2), Color(0xFFB39DDB)),
            onClick = onNavigateToDocumentManagement
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp, 
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HuertoHogarColors.Primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            // Header con saludo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "¡Hola, $adminName! 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = HuertoHogarColors.Primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Panel de Administración",
                    style = MaterialTheme.typography.bodyLarge,
                    color = HuertoHogarColors.TextSecondary
                )
            }

            // Estadísticas rápidas (placeholder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HuertoHogarColors.AccentWarm
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(value = "24", label = "Productos", icon = Icons.Default.Inventory)
                    StatItem(value = "156", label = "Usuarios", icon = Icons.Default.People)
                    StatItem(value = "12", label = "Pedidos Hoy", icon = Icons.Default.ShoppingCart)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título de sección
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = HuertoHogarColors.OnBackground
            )

            // Grid de opciones
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(adminOptions) { option ->
                    AdminOptionCard(option = option)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HuertoHogarColors.Primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = HuertoHogarColors.Primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = HuertoHogarColors.TextSecondary
        )
    }
}

@Composable
private fun AdminOptionCard(option: AdminOption) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = option.gradientColors[0].copy(alpha = 0.3f),
                spotColor = option.gradientColors[0].copy(alpha = 0.3f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White),
                onClick = option.onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(colors = option.gradientColors)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.title,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
