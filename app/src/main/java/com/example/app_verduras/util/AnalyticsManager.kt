package com.example.app_verduras.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Manager centralizado para Firebase Analytics.
 * 
 * Permite trackear eventos importantes de la aplicación para
 * entender el comportamiento de los usuarios y optimizar la experiencia.
 * 
 * EVENTOS PRINCIPALES:
 * - Autenticación (login, registro, logout)
 * - E-commerce (view_item, add_to_cart, purchase)
 * - Navegación (screen_view)
 * - Búsqueda (search)
 */
object AnalyticsManager {
    
    private const val TAG = "AnalyticsManager"
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var isInitialized = false
    
    /**
     * Inicializa Firebase Analytics.
     * Debe llamarse desde Application o MainActivity.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            firebaseAnalytics = Firebase.analytics
            isInitialized = true
            Log.d(TAG, "Firebase Analytics inicializado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar Firebase Analytics: ${e.message}")
        }
    }
    
    /**
     * Registra una vista de pantalla
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        Log.d(TAG, "Screen view: $screenName")
    }
    
    // ==================== EVENTOS DE AUTENTICACIÓN ====================
    
    /**
     * Registra un evento de login exitoso
     */
    fun logLogin(method: String = "email") {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
        Log.d(TAG, "Login event: method=$method")
    }
    
    /**
     * Registra un evento de registro exitoso
     */
    fun logSignUp(method: String = "email") {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
        Log.d(TAG, "SignUp event: method=$method")
    }
    
    /**
     * Registra un evento de cierre de sesión
     */
    fun logLogout() {
        firebaseAnalytics?.logEvent("logout", null)
        Log.d(TAG, "Logout event")
    }
    
    /**
     * Registra una solicitud de recuperación de contraseña
     */
    fun logPasswordReset() {
        firebaseAnalytics?.logEvent("password_reset_request", null)
        Log.d(TAG, "Password reset request event")
    }
    
    // ==================== EVENTOS DE E-COMMERCE ====================
    
    /**
     * Registra la visualización de un producto
     */
    fun logViewItem(
        itemId: String,
        itemName: String,
        category: String? = null,
        price: Double? = null
    ) {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, itemId)
            param(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            category?.let { param(FirebaseAnalytics.Param.ITEM_CATEGORY, it) }
            price?.let { param(FirebaseAnalytics.Param.PRICE, it) }
            param(FirebaseAnalytics.Param.CURRENCY, "CLP")
        }
        Log.d(TAG, "View item: $itemName ($itemId)")
    }
    
    /**
     * Registra cuando se añade un producto al carrito
     */
    fun logAddToCart(
        itemId: String,
        itemName: String,
        quantity: Int,
        price: Double
    ) {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.ADD_TO_CART) {
            param(FirebaseAnalytics.Param.ITEM_ID, itemId)
            param(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            param(FirebaseAnalytics.Param.QUANTITY, quantity.toLong())
            param(FirebaseAnalytics.Param.PRICE, price)
            param(FirebaseAnalytics.Param.VALUE, price * quantity)
            param(FirebaseAnalytics.Param.CURRENCY, "CLP")
        }
        Log.d(TAG, "Add to cart: $itemName x$quantity")
    }
    
    /**
     * Registra cuando se elimina un producto del carrito
     */
    fun logRemoveFromCart(
        itemId: String,
        itemName: String,
        quantity: Int,
        price: Double
    ) {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.REMOVE_FROM_CART) {
            param(FirebaseAnalytics.Param.ITEM_ID, itemId)
            param(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            param(FirebaseAnalytics.Param.QUANTITY, quantity.toLong())
            param(FirebaseAnalytics.Param.PRICE, price)
            param(FirebaseAnalytics.Param.CURRENCY, "CLP")
        }
        Log.d(TAG, "Remove from cart: $itemName x$quantity")
    }
    
    /**
     * Registra el inicio del proceso de checkout
     */
    fun logBeginCheckout(
        totalValue: Double,
        itemCount: Int
    ) {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT) {
            param(FirebaseAnalytics.Param.VALUE, totalValue)
            param(FirebaseAnalytics.Param.CURRENCY, "CLP")
            param(FirebaseAnalytics.Param.ITEMS, itemCount.toLong())
        }
        Log.d(TAG, "Begin checkout: $itemCount items, total=$totalValue")
    }
    
    /**
     * Registra una compra exitosa
     */
    fun logPurchase(
        orderId: String,
        totalValue: Double,
        itemCount: Int,
        paymentMethod: String? = null
    ) {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.TRANSACTION_ID, orderId)
            param(FirebaseAnalytics.Param.VALUE, totalValue)
            param(FirebaseAnalytics.Param.CURRENCY, "CLP")
            param(FirebaseAnalytics.Param.ITEMS, itemCount.toLong())
            paymentMethod?.let { param(FirebaseAnalytics.Param.PAYMENT_TYPE, it) }
        }
        Log.d(TAG, "Purchase: orderId=$orderId, total=$totalValue")
    }
    
    /**
     * Registra la visualización del carrito
     */
    fun logViewCart(totalValue: Double, itemCount: Int) {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_CART) {
            param(FirebaseAnalytics.Param.VALUE, totalValue)
            param(FirebaseAnalytics.Param.CURRENCY, "CLP")
            param(FirebaseAnalytics.Param.ITEMS, itemCount.toLong())
        }
        Log.d(TAG, "View cart: $itemCount items, total=$totalValue")
    }
    
    // ==================== EVENTOS DE BÚSQUEDA ====================
    
    /**
     * Registra una búsqueda de productos
     */
    fun logSearch(searchTerm: String, resultsCount: Int? = null) {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SEARCH) {
            param(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
            resultsCount?.let { param("results_count", it.toLong()) }
        }
        Log.d(TAG, "Search: '$searchTerm' (results: $resultsCount)")
    }
    
    /**
     * Registra el filtrado por categoría
     */
    fun logSelectCategory(categoryName: String) {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "category")
            param(FirebaseAnalytics.Param.ITEM_ID, categoryName)
        }
        Log.d(TAG, "Select category: $categoryName")
    }
    
    // ==================== EVENTOS DE QR ====================
    
    /**
     * Registra el escaneo de un código QR
     */
    fun logQRScan(productCode: String, success: Boolean) {
        firebaseAnalytics?.logEvent("qr_scan") {
            param("product_code", productCode)
            param("success", if (success) "true" else "false")
        }
        Log.d(TAG, "QR Scan: code=$productCode, success=$success")
    }
    
    // ==================== EVENTOS PERSONALIZADOS ====================
    
    /**
     * Registra un evento personalizado
     */
    fun logCustomEvent(eventName: String, params: Map<String, Any>? = null) {
        val bundle = Bundle()
        params?.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        firebaseAnalytics?.logEvent(eventName, bundle)
        Log.d(TAG, "Custom event: $eventName")
    }
    
    /**
     * Establece el ID del usuario para asociar eventos
     */
    fun setUserId(userId: String?) {
        firebaseAnalytics?.setUserId(userId)
        Log.d(TAG, "User ID set: $userId")
    }
    
    /**
     * Establece propiedades de usuario
     */
    fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics?.setUserProperty(name, value)
        Log.d(TAG, "User property set: $name=$value")
    }
    
    /**
     * Establece el rol del usuario como propiedad
     */
    fun setUserRole(role: String) {
        setUserProperty("user_role", role)
    }
}
