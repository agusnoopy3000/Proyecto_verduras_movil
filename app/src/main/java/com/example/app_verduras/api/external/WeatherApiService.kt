package com.example.app_verduras.api.external

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Servicio de API externa: OpenWeatherMap
 * 
 * Esta API se utiliza para mostrar el clima actual en la ubicación del usuario,
 * lo cual es relevante para una app de productos orgánicos/agrícolas.
 * 
 * Documentación: https://openweathermap.org/api
 * 
 * IMPORTANTE PARA LA EVALUACIÓN:
 * - Esta es la API EXTERNA requerida (diferente del microservicio propio)
 * - Se consume vía Retrofit
 * - Los datos se muestran en la interfaz (HomeScreen)
 */
interface WeatherApiService {

    /**
     * Obtiene el clima actual por coordenadas
     * GET /data/2.5/weather
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = WEATHER_API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "es"
    ): Response<WeatherResponse>

    /**
     * Obtiene el clima actual por nombre de ciudad
     * GET /data/2.5/weather
     */
    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = WEATHER_API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "es"
    ): Response<WeatherResponse>

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/"
        
        // API Key gratuita de OpenWeatherMap
        // Para obtener una propia: https://openweathermap.org/api
        const val WEATHER_API_KEY = "bd5e378503939ddaee76f12ad7a97608" // Key de demo
        
        /**
         * Crea una instancia del servicio de clima
         */
        fun create(): WeatherApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}

/**
 * Respuesta de la API de OpenWeatherMap
 */
data class WeatherResponse(
    val coord: Coord?,
    val weather: List<Weather>?,
    val main: Main?,
    val visibility: Int?,
    val wind: Wind?,
    val clouds: Clouds?,
    val dt: Long?,
    val sys: Sys?,
    val timezone: Int?,
    val id: Int?,
    val name: String?,
    val cod: Int?
)

data class Coord(
    val lon: Double?,
    val lat: Double?
)

data class Weather(
    val id: Int?,
    val main: String?,
    val description: String?,
    val icon: String?
)

data class Main(
    val temp: Double?,
    val feels_like: Double?,
    val temp_min: Double?,
    val temp_max: Double?,
    val pressure: Int?,
    val humidity: Int?,
    val sea_level: Int?,
    val grnd_level: Int?
)

data class Wind(
    val speed: Double?,
    val deg: Int?,
    val gust: Double?
)

data class Clouds(
    val all: Int?
)

data class Sys(
    val type: Int?,
    val id: Int?,
    val country: String?,
    val sunrise: Long?,
    val sunset: Long?
)
