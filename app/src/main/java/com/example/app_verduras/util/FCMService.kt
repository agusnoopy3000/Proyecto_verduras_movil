package com.example.app_verduras.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.app_verduras.MainActivity
import com.example.app_verduras.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio de Firebase Cloud Messaging para recibir notificaciones push.
 * 
 * TIPOS DE NOTIFICACIONES:
 * - Actualizaciones de estado de pedido
 * - Ofertas y promociones
 * - Alertas de stock
 * - Comunicaciones generales
 */
class FCMService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCMService"
        
        // Canales de notificación
        const val CHANNEL_ORDERS = "orders_channel"
        const val CHANNEL_PROMOTIONS = "promotions_channel"
        const val CHANNEL_GENERAL = "general_channel"
        
        // Keys para datos de notificación
        const val KEY_TYPE = "type"
        const val KEY_ORDER_ID = "order_id"
        const val KEY_ORDER_STATUS = "order_status"
        
        // Tipos de notificación
        const val TYPE_ORDER_UPDATE = "order_update"
        const val TYPE_PROMOTION = "promotion"
        const val TYPE_GENERAL = "general"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    /**
     * Llamado cuando se genera un nuevo token FCM.
     * Este token debe enviarse al backend para asociarlo con el usuario.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM generado: $token")
        
        // Guardar el token localmente
        FCMTokenManager.saveToken(this, token)
        
        // Enviar al backend si el usuario está logueado
        sendTokenToBackend(token)
    }
    
    /**
     * Llamado cuando se recibe un mensaje de FCM.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")
        
        // Verificar si hay datos en el mensaje
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Verificar si hay una notificación en el mensaje
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notificación: ${notification.title} - ${notification.body}")
            showNotification(
                title = notification.title ?: "Huerto Hogar",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }
    
    /**
     * Procesa los datos del mensaje para determinar el tipo y acción.
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data[KEY_TYPE] ?: TYPE_GENERAL
        
        when (type) {
            TYPE_ORDER_UPDATE -> handleOrderUpdate(data)
            TYPE_PROMOTION -> handlePromotion(data)
            else -> handleGeneralMessage(data)
        }
    }
    
    private fun handleOrderUpdate(data: Map<String, String>) {
        val orderId = data[KEY_ORDER_ID] ?: return
        val status = data[KEY_ORDER_STATUS] ?: "actualizado"
        
        val title = "Actualización de pedido"
        val body = when (status.lowercase()) {
            "confirmado" -> "Tu pedido #$orderId ha sido confirmado"
            "en_camino" -> "Tu pedido #$orderId está en camino 🚚"
            "entregado" -> "Tu pedido #$orderId ha sido entregado ✅"
            "cancelado" -> "Tu pedido #$orderId ha sido cancelado"
            else -> "Tu pedido #$orderId ha sido $status"
        }
        
        showNotification(
            title = title,
            body = body,
            data = data,
            channelId = CHANNEL_ORDERS,
            notificationId = orderId.hashCode()
        )
        
        // Log analytics event
        AnalyticsManager.logCustomEvent("order_notification_received", mapOf(
            "order_id" to orderId,
            "status" to status
        ))
    }
    
    private fun handlePromotion(data: Map<String, String>) {
        val title = data["title"] ?: "¡Nueva oferta!"
        val body = data["body"] ?: "Tenemos nuevas ofertas para ti"
        
        showNotification(
            title = title,
            body = body,
            data = data,
            channelId = CHANNEL_PROMOTIONS
        )
        
        AnalyticsManager.logCustomEvent("promotion_notification_received")
    }
    
    private fun handleGeneralMessage(data: Map<String, String>) {
        val title = data["title"] ?: "Huerto Hogar"
        val body = data["body"] ?: ""
        
        if (body.isNotEmpty()) {
            showNotification(
                title = title,
                body = body,
                data = data,
                channelId = CHANNEL_GENERAL
            )
        }
    }
    
    /**
     * Muestra una notificación en el sistema.
     */
    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        channelId: String = CHANNEL_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pasar datos para navegación
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * Crea los canales de notificación (requerido para Android 8.0+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal de pedidos
            val ordersChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "Actualizaciones de pedidos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre el estado de tus pedidos"
                enableVibration(true)
            }
            
            // Canal de promociones
            val promotionsChannel = NotificationChannel(
                CHANNEL_PROMOTIONS,
                "Ofertas y promociones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ofertas especiales y descuentos"
            }
            
            // Canal general
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de la aplicación"
            }
            
            notificationManager.createNotificationChannels(
                listOf(ordersChannel, promotionsChannel, generalChannel)
            )
            
            Log.d(TAG, "Canales de notificación creados")
        }
    }
    
    /**
     * Envía el token FCM al backend para asociarlo con el usuario actual.
     */
    private fun sendTokenToBackend(token: String) {
        // Solo enviar si el usuario está logueado
        if (SessionManager.isLoggedIn()) {
            Log.d(TAG, "Enviando token FCM al backend...")
            // TODO: Implementar llamada al backend cuando tengas el endpoint
            // ApiService.registerFCMToken(token)
        } else {
            Log.d(TAG, "Usuario no logueado, token se enviará después del login")
        }
    }
}

/**
 * Manager para el token FCM.
 * Maneja el almacenamiento y recuperación del token de notificaciones.
 */
object FCMTokenManager {
    private const val PREFS_NAME = "fcm_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"
    
    /**
     * Guarda el token FCM en SharedPreferences
     */
    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
    }
    
    /**
     * Obtiene el token FCM guardado
     */
    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, null)
    }
    
    /**
     * Elimina el token guardado
     */
    fun clearToken(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_FCM_TOKEN)
            .apply()
    }
}
