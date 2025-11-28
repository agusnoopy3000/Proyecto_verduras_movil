package com.example.app_verduras.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_verduras.ai.QuickHelpTopic
import com.example.app_verduras.ui.theme.HuertoHogarColors
import com.example.app_verduras.viewmodel.AIAssistantViewModel
import com.example.app_verduras.viewmodel.CartItem
import com.example.app_verduras.viewmodel.ChatMessage
import com.example.app_verduras.viewmodel.MessageType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    viewModel: AIAssistantViewModel,
    cartItems: List<CartItem> = emptyList(),
    onNavigateBack: () -> Unit
) {
    val chatState by viewModel.chatState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val streamingMessage by viewModel.streamingMessage.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll cuando hay nuevos mensajes
    LaunchedEffect(chatState.messages.size, streamingMessage) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar del asistente
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            HuertoHogarColors.Primary,
                                            HuertoHogarColors.Secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🥬",
                                fontSize = 20.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Asistente Huerto",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = if (isLoading) "Escribiendo..." else "En línea",
                                fontSize = 12.sp,
                                color = if (isLoading) HuertoHogarColors.Secondary else HuertoHogarColors.Primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Botón de recetas si hay productos en el carrito
                    if (cartItems.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.suggestRecipes(cartItems) },
                            enabled = !isLoading
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = "Sugerir recetas",
                                tint = HuertoHogarColors.Secondary
                            )
                        }
                    }
                    // Botón de reiniciar chat
                    IconButton(onClick = { viewModel.resetChat() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Nuevo chat"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HuertoHogarColors.Background
                )
            )
        },
        containerColor = HuertoHogarColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HuertoHogarColors.Background,
                            HuertoHogarColors.Accent.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            // Lista de mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Mostrar mensaje inicial si no hay mensajes
                if (chatState.messages.isEmpty() && !isLoading) {
                    item {
                        WelcomeCard()
                    }
                }
                
                // Mensajes del chat
                items(chatState.messages) { message ->
                    ChatBubble(message = message)
                }
                
                // Mensaje de streaming (mientras escribe)
                if (streamingMessage.isNotEmpty()) {
                    item {
                        ChatBubble(
                            message = ChatMessage(
                                content = streamingMessage,
                                isFromUser = false,
                                type = MessageType.TEXT
                            )
                        )
                    }
                }
                
                // Indicador de carga
                if (isLoading && streamingMessage.isEmpty()) {
                    item {
                        TypingIndicator()
                    }
                }
            }
            
            // Botones de ayuda rápida
            if (chatState.messages.size <= 1) {
                QuickHelpButtons(
                    onQuickHelp = { topic ->
                        viewModel.quickHelp(topic)
                    },
                    enabled = !isLoading
                )
            }
            
            // Campo de entrada
            ChatInputField(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = HuertoHogarColors.Primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🥬🍅🥕",
                fontSize = 40.sp
            )
            Text(
                text = "¡Bienvenido al Asistente de Huerto Hogar!",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = HuertoHogarColors.Primary
            )
            Text(
                text = "Puedo ayudarte con:\n• Recetas con tus productos\n• Información nutricional\n• Tips de conservación\n• Y mucho más...",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = HuertoHogarColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val bubbleColor = when {
        message.isFromUser -> HuertoHogarColors.Primary
        message.type == MessageType.ERROR -> Color(0xFFE57373)
        else -> Color.White
    }
    
    val textColor = when {
        message.isFromUser -> Color.White
        message.type == MessageType.ERROR -> Color.White
        else -> HuertoHogarColors.OnSurface
    }
    
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .shadow(2.dp, shape)
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            // Icono de tipo de mensaje
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!message.isFromUser) {
                    when (message.type) {
                        MessageType.RECIPE -> Text("🍳", fontSize = 16.sp)
                        MessageType.PRODUCT_SUGGESTION -> Text("🛒", fontSize = 16.sp)
                        MessageType.PRODUCT_INFO -> Text("ℹ️", fontSize = 16.sp)
                        MessageType.ERROR -> Text("⚠️", fontSize = 16.sp)
                        else -> {}
                    }
                }
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "typing")
        
        repeat(3) { index ->
            val delay = index * 100
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 600
                        0.3f at 0
                        1f at 300
                        0.3f at 600
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(delay)
                ),
                label = "dot_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(HuertoHogarColors.Primary.copy(alpha = alpha))
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "Escribiendo...",
            fontSize = 12.sp,
            color = HuertoHogarColors.TextSecondary
        )
    }
}

@Composable
private fun QuickHelpButtons(
    onQuickHelp: (QuickHelpTopic) -> Unit,
    enabled: Boolean
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            QuickHelpChip(
                icon = Icons.Default.LocalShipping,
                label = "Delivery",
                onClick = { onQuickHelp(QuickHelpTopic.DELIVERY) },
                enabled = enabled
            )
        }
        item {
            QuickHelpChip(
                icon = Icons.Default.Eco,
                label = "Orgánicos",
                onClick = { onQuickHelp(QuickHelpTopic.ORGANIC) },
                enabled = enabled
            )
        }
        item {
            QuickHelpChip(
                icon = Icons.Default.Kitchen,
                label = "Conservación",
                onClick = { onQuickHelp(QuickHelpTopic.CONSERVATION) },
                enabled = enabled
            )
        }
        item {
            QuickHelpChip(
                icon = Icons.Default.CalendarMonth,
                label = "Temporada",
                onClick = { onQuickHelp(QuickHelpTopic.SEASONAL) },
                enabled = enabled
            )
        }
        item {
            QuickHelpChip(
                icon = Icons.Default.Payment,
                label = "Pagos",
                onClick = { onQuickHelp(QuickHelpTopic.PAYMENT) },
                enabled = enabled
            )
        }
    }
}

@Composable
private fun QuickHelpChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = HuertoHogarColors.Accent,
            labelColor = HuertoHogarColors.Primary,
            leadingIconContentColor = HuertoHogarColors.Primary
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = enabled,
            borderColor = HuertoHogarColors.Primary.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun ChatInputField(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "Escribe tu mensaje...",
                        color = HuertoHogarColors.TextSecondary
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HuertoHogarColors.Primary,
                    unfocusedBorderColor = HuertoHogarColors.Primary.copy(alpha = 0.3f),
                    focusedContainerColor = HuertoHogarColors.Accent.copy(alpha = 0.3f),
                    unfocusedContainerColor = HuertoHogarColors.Accent.copy(alpha = 0.3f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4,
                enabled = !isLoading
            )
            
            // Botón de enviar
            FilledIconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = HuertoHogarColors.Primary,
                    contentColor = Color.White,
                    disabledContainerColor = HuertoHogarColors.Primary.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar"
                    )
                }
            }
        }
    }
}
