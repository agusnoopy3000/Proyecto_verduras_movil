package com.example.app_verduras.api

import com.example.app_verduras.api.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz del servicio API de Huerto Hogar.
 * Base URL: http://52.2.172.54:8080/api/v1/
 * 
 * Documentación: http://52.2.172.54:8080/swagger-ui/index.html
 */
interface ApiService {

    // ==================== AUTENTICACIÓN ====================

    /**
     * Registro de nuevo usuario (directo al backend)
     * POST /auth/register
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    /**
     * Login de usuario (directo al backend)
     * POST /auth/login
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    /**
     * Sincronización con Firebase - AUTENTICACIÓN HÍBRIDA
     * POST /auth/firebase-sync
     * 
     * Este endpoint recibe el token de Firebase, lo valida, y devuelve un JWT propio del backend.
     * Es el paso final del flujo híbrido de autenticación.
     * 
     * @param request Contiene el firebaseIdToken y datos opcionales del usuario
     * @return AuthResponse con el JWT del backend y datos del usuario
     */
    @POST("auth/firebase-sync")
    suspend fun syncWithFirebase(@Body request: FirebaseSyncRequest): Response<AuthResponse>

    // ==================== PRODUCTOS ====================

    /**
     * Obtener lista de todos los productos
     * GET /products
     * @param search Búsqueda opcional por nombre/descripción
     */
    @GET("products")
    suspend fun getProducts(@Query("q") search: String? = null): Response<List<ProductResponse>>

    /**
     * Obtener producto por ID
     * GET /products/{id}
     */
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<ProductResponse>

    /**
     * Obtener producto por código
     * GET /products/codigo/{codigo}
     */
    @GET("products/codigo/{codigo}")
    suspend fun getProductByCodigo(@Path("codigo") codigo: String): Response<ProductResponse>

    // ==================== PEDIDOS ====================

    /**
     * Crear un nuevo pedido
     * POST /orders
     */
    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>

    /**
     * Obtener mis pedidos
     * GET /orders
     */
    @GET("orders")
    suspend fun getMyOrders(): Response<List<OrderResponse>>

    /**
     * Obtener pedido por ID
     * GET /orders/{id}
     */
    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): Response<OrderResponse>

    // ==================== USUARIO ====================

    /**
     * Obtener mi perfil
     * GET /users/me
     */
    @GET("users/me")
    suspend fun getMyProfile(): Response<UserResponse>
}
