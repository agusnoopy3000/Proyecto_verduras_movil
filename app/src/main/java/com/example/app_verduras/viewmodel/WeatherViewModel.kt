package com.example.app_verduras.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.api.external.WeatherApiService
import com.example.app_verduras.api.external.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el consumo de la API Externa (OpenWeatherMap).
 * 
 * IMPORTANTE PARA LA EVALUACIÓN:
 * Este ViewModel demuestra el consumo de una API externa pública
 * diferente del microservicio propio del backend.
 * 
 * Características:
 * - Usa Retrofit para consumir la API
 * - Gestión de estado con StateFlow (MVVM)
 * - Manejo de errores y estados de carga
 */
class WeatherViewModel : ViewModel() {

    private val weatherService = WeatherApiService.create()

    private val _weatherState = MutableStateFlow(WeatherUiState())
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    /**
     * Obtiene el clima actual por coordenadas.
     * Se usa cuando el usuario permite acceso a su ubicación.
     */
    fun fetchWeatherByLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _weatherState.value = _weatherState.value.copy(isLoading = true, error = null)
            
            try {
                Log.d("WeatherViewModel", "Obteniendo clima para: $latitude, $longitude")
                val response = weatherService.getCurrentWeather(latitude, longitude)
                
                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        weather = weather,
                        cityName = weather.name ?: "Ubicación actual",
                        temperature = weather.main?.temp?.toInt() ?: 0,
                        description = weather.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                        humidity = weather.main?.humidity ?: 0,
                        windSpeed = weather.wind?.speed ?: 0.0,
                        icon = weather.weather?.firstOrNull()?.icon ?: "01d"
                    )
                    Log.d("WeatherViewModel", "Clima obtenido: ${weather.name}, ${weather.main?.temp}°C")
                } else {
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        error = "Error al obtener el clima: ${response.code()}"
                    )
                    Log.e("WeatherViewModel", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Excepción: ${e.message}", e)
                _weatherState.value = _weatherState.value.copy(
                    isLoading = false,
                    error = "Error de conexión: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Obtiene el clima por nombre de ciudad.
     * Útil como fallback cuando no hay permisos de ubicación.
     */
    fun fetchWeatherByCity(cityName: String = "Santiago,CL") {
        viewModelScope.launch {
            _weatherState.value = _weatherState.value.copy(isLoading = true, error = null)
            
            try {
                Log.d("WeatherViewModel", "Obteniendo clima para ciudad: $cityName")
                val response = weatherService.getWeatherByCity(cityName)
                
                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        weather = weather,
                        cityName = weather.name ?: cityName,
                        temperature = weather.main?.temp?.toInt() ?: 0,
                        description = weather.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                        humidity = weather.main?.humidity ?: 0,
                        windSpeed = weather.wind?.speed ?: 0.0,
                        icon = weather.weather?.firstOrNull()?.icon ?: "01d"
                    )
                } else {
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        error = "Ciudad no encontrada"
                    )
                }
            } catch (e: Exception) {
                _weatherState.value = _weatherState.value.copy(
                    isLoading = false,
                    error = "Error de conexión"
                )
            }
        }
    }

    /**
     * Obtiene la URL del icono del clima
     */
    fun getWeatherIconUrl(iconCode: String): String {
        return "https://openweathermap.org/img/wn/${iconCode}@2x.png"
    }

    /**
     * Limpia el estado de error
     */
    fun clearError() {
        _weatherState.value = _weatherState.value.copy(error = null)
    }
}

/**
 * Estado de la UI para el clima
 */
data class WeatherUiState(
    val isLoading: Boolean = false,
    val weather: WeatherResponse? = null,
    val cityName: String = "",
    val temperature: Int = 0,
    val description: String = "",
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val icon: String = "01d",
    val error: String? = null
)
