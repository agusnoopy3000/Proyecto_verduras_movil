package com.example.app_verduras.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_verduras.ui.theme.HuertoHogarColors
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Estados de conexión posibles
 */
enum class ConnectionState {
    CONNECTED,
    DISCONNECTED,
    CONNECTING
}

/**
 * Observa el estado de conexión del dispositivo
 */
@Composable
fun rememberConnectionState(): State<ConnectionState> {
    val context = LocalContext.current
    
    return produceState(initialValue = getCurrentConnectivityState(context)) {
        observeConnectivityAsFlow(context).collect { state ->
            value = state
        }
    }
}

/**
 * Obtiene el estado de conexión actual
 */
private fun getCurrentConnectivityState(context: Context): ConnectionState {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    
    return if (network != null) {
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
            ConnectionState.CONNECTED
        } else {
            ConnectionState.DISCONNECTED
        }
    } else {
        ConnectionState.DISCONNECTED
    }
}

/**
 * Observa cambios en la conectividad como Flow
 */
private fun observeConnectivityAsFlow(context: Context): Flow<ConnectionState> = callbackFlow {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(ConnectionState.CONNECTED)
        }
        
        override fun onLosing(network: Network, maxMsToLive: Int) {
            trySend(ConnectionState.CONNECTING)
        }
        
        override fun onLost(network: Network) {
            trySend(ConnectionState.DISCONNECTED)
        }
        
        override fun onUnavailable() {
            trySend(ConnectionState.DISCONNECTED)
        }
    }
    
    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    
    connectivityManager.registerNetworkCallback(networkRequest, callback)
    
    // Emitir estado inicial
    trySend(getCurrentConnectivityState(context))
    
    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}.distinctUntilChanged()

/**
 * Barra de estado de conexión animada.
 * Se muestra cuando hay problemas de conectividad.
 */
@Composable
fun ConnectionStatusBar(
    modifier: Modifier = Modifier,
    showWhenConnected: Boolean = false
) {
    val connectionState by rememberConnectionState()
    var wasDisconnected by remember { mutableStateOf(false) }
    var showReconnectedMessage by remember { mutableStateOf(false) }
    
    // Detectar reconexión
    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.DISCONNECTED) {
            wasDisconnected = true
        } else if (connectionState == ConnectionState.CONNECTED && wasDisconnected) {
            showReconnectedMessage = true
            kotlinx.coroutines.delay(3000)
            showReconnectedMessage = false
            wasDisconnected = false
        }
    }
    
    val shouldShow = when {
        connectionState == ConnectionState.DISCONNECTED -> true
        connectionState == ConnectionState.CONNECTING -> true
        showReconnectedMessage -> true
        showWhenConnected && connectionState == ConnectionState.CONNECTED -> true
        else -> false
    }
    
    AnimatedVisibility(
        visible = shouldShow,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        val (backgroundColor, icon, message) = when {
            showReconnectedMessage -> Triple(
                HuertoHogarColors.Success,
                Icons.Default.Wifi,
                "¡Conexión restaurada!"
            )
            connectionState == ConnectionState.DISCONNECTED -> Triple(
                HuertoHogarColors.Error,
                Icons.Default.WifiOff,
                "Sin conexión a internet"
            )
            connectionState == ConnectionState.CONNECTING -> Triple(
                HuertoHogarColors.Warning,
                Icons.Default.SignalWifi4Bar,
                "Conectando..."
            )
            else -> Triple(
                HuertoHogarColors.Success,
                Icons.Default.Wifi,
                "Conectado"
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (connectionState == ConnectionState.CONNECTING) {
                // Icono animado de carga
                val infiniteTransition = rememberInfiniteTransition(label = "connecting")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "connecting_alpha"
                )
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = alpha),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = message,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Banner compacto para mostrar estado de conexión
 */
@Composable
fun ConnectionStatusChip(
    modifier: Modifier = Modifier
) {
    val connectionState by rememberConnectionState()
    
    AnimatedVisibility(
        visible = connectionState != ConnectionState.CONNECTED,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        val (color, icon, text) = when (connectionState) {
            ConnectionState.DISCONNECTED -> Triple(
                HuertoHogarColors.Error,
                Icons.Default.WifiOff,
                "Offline"
            )
            ConnectionState.CONNECTING -> Triple(
                HuertoHogarColors.Warning,
                Icons.Default.Sync,
                "Conectando"
            )
            else -> Triple(
                HuertoHogarColors.Success,
                Icons.Default.Wifi,
                "Online"
            )
        }
        
        Surface(
            color = color.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (connectionState == ConnectionState.CONNECTING) {
                    RotatingIcon(
                        icon = Icons.Default.Sync,
                        contentDescription = null,
                        size = 14.dp,
                        tint = Color.White
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Icono con rotación para estado de "conectando"
 */
@Composable
private fun RotatingIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    size: androidx.compose.ui.unit.Dp,
    tint: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotating")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation }
    )
}
