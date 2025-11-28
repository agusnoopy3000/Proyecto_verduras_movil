package com.example.app_verduras.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_verduras.ui.theme.HuertoHogarColors

/**
 * Pantalla de Perfil del Usuario
 * Muestra información personal y opciones de configuración
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String = "Usuario",
    userEmail: String = "usuario@ejemplo.com",
    userPhone: String = "+56 9 1234 5678",
    userAddress: String = "Santiago, Chile",
    onEditProfile: () -> Unit = {},
    onAddPaymentMethod: () -> Unit = {},
    onViewOrders: () -> Unit = {},
    onScanQR: () -> Unit = {},
    onSecuritySettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showPaymentDialog by remember { mutableStateOf(false) }
    var savedCards by remember { mutableStateOf(listOf<PaymentCard>()) }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HuertoHogarColors.Primary.copy(alpha = 0.08f),
                        HuertoHogarColors.Accent.copy(alpha = 0.5f),
                        HuertoHogarColors.Background
                    )
                )
            )
            .verticalScroll(scrollState)
    ) {
        // Header con avatar y nombre
        ProfileHeader(
            userName = userName,
            userEmail = userEmail
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de información personal
        ProfileSection(
            title = "Información Personal",
            icon = Icons.Outlined.Person
        ) {
            ProfileInfoItem(
                icon = Icons.Outlined.Email,
                label = "Correo electrónico",
                value = userEmail
            )
            ProfileInfoItem(
                icon = Icons.Outlined.Phone,
                label = "Teléfono",
                value = userPhone
            )
            ProfileInfoItem(
                icon = Icons.Outlined.LocationOn,
                label = "Dirección",
                value = userAddress
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar perfil")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de métodos de pago
        ProfileSection(
            title = "Métodos de Pago",
            icon = Icons.Outlined.CreditCard
        ) {
            if (savedCards.isEmpty()) {
                EmptyPaymentState(onAddCard = { showPaymentDialog = true })
            } else {
                savedCards.forEach { card ->
                    PaymentCardItem(
                        card = card,
                        onRemove = { savedCards = savedCards.filter { it.id != card.id } }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showPaymentDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar otra tarjeta")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de acciones rápidas
        ProfileSection(
            title = "Acciones Rápidas",
            icon = Icons.Outlined.Speed
        ) {
            // Escanear QR - Recurso nativo de cámara
            QuickActionItem(
                icon = Icons.Default.QrCodeScanner,
                title = "Escanear Producto",
                subtitle = "Usa la cámara para agregar productos",
                onClick = onScanQR,
                badge = "📷"
            )
            QuickActionItem(
                icon = Icons.Default.Receipt,
                title = "Mis Pedidos",
                subtitle = "Ver historial de compras",
                onClick = onViewOrders
            )
            QuickActionItem(
                icon = Icons.Default.LocalOffer,
                title = "Ofertas Especiales",
                subtitle = "Productos con descuento",
                onClick = { /* Deep link a ofertas */ },
                badge = "🔥"
            )
            QuickActionItem(
                icon = Icons.Default.Help,
                title = "Ayuda y Soporte",
                subtitle = "Preguntas frecuentes",
                onClick = { }
            )
            QuickActionItem(
                icon = Icons.Default.Security,
                title = "Seguridad",
                subtitle = "Verificación en dos pasos",
                onClick = onSecuritySettings,
                badge = "🔐"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de cerrar sesión
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Dialog para agregar método de pago
    if (showPaymentDialog) {
        AddPaymentMethodDialog(
            onDismiss = { showPaymentDialog = false },
            onSave = { card ->
                savedCards = savedCards + card
                showPaymentDialog = false
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    userName: String,
    userEmail: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HuertoHogarColors.Primary,
                        HuertoHogarColors.Primary.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = HuertoHogarColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badge de cliente
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Cliente Frecuente",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = HuertoHogarColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HuertoHogarColors.OnBackground
                )
            }
            content()
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = HuertoHogarColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = HuertoHogarColors.TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = HuertoHogarColors.OnBackground
            )
        }
    }
}

@Composable
private fun EmptyPaymentState(onAddCard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.CreditCard,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = HuertoHogarColors.TextSecondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "No tienes métodos de pago guardados",
            style = MaterialTheme.typography.bodyMedium,
            color = HuertoHogarColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddCard,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HuertoHogarColors.Primary
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar tarjeta")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "💳 Próximamente: Pasarela de pago integrada",
            style = MaterialTheme.typography.labelSmall,
            color = HuertoHogarColors.TextSecondary.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PaymentCardItem(
    card: PaymentCard,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HuertoHogarColors.Accent.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de tarjeta según el tipo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (card.type) {
                        CardType.VISA -> "💳"
                        CardType.MASTERCARD -> "💳"
                        CardType.AMEX -> "💳"
                        else -> "💳"
                    },
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "**** **** **** ${card.lastFourDigits}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Vence: ${card.expiryDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = HuertoHogarColors.TextSecondary
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar tarjeta",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HuertoHogarColors.Background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HuertoHogarColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = HuertoHogarColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(badge, fontSize = 14.sp)
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = HuertoHogarColors.TextSecondary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = HuertoHogarColors.TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPaymentMethodDialog(
    onDismiss: () -> Unit,
    onSave: (PaymentCard) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Agregar Método de Pago",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "⚠️ Esta funcionalidad es solo visual. La pasarela de pago se implementará próximamente.",
                    style = MaterialTheme.typography.labelSmall,
                    color = HuertoHogarColors.TextSecondary
                )
                
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { if (it.length <= 16) cardNumber = it.filter { c -> c.isDigit() } },
                    label = { Text("Número de tarjeta") },
                    placeholder = { Text("1234 5678 9012 3456") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { 
                            if (it.length <= 5) {
                                expiryDate = it.filter { c -> c.isDigit() || c == '/' }
                            }
                        },
                        label = { Text("Vencimiento") },
                        placeholder = { Text("MM/AA") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 4) cvv = it.filter { c -> c.isDigit() } },
                        label = { Text("CVV") },
                        placeholder = { Text("123") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = cardHolder,
                    onValueChange = { cardHolder = it },
                    label = { Text("Nombre del titular") },
                    placeholder = { Text("Como aparece en la tarjeta") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cardNumber.length >= 4) {
                        onSave(
                            PaymentCard(
                                id = System.currentTimeMillis().toString(),
                                lastFourDigits = cardNumber.takeLast(4),
                                expiryDate = expiryDate,
                                type = CardType.VISA,
                                holderName = cardHolder
                            )
                        )
                    }
                },
                enabled = cardNumber.length >= 4 && expiryDate.isNotBlank() && cardHolder.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Data classes para métodos de pago
data class PaymentCard(
    val id: String,
    val lastFourDigits: String,
    val expiryDate: String,
    val type: CardType,
    val holderName: String
)

enum class CardType {
    VISA, MASTERCARD, AMEX, OTHER
}
