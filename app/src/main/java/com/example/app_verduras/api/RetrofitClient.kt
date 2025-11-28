package com.example.app_verduras.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Gestor de tokens JWT para la autenticación.
 * Almacena el token en SharedPreferences de forma segura.
 */
object TokenManager {
    private const val PREF_NAME = "huerto_hogar_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_APELLIDO = "user_apellido"
    private const val KEY_USER_RUN = "user_run"
    private const val KEY_USER_DIRECCION = "user_direccion"
    private const val KEY_USER_TELEFONO = "user_telefono"
    private const val KEY_USER_ROL = "user_rol"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveToken(token: String) {
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun getToken(): String? = prefs?.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    fun saveUserData(
        email: String,
        nombre: String,
        apellido: String,
        run: String?,
        direccion: String?,
        telefono: String?,
        rol: String
    ) {
        prefs?.edit()?.apply {
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, nombre)
            putString(KEY_USER_APELLIDO, apellido)
            putString(KEY_USER_RUN, run)
            putString(KEY_USER_DIRECCION, direccion)
            putString(KEY_USER_TELEFONO, telefono)
            putString(KEY_USER_ROL, rol)
            apply()
        }
    }

    fun getUserEmail(): String? = prefs?.getString(KEY_USER_EMAIL, null)
    fun getUserName(): String? = prefs?.getString(KEY_USER_NAME, null)
    fun getUserApellido(): String? = prefs?.getString(KEY_USER_APELLIDO, null)
    fun getUserRun(): String? = prefs?.getString(KEY_USER_RUN, null)
    fun getUserDireccion(): String? = prefs?.getString(KEY_USER_DIRECCION, null)
    fun getUserTelefono(): String? = prefs?.getString(KEY_USER_TELEFONO, null)
    fun getUserRol(): String? = prefs?.getString(KEY_USER_ROL, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
    }
}

/**
 * Cliente Retrofit configurado para Huerto Hogar API.
 * 
 * API Base URL: http://52.2.172.54:8080/api/v1/
 * Swagger: http://52.2.172.54:8080/swagger-ui/index.html
 */
object RetrofitClient {

    // URL base de la API de Huerto Hogar
    private const val BASE_URL = "http://52.2.172.54:8080/api/v1/"

    // Interceptor para logging de requests/responses (útil para debugging)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente OkHttp con interceptor de autenticación JWT
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                
                // Obtener el token JWT guardado
                val token = TokenManager.getToken()
                
                // Si hay token, añadir el header Authorization
                val request = if (token != null) {
                    originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/json")
                        .build()
                } else {
                    originalRequest.newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .build()
                }
                
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Instancia de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API Service listo para usar
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
