package com.example.app_verduras.util

import android.util.Log
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.ktx.trace
import com.google.firebase.perf.metrics.HttpMetric
import com.google.firebase.perf.metrics.Trace

/**
 * Manager centralizado para Firebase Performance Monitoring.
 * 
 * Permite medir y monitorear el rendimiento de:
 * - Operaciones personalizadas (traces)
 * - Llamadas de red HTTP
 * - Tiempos de carga de pantallas
 * - Operaciones de base de datos
 * 
 * MÉTRICAS AUTOMÁTICAS (sin código adicional):
 * - Tiempo de inicio de la app
 * - Renderizado de frames
 * - Llamadas de red (automático con OkHttp)
 * 
 * MÉTRICAS PERSONALIZADAS (implementadas aquí):
 * - Tiempo de carga de pantallas
 * - Operaciones de checkout
 * - Búsquedas de productos
 * - Escaneo de QR
 */
object PerformanceManager {
    
    private const val TAG = "PerformanceManager"
    private var isInitialized = false
    
    // Traces activos (para poder detenerlos después)
    private val activeTraces = mutableMapOf<String, Trace>()
    
    /**
     * Inicializa Performance Monitoring.
     * Se inicializa automáticamente, pero este método permite configuraciones adicionales.
     */
    fun initialize() {
        if (isInitialized) return
        
        try {
            // Performance se inicializa automáticamente, pero podemos configurar opciones
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true
            isInitialized = true
            Log.d(TAG, "Firebase Performance Monitoring inicializado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar Performance: ${e.message}")
        }
    }
    
    /**
     * Habilita o deshabilita la recolección de métricas.
     * Útil para dar control al usuario sobre privacidad.
     */
    fun setPerformanceCollectionEnabled(enabled: Boolean) {
        try {
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = enabled
            Log.d(TAG, "Performance collection enabled: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cambiar estado de collection: ${e.message}")
        }
    }
    
    // ==================== TRACES PERSONALIZADOS ====================
    
    /**
     * Inicia un trace personalizado.
     * Usar para medir operaciones específicas.
     * 
     * @param traceName Nombre del trace (debe ser único)
     * @return El trace iniciado o null si hay error
     */
    fun startTrace(traceName: String): Trace? {
        return try {
            val trace = FirebasePerformance.getInstance().newTrace(traceName)
            trace.start()
            activeTraces[traceName] = trace
            Log.d(TAG, "Trace iniciado: $traceName")
            trace
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar trace $traceName: ${e.message}")
            null
        }
    }
    
    /**
     * Detiene un trace y lo envía a Firebase.
     * 
     * @param traceName Nombre del trace a detener
     */
    fun stopTrace(traceName: String) {
        try {
            activeTraces[traceName]?.let { trace ->
                trace.stop()
                activeTraces.remove(traceName)
                Log.d(TAG, "Trace detenido: $traceName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener trace $traceName: ${e.message}")
        }
    }
    
    /**
     * Agrega un atributo a un trace activo.
     */
    fun addTraceAttribute(traceName: String, key: String, value: String) {
        try {
            activeTraces[traceName]?.putAttribute(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Error al agregar atributo a trace: ${e.message}")
        }
    }
    
    /**
     * Incrementa una métrica de un trace activo.
     */
    fun incrementTraceMetric(traceName: String, metricName: String, incrementBy: Long = 1) {
        try {
            activeTraces[traceName]?.incrementMetric(metricName, incrementBy)
        } catch (e: Exception) {
            Log.e(TAG, "Error al incrementar métrica: ${e.message}")
        }
    }
    
    // ==================== TRACES DE PANTALLAS ====================
    
    /**
     * Inicia el trace de carga de una pantalla.
     * Llamar cuando la pantalla comienza a cargar.
     */
    fun startScreenTrace(screenName: String): Trace? {
        val traceName = "screen_load_$screenName"
        return startTrace(traceName)?.also {
            it.putAttribute("screen_name", screenName)
        }
    }
    
    /**
     * Detiene el trace de carga de una pantalla.
     * Llamar cuando la pantalla termina de cargar (datos listos, UI renderizada).
     */
    fun stopScreenTrace(screenName: String) {
        stopTrace("screen_load_$screenName")
    }
    
    // ==================== TRACES DE OPERACIONES ====================
    
    /**
     * Mide el tiempo de una operación de login.
     */
    fun startLoginTrace(): Trace? {
        return startTrace("user_login")
    }
    
    fun stopLoginTrace(success: Boolean) {
        activeTraces["user_login"]?.putAttribute("success", success.toString())
        stopTrace("user_login")
    }
    
    /**
     * Mide el tiempo del proceso de checkout.
     */
    fun startCheckoutTrace(itemCount: Int): Trace? {
        return startTrace("checkout_process")?.also {
            it.putAttribute("item_count", itemCount.toString())
        }
    }
    
    fun stopCheckoutTrace(success: Boolean, orderId: String? = null) {
        activeTraces["checkout_process"]?.apply {
            putAttribute("success", success.toString())
            orderId?.let { putAttribute("order_id", it) }
        }
        stopTrace("checkout_process")
    }
    
    /**
     * Mide el tiempo de búsqueda de productos.
     */
    fun startSearchTrace(query: String): Trace? {
        return startTrace("product_search")?.also {
            it.putAttribute("query_length", query.length.toString())
        }
    }
    
    fun stopSearchTrace(resultsCount: Int) {
        activeTraces["product_search"]?.apply {
            putAttribute("results_count", resultsCount.toString())
            putMetric("results", resultsCount.toLong())
        }
        stopTrace("product_search")
    }
    
    /**
     * Mide el tiempo de escaneo QR.
     */
    fun startQRScanTrace(): Trace? {
        return startTrace("qr_scan")
    }
    
    fun stopQRScanTrace(success: Boolean, productCode: String? = null) {
        activeTraces["qr_scan"]?.apply {
            putAttribute("success", success.toString())
            productCode?.let { putAttribute("product_code", it) }
        }
        stopTrace("qr_scan")
    }
    
    /**
     * Mide el tiempo de carga del catálogo.
     */
    fun startCatalogLoadTrace(): Trace? {
        return startTrace("catalog_load")
    }
    
    fun stopCatalogLoadTrace(productCount: Int, fromCache: Boolean) {
        activeTraces["catalog_load"]?.apply {
            putAttribute("from_cache", fromCache.toString())
            putMetric("product_count", productCount.toLong())
        }
        stopTrace("catalog_load")
    }
    
    /**
     * Mide el tiempo de carga de pedidos.
     */
    fun startOrdersLoadTrace(): Trace? {
        return startTrace("orders_load")
    }
    
    fun stopOrdersLoadTrace(orderCount: Int) {
        activeTraces["orders_load"]?.apply {
            putMetric("order_count", orderCount.toLong())
        }
        stopTrace("orders_load")
    }
    
    // ==================== MÉTRICAS HTTP PERSONALIZADAS ====================
    
    /**
     * Crea una métrica HTTP personalizada.
     * Útil para medir llamadas a APIs externas que no usan OkHttp.
     */
    fun createHttpMetric(url: String, httpMethod: String): HttpMetric? {
        return try {
            FirebasePerformance.getInstance().newHttpMetric(
                url,
                httpMethod
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear HTTP metric: ${e.message}")
            null
        }
    }
    
    /**
     * Registra una llamada HTTP completada.
     */
    fun recordHttpCall(
        url: String,
        method: String,
        responseCode: Int,
        requestPayloadSize: Long = 0,
        responsePayloadSize: Long = 0,
        startTimeMs: Long,
        endTimeMs: Long
    ) {
        try {
            createHttpMetric(url, method)?.apply {
                setHttpResponseCode(responseCode)
                setRequestPayloadSize(requestPayloadSize)
                setResponsePayloadSize(responsePayloadSize)
                // El trace se inicia y detiene inmediatamente con los tiempos registrados
                start()
                stop()
            }
            Log.d(TAG, "HTTP metric registrada: $method $url -> $responseCode")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar HTTP metric: ${e.message}")
        }
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Ejecuta un bloque de código midiendo su tiempo.
     * 
     * Ejemplo:
     * ```
     * val result = PerformanceManager.measureTrace("my_operation") {
     *     // código a medir
     *     doSomething()
     * }
     * ```
     */
    @PublishedApi
    internal fun <T> measureTrace(traceName: String, block: () -> T): T {
        val trace = startTrace(traceName)
        return try {
            block()
        } finally {
            trace?.stop()
            activeTraces.remove(traceName)
        }
    }
    
    /**
     * Mide el tiempo de ejecución de un bloque de código (versión inline).
     */
    inline fun <T> measure(traceName: String, noinline block: () -> T): T {
        return measureTrace(traceName, block)
    }
    
    /**
     * Limpia todos los traces activos.
     * Llamar si hay un error crítico o al cerrar la app.
     */
    fun clearAllTraces() {
        activeTraces.forEach { (name, trace) ->
            try {
                trace.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error al detener trace $name: ${e.message}")
            }
        }
        activeTraces.clear()
        Log.d(TAG, "Todos los traces limpiados")
    }
}
