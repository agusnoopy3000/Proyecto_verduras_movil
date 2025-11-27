package com.example.app_verduras.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.example.app_verduras.R
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.viewmodel.CartViewModel

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onConfirmOrder: () -> Unit,
    onGoToCatalog: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()

    if (cartState.items.isEmpty()) {
        EmptyCartView(onGoToCatalog = onGoToCatalog)
    } else {
        FullCartView(cartViewModel, onConfirmOrder)
    }
}

@Composable
fun FullCartView(
    cartViewModel: CartViewModel,
    onConfirmOrder: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HuertoHogarColors.Accent,
                        HuertoHogarColors.Background
                    )
                )
            )
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Tu Carrito",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = HuertoHogarColors.Primary
                    )
                    Text(
                        "${cartState.items.size} productos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HuertoHogarColors.TextSecondary
                    )
                }
                
                // Badge con total de items
                Surface(
                    shape = CircleShape,
                    color = HuertoHogarColors.Primary
                ) {
                    Text(
                        text = "${cartState.items.sumOf { it.qty }}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Lista de productos
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = cartState.items,
                key = { it.product.id }
            ) { item ->
                CartItemCard(
                    item = item,
                    onIncrease = { cartViewModel.increase(item.product.id) },
                    onDecrease = { cartViewModel.decrease(item.product.id) },
                    onRemove = { cartViewModel.remove(item.product.id) }
                )
            }
        }

        // Sección de resumen y checkout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Desglose
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Subtotal",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HuertoHogarColors.TextSecondary
                    )
                    Text(
                        "$${cartState.total.toInt()} CLP",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Envío",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HuertoHogarColors.TextSecondary
                    )
                    Text(
                        "A calcular",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HuertoHogarColors.Secondary
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = HuertoHogarColors.Accent
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total estimado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$${cartState.total.toInt()} CLP",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = HuertoHogarColors.Primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón de checkout
                Button(
                    onClick = onConfirmOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HuertoHogarColors.Primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Default.ShoppingCartCheckout,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Continuar al Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: com.example.app_verduras.viewmodel.CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.product.imagen)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_menu_report_image),
                contentDescription = item.product.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Info del producto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.product.nombre,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = HuertoHogarColors.OnBackground
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    "$${item.product.precio.toInt()} CLP",
                    style = MaterialTheme.typography.bodyLarge,
                    color = HuertoHogarColors.Primary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Controles de cantidad
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HuertoHogarColors.Accent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        IconButton(
                            onClick = onDecrease,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Disminuir",
                                tint = HuertoHogarColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Text(
                            "${item.qty}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = HuertoHogarColors.Primary
                        )
                        
                        IconButton(
                            onClick = onIncrease,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Aumentar",
                                tint = HuertoHogarColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Botón eliminar y subtotal
            Column(
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = HuertoHogarColors.Error,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "$${(item.qty * item.product.precio).toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HuertoHogarColors.OnBackground
                )
            }
        }
    }
}

@Composable
fun EmptyCartView(onGoToCatalog: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_cart))
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HuertoHogarColors.Accent,
                        HuertoHogarColors.Background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(220.dp)
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Tu carrito está vacío",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = HuertoHogarColors.OnBackground
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Parece que todavía no has añadido nada.\n¡Explora nuestros productos frescos!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = HuertoHogarColors.TextSecondary
            )
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = onGoToCatalog,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HuertoHogarColors.Primary
                ),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Ir al Catálogo",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
