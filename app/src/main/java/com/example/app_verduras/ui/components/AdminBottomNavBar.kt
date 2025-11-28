package com.example.app_verduras.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_verduras.ui.theme.HuertoHogarColors

/**
 * Enum que representa las diferentes secciones del panel de administración
 */
enum class AdminSection {
    MENU, PRODUCTS, USERS, ORDERS, DOCUMENTS
}

/**
 * Datos para cada ítem de navegación
 */
data class AdminNavItem(
    val section: AdminSection,
    val title: String,
    val icon: ImageVector,
    val selectedColor: Color
)

/**
 * Barra de navegación inferior para el panel de administración
 * Permite navegación rápida entre las diferentes funcionalidades
 */
@Composable
fun AdminBottomNavBar(
    currentSection: AdminSection,
    onNavigateToMenu: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        AdminNavItem(
            section = AdminSection.MENU,
            title = "Menú",
            icon = Icons.Default.Dashboard,
            selectedColor = HuertoHogarColors.Primary
        ),
        AdminNavItem(
            section = AdminSection.PRODUCTS,
            title = "Productos",
            icon = Icons.Default.Inventory,
            selectedColor = Color(0xFF43A047)
        ),
        AdminNavItem(
            section = AdminSection.USERS,
            title = "Usuarios",
            icon = Icons.Default.People,
            selectedColor = Color(0xFF1E88E5)
        ),
        AdminNavItem(
            section = AdminSection.ORDERS,
            title = "Pedidos",
            icon = Icons.Default.LocalShipping,
            selectedColor = Color(0xFFFF7043)
        ),
        AdminNavItem(
            section = AdminSection.DOCUMENTS,
            title = "Docs",
            icon = Icons.Default.CloudUpload,
            selectedColor = Color(0xFF7E57C2)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                AdminNavBarItem(
                    item = item,
                    isSelected = currentSection == item.section,
                    onClick = {
                        when (item.section) {
                            AdminSection.MENU -> onNavigateToMenu()
                            AdminSection.PRODUCTS -> onNavigateToProducts()
                            AdminSection.USERS -> onNavigateToUsers()
                            AdminSection.ORDERS -> onNavigateToOrders()
                            AdminSection.DOCUMENTS -> onNavigateToDocuments()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminNavBarItem(
    item: AdminNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) item.selectedColor.copy(alpha = 0.15f) else Color.Transparent,
        label = "backgroundColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) item.selectedColor else HuertoHogarColors.TextSecondary,
        label = "contentColor"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = item.selectedColor),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
            fontSize = 10.sp
        )
    }
}

/**
 * Componente de navegación flotante compacto para volver al menú
 * Se puede usar como alternativa al bottom nav cuando se necesita más espacio
 */
@Composable
fun AdminQuickNavigationFab(
    onNavigateToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onNavigateToMenu,
        modifier = modifier,
        containerColor = HuertoHogarColors.Primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Dashboard,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Menú",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
