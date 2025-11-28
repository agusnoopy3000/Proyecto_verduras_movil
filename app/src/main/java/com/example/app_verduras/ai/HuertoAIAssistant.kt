package com.example.app_verduras.ai

import android.util.Log
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.viewmodel.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.vertexai.FirebaseVertexAI
import com.google.firebase.vertexai.type.Content
import com.google.firebase.vertexai.type.GenerateContentResponse
import com.google.firebase.vertexai.type.GenerationConfig
import com.google.firebase.vertexai.type.HarmBlockThreshold
import com.google.firebase.vertexai.type.HarmCategory
import com.google.firebase.vertexai.type.SafetySetting
import com.google.firebase.vertexai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Asistente de IA para Huerto Hogar usando Firebase Vertex AI.
 * 
 * Funcionalidades:
 * - Chat conversacional sobre productos orgánicos
 * - Recomendaciones de recetas basadas en el carrito
 * - Sugerencias de productos
 * - Tips de conservación y cultivo
 * - Búsqueda semántica inteligente
 */
object HuertoAIAssistant {
    
    private const val TAG = "HuertoAIAssistant"
    
    // Modelo de Gemini a usar (actualizado a versión 2.0)
   
    private const val MODEL_NAME = "gemini-2.0-flash"
    
    // Configuración del modelo
    private val generationConfig = GenerationConfig.builder().apply {
        temperature = 0.7f
        topK = 40
        topP = 0.95f
        maxOutputTokens = 1024
    }.build()
    
    // Configuración de seguridad
    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, HarmBlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, HarmBlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, HarmBlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, HarmBlockThreshold.MEDIUM_AND_ABOVE)
    )
    
    // Prompt del sistema para el asistente
    private val systemPrompt = """
        Eres el asistente virtual de "Huerto Hogar", una tienda de productos orgánicos y hortalizas frescas en Chile.
        
        Tu personalidad:
        - Amigable, cercano y entusiasta sobre la alimentación saludable
        - Experto en productos orgánicos, frutas, verduras y alimentos naturales
        - Conocedor de recetas chilenas y cocina saludable
        - Siempre das respuestas útiles y prácticas
        
        Tus capacidades:
        - Recomendar productos basados en las necesidades del usuario
        - Sugerir recetas con los productos disponibles o del carrito
        - Dar tips de conservación de alimentos
        - Explicar beneficios nutricionales de los productos
        - Responder preguntas sobre agricultura orgánica
        
        Reglas importantes:
        - Responde siempre en español chileno (usa expresiones como "bacán", "rico", etc. ocasionalmente)
        - Mantén las respuestas concisas pero informativas (máximo 3-4 párrafos)
        - Si no sabes algo, dilo honestamente
        - Siempre relaciona las respuestas con productos que podrían encontrar en la tienda
        - Usa emojis ocasionalmente para hacer la conversación más amigable 🥬🍅🥕
        
        Contexto de la tienda:
        - Vendemos frutas, verduras, lácteos, cereales y productos orgánicos
        - Hacemos delivery a domicilio
        - Nuestros productos son frescos y de origen local cuando es posible
    """.trimIndent()
    
    // Instancia del modelo
    private val generativeModel by lazy {
        FirebaseVertexAI.instance.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig,
            safetySettings = safetySettings,
            systemInstruction = content { text(systemPrompt) }
        )
    }
    
    // Chat con historial
    private var chat = generativeModel.startChat()
    
    /**
     * Reinicia el chat (nueva conversación)
     */
    fun resetChat() {
        chat = generativeModel.startChat()
        Log.d(TAG, "Chat reiniciado")
    }
    
    /**
     * Envía un mensaje al asistente y obtiene la respuesta.
     * 
     * @param message Mensaje del usuario
     * @return Respuesta del asistente
     */
    suspend fun sendMessage(message: String): AIResponse {
        // Verificar si el usuario está autenticado en Firebase
        if (FirebaseAuth.getInstance().currentUser == null) {
            Log.w(TAG, "Usuario no autenticado en Firebase Auth")
            return AIResponse.Error(
                "Para usar el asistente IA, necesitas iniciar sesión. " +
                "Por favor, cierra sesión y vuelve a iniciar con tu cuenta. 🔐"
            )
        }
        
        return try {
            Log.d(TAG, "Enviando mensaje: $message")
            val response = chat.sendMessage(message)
            val responseText = response.text ?: "Lo siento, no pude generar una respuesta."
            Log.d(TAG, "Respuesta recibida: ${responseText.take(100)}...")
            AIResponse.Success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar mensaje: ${e.message}", e)
            
            // Manejar errores específicos
            val errorMessage = when {
                e.message?.contains("App Check") == true -> 
                    "El servicio de IA no está disponible en este momento. Intenta más tarde. 🔧"
                e.message?.contains("sign in") == true || e.message?.contains("auth") == true ->
                    "Necesitas iniciar sesión para usar el asistente. 🔐"
                e.message?.contains("PERMISSION_DENIED") == true ->
                    "No tienes permisos para usar este servicio. Contacta al administrador. 🚫"
                e.message?.contains("network") == true || e.message?.contains("connect") == true ->
                    "Error de conexión. Verifica tu internet y vuelve a intentar. 📶"
                else -> "No pude procesar tu mensaje. Por favor, intenta de nuevo. 😅"
            }
            AIResponse.Error(errorMessage)
        }
    }
    
    /**
     * Envía un mensaje y obtiene la respuesta en streaming.
     * Útil para mostrar la respuesta mientras se genera.
     */
    fun sendMessageStream(message: String): Flow<String> = flow {
        // Verificar si el usuario está autenticado en Firebase
        if (FirebaseAuth.getInstance().currentUser == null) {
            Log.w(TAG, "Usuario no autenticado en Firebase Auth (stream)")
            emit("Para usar el asistente IA, necesitas iniciar sesión. Por favor, cierra sesión y vuelve a iniciar con tu cuenta. 🔐")
            return@flow
        }
        
        try {
            Log.d(TAG, "Enviando mensaje (stream): $message")
            val response = chat.sendMessageStream(message)
            response.collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en streaming: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("App Check") == true -> 
                    "El servicio de IA no está disponible en este momento. 🔧"
                e.message?.contains("sign in") == true || e.message?.contains("auth") == true ->
                    "Necesitas iniciar sesión para usar el asistente. 🔐"
                else -> "Error: No pude procesar tu mensaje. 😅"
            }
            emit(errorMessage)
        }
    }
    
    /**
     * Genera recetas basadas en los productos del carrito.
     * 
     * @param cartItems Lista de productos en el carrito
     * @return Sugerencias de recetas
     */
    suspend fun suggestRecipes(cartItems: List<CartItem>): AIResponse {
        if (cartItems.isEmpty()) {
            return AIResponse.Success("¡Tu carrito está vacío! Agrega algunos productos y te sugeriré deliciosas recetas 🛒")
        }
        
        val productList = cartItems.joinToString(", ") { 
            "${it.product.nombre} (${it.qty} unidades)" 
        }
        
        val prompt = """
            Tengo estos productos en mi carrito de compras:
            $productList
            
            Por favor, sugiéreme 2-3 recetas saludables y fáciles de preparar que pueda hacer con estos ingredientes.
            Para cada receta incluye:
            - Nombre de la receta
            - Ingredientes necesarios (marca cuáles tengo y cuáles me faltan)
            - Pasos breves de preparación
            - Tiempo aproximado de preparación
        """.trimIndent()
        
        return sendMessage(prompt)
    }
    
    /**
     * Busca productos de forma inteligente usando lenguaje natural.
     * 
     * @param query Búsqueda del usuario (ej: "algo para ensalada")
     * @param availableProducts Lista de productos disponibles
     * @return Sugerencias de productos
     */
    suspend fun smartSearch(query: String, availableProducts: List<Producto>): AIResponse {
        val productCatalog = availableProducts.joinToString("\n") { 
            "- ${it.nombre}: ${it.descripcion} (Categoría: ${it.categoria}, Precio: $${it.precio})" 
        }
        
        val prompt = """
            El usuario busca: "$query"
            
            Catálogo de productos disponibles:
            $productCatalog
            
            Basándote en lo que el usuario busca, recomienda los productos más relevantes del catálogo.
            Explica brevemente por qué cada producto es una buena opción.
            Si no hay productos que coincidan exactamente, sugiere alternativas.
        """.trimIndent()
        
        return sendMessage(prompt)
    }
    
    /**
     * Obtiene información nutricional y tips sobre un producto.
     * 
     * @param producto Producto sobre el que se quiere información
     * @return Información del producto
     */
    suspend fun getProductInfo(producto: Producto): AIResponse {
        val prompt = """
            Dame información útil sobre: ${producto.nombre}
            
            Incluye:
            1. Beneficios nutricionales principales
            2. Tips de conservación para que dure más
            3. Cómo saber si está en buen estado
            4. Ideas de uso en la cocina
            
            Mantén la respuesta concisa y práctica.
        """.trimIndent()
        
        return sendMessage(prompt)
    }
    
    /**
     * Genera una respuesta rápida para preguntas frecuentes.
     */
    suspend fun quickHelp(topic: QuickHelpTopic): AIResponse {
        val prompt = when (topic) {
            QuickHelpTopic.DELIVERY -> "¿Cómo funciona el delivery de Huerto Hogar? Dame información sobre tiempos y zonas de entrega."
            QuickHelpTopic.ORGANIC -> "¿Qué significa que un producto sea orgánico? ¿Cuáles son sus beneficios?"
            QuickHelpTopic.CONSERVATION -> "Dame tips generales para conservar frutas y verduras frescas por más tiempo."
            QuickHelpTopic.SEASONAL -> "¿Cuáles son las frutas y verduras de temporada en Chile actualmente?"
            QuickHelpTopic.PAYMENT -> "¿Qué métodos de pago acepta Huerto Hogar?"
        }
        
        return sendMessage(prompt)
    }
    
    /**
     * Genera un saludo personalizado para el chat.
     */
    suspend fun getGreeting(userName: String?): AIResponse {
        val greeting = if (userName != null) {
            "Saluda al usuario llamado $userName que acaba de abrir el chat del asistente de Huerto Hogar. Sé breve y pregunta en qué puedes ayudarle."
        } else {
            "Saluda al usuario que acaba de abrir el chat del asistente de Huerto Hogar. Sé breve y pregunta en qué puedes ayudarle."
        }
        
        return sendMessage(greeting)
    }
}

/**
 * Respuesta del asistente de IA
 */
sealed class AIResponse {
    data class Success(val message: String) : AIResponse()
    data class Error(val errorMessage: String) : AIResponse()
}

/**
 * Temas de ayuda rápida
 */
enum class QuickHelpTopic {
    DELIVERY,
    ORGANIC,
    CONSERVATION,
    SEASONAL,
    PAYMENT
}
