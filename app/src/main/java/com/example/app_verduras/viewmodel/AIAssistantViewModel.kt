package com.example.app_verduras.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.ai.AIResponse
import com.example.app_verduras.ai.HuertoAIAssistant
import com.example.app_verduras.ai.QuickHelpTopic
import com.example.app_verduras.util.AnalyticsManager
import com.example.app_verduras.util.CrashlyticsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para el asistente de IA de Huerto Hogar.
 * Maneja el estado del chat y la interacción con el modelo de IA.
 */
class AIAssistantViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "AIAssistantViewModel"
    }
    
    // Estado del chat
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()
    
    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Estado de streaming (para mostrar la respuesta mientras se genera)
    private val _streamingMessage = MutableStateFlow("")
    val streamingMessage: StateFlow<String> = _streamingMessage.asStateFlow()
    
    init {
        // Enviar saludo inicial cuando se abre el chat
        sendGreeting(null)
    }
    
    /**
     * Envía el saludo inicial al abrir el chat
     */
    fun sendGreeting(userName: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = HuertoAIAssistant.getGreeting(userName)
                when (response) {
                    is AIResponse.Success -> {
                        addMessage(ChatMessage(
                            content = response.message,
                            isFromUser = false,
                            type = MessageType.TEXT
                        ))
                    }
                    is AIResponse.Error -> {
                        addMessage(ChatMessage(
                            content = "¡Hola! 👋 Soy el asistente de Huerto Hogar. ¿En qué puedo ayudarte hoy?",
                            isFromUser = false,
                            type = MessageType.TEXT
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en saludo: ${e.message}", e)
                CrashlyticsManager.recordException(e)
                addMessage(ChatMessage(
                    content = "¡Hola! 👋 Soy el asistente de Huerto Hogar. ¿En qué puedo ayudarte hoy?",
                    isFromUser = false,
                    type = MessageType.TEXT
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Envía un mensaje del usuario al asistente
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        // Agregar mensaje del usuario
        addMessage(ChatMessage(
            content = message,
            isFromUser = true,
            type = MessageType.TEXT
        ))
        
        // Registrar en Analytics
        AnalyticsManager.logSearch(message)
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = HuertoAIAssistant.sendMessage(message)
                when (response) {
                    is AIResponse.Success -> {
                        addMessage(ChatMessage(
                            content = response.message,
                            isFromUser = false,
                            type = MessageType.TEXT
                        ))
                    }
                    is AIResponse.Error -> {
                        addMessage(ChatMessage(
                            content = response.errorMessage,
                            isFromUser = false,
                            type = MessageType.ERROR
                        ))
                        CrashlyticsManager.log("AI Error: ${response.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar mensaje: ${e.message}", e)
                CrashlyticsManager.recordException(e)
                addMessage(ChatMessage(
                    content = "Lo siento, hubo un error al procesar tu mensaje. Por favor, intenta de nuevo.",
                    isFromUser = false,
                    type = MessageType.ERROR
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Envía un mensaje con streaming de respuesta
     */
    fun sendMessageWithStreaming(message: String) {
        if (message.isBlank()) return
        
        // Agregar mensaje del usuario
        addMessage(ChatMessage(
            content = message,
            isFromUser = true,
            type = MessageType.TEXT
        ))
        
        viewModelScope.launch {
            _isLoading.value = true
            _streamingMessage.value = ""
            
            try {
                val fullResponse = StringBuilder()
                
                HuertoAIAssistant.sendMessageStream(message).collect { chunk ->
                    fullResponse.append(chunk)
                    _streamingMessage.value = fullResponse.toString()
                }
                
                // Al terminar, agregar el mensaje completo
                if (fullResponse.isNotEmpty()) {
                    addMessage(ChatMessage(
                        content = fullResponse.toString(),
                        isFromUser = false,
                        type = MessageType.TEXT
                    ))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en streaming: ${e.message}", e)
                CrashlyticsManager.recordException(e)
                addMessage(ChatMessage(
                    content = "Lo siento, hubo un error. Por favor, intenta de nuevo.",
                    isFromUser = false,
                    type = MessageType.ERROR
                ))
            } finally {
                _isLoading.value = false
                _streamingMessage.value = ""
            }
        }
    }
    
    /**
     * Solicita sugerencias de recetas basadas en el carrito
     */
    fun suggestRecipes(cartItems: List<CartItem>) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Agregar mensaje del usuario
            addMessage(ChatMessage(
                content = "¿Qué recetas puedo hacer con los productos de mi carrito? 🍳",
                isFromUser = true,
                type = MessageType.TEXT
            ))
            
            try {
                val response = HuertoAIAssistant.suggestRecipes(cartItems)
                when (response) {
                    is AIResponse.Success -> {
                        addMessage(ChatMessage(
                            content = response.message,
                            isFromUser = false,
                            type = MessageType.RECIPE
                        ))
                    }
                    is AIResponse.Error -> {
                        addMessage(ChatMessage(
                            content = response.errorMessage,
                            isFromUser = false,
                            type = MessageType.ERROR
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al sugerir recetas: ${e.message}", e)
                CrashlyticsManager.recordException(e)
                addMessage(ChatMessage(
                    content = "No pude generar sugerencias de recetas. Por favor, intenta de nuevo.",
                    isFromUser = false,
                    type = MessageType.ERROR
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Realiza una búsqueda inteligente de productos
     */
    fun smartSearch(query: String, availableProducts: List<Producto>) {
        viewModelScope.launch {
            _isLoading.value = true
            
            addMessage(ChatMessage(
                content = "Buscar: $query",
                isFromUser = true,
                type = MessageType.TEXT
            ))
            
            try {
                val response = HuertoAIAssistant.smartSearch(query, availableProducts)
                when (response) {
                    is AIResponse.Success -> {
                        addMessage(ChatMessage(
                            content = response.message,
                            isFromUser = false,
                            type = MessageType.PRODUCT_SUGGESTION
                        ))
                    }
                    is AIResponse.Error -> {
                        addMessage(ChatMessage(
                            content = response.errorMessage,
                            isFromUser = false,
                            type = MessageType.ERROR
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en búsqueda: ${e.message}", e)
                CrashlyticsManager.recordException(e)
                addMessage(ChatMessage(
                    content = "No pude realizar la búsqueda. Por favor, intenta de nuevo.",
                    isFromUser = false,
                    type = MessageType.ERROR
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Obtiene información sobre un producto específico
     */
    fun getProductInfo(producto: Producto) {
        viewModelScope.launch {
            _isLoading.value = true
            
            addMessage(ChatMessage(
                content = "Cuéntame sobre ${producto.nombre} 🔍",
                isFromUser = true,
                type = MessageType.TEXT
            ))
            
            try {
                val response = HuertoAIAssistant.getProductInfo(producto)
                when (response) {
                    is AIResponse.Success -> {
                        addMessage(ChatMessage(
                            content = response.message,
                            isFromUser = false,
                            type = MessageType.PRODUCT_INFO
                        ))
                    }
                    is AIResponse.Error -> {
                        addMessage(ChatMessage(
                            content = response.errorMessage,
                            isFromUser = false,
                            type = MessageType.ERROR
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener info: ${e.message}", e)
                CrashlyticsManager.recordException(e)
                addMessage(ChatMessage(
                    content = "No pude obtener información del producto.",
                    isFromUser = false,
                    type = MessageType.ERROR
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Obtiene ayuda rápida sobre un tema específico
     */
    fun quickHelp(topic: QuickHelpTopic) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val userMessage = when (topic) {
                QuickHelpTopic.DELIVERY -> "¿Cómo funciona el delivery? 🚚"
                QuickHelpTopic.ORGANIC -> "¿Qué son los productos orgánicos? 🌱"
                QuickHelpTopic.CONSERVATION -> "Tips para conservar alimentos 🥬"
                QuickHelpTopic.SEASONAL -> "Productos de temporada 📅"
                QuickHelpTopic.PAYMENT -> "Métodos de pago 💳"
            }
            
            addMessage(ChatMessage(
                content = userMessage,
                isFromUser = true,
                type = MessageType.QUICK_ACTION
            ))
            
            try {
                val response = HuertoAIAssistant.quickHelp(topic)
                when (response) {
                    is AIResponse.Success -> {
                        addMessage(ChatMessage(
                            content = response.message,
                            isFromUser = false,
                            type = MessageType.TEXT
                        ))
                    }
                    is AIResponse.Error -> {
                        addMessage(ChatMessage(
                            content = response.errorMessage,
                            isFromUser = false,
                            type = MessageType.ERROR
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en ayuda rápida: ${e.message}", e)
                CrashlyticsManager.recordException(e)
                addMessage(ChatMessage(
                    content = "No pude obtener la información solicitada.",
                    isFromUser = false,
                    type = MessageType.ERROR
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Reinicia el chat
     */
    fun resetChat() {
        HuertoAIAssistant.resetChat()
        _chatState.update { ChatState() }
        sendGreeting(null)
    }
    
    /**
     * Agrega un mensaje al historial
     */
    private fun addMessage(message: ChatMessage) {
        _chatState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + message
            )
        }
    }
    
    /**
     * Factory para crear el ViewModel
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AIAssistantViewModel::class.java)) {
                return AIAssistantViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Estado del chat
 */
data class ChatState(
    val messages: List<ChatMessage> = emptyList()
)

/**
 * Mensaje individual del chat
 */
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Tipos de mensajes
 */
enum class MessageType {
    TEXT,
    RECIPE,
    PRODUCT_SUGGESTION,
    PRODUCT_INFO,
    ERROR,
    QUICK_ACTION
}
