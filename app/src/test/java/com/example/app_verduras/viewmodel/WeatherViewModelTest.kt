package com.example.app_verduras.viewmodel

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para WeatherUiState.
 * 
 * Este test demuestra las pruebas del estado de UI para la API externa.
 */
class WeatherViewModelTest {

    @Test
    fun `estado inicial tiene valores por defecto`() {
        val state = WeatherUiState()
        
        assertFalse(state.isLoading)
        assertNull(state.weather)
        assertEquals("", state.cityName)
        assertEquals(0, state.temperature)
        assertEquals("", state.description)
        assertEquals(0, state.humidity)
        assertEquals(0.0, state.windSpeed, 0.01)
        assertEquals("01d", state.icon)
        assertNull(state.error)
    }

    @Test
    fun `estado de carga activa`() {
        val state = WeatherUiState(isLoading = true)
        
        assertTrue(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `estado con error`() {
        val state = WeatherUiState(
            isLoading = false,
            error = "Error de conexión"
        )
        
        assertFalse(state.isLoading)
        assertEquals("Error de conexión", state.error)
    }

    @Test
    fun `estado con datos de clima`() {
        val state = WeatherUiState(
            isLoading = false,
            cityName = "Santiago",
            temperature = 25,
            description = "Cielo despejado",
            humidity = 60,
            windSpeed = 3.5,
            icon = "01d"
        )
        
        assertFalse(state.isLoading)
        assertEquals("Santiago", state.cityName)
        assertEquals(25, state.temperature)
        assertEquals("Cielo despejado", state.description)
        assertEquals(60, state.humidity)
        assertEquals(3.5, state.windSpeed, 0.01)
        assertEquals("01d", state.icon)
        assertNull(state.error)
    }

    @Test
    fun `copia de estado con modificaciones`() {
        val original = WeatherUiState(cityName = "Santiago", temperature = 20)
        val modificado = original.copy(temperature = 25, description = "Soleado")
        
        assertEquals("Santiago", original.cityName)
        assertEquals(20, original.temperature)
        assertEquals("", original.description)
        
        assertEquals("Santiago", modificado.cityName)
        assertEquals(25, modificado.temperature)
        assertEquals("Soleado", modificado.description)
    }

    @Test
    fun `verificar iconos de clima válidos`() {
        val iconosValidos = listOf(
            "01d", "01n",  // Cielo despejado
            "02d", "02n",  // Pocas nubes
            "03d", "03n",  // Nubes dispersas
            "04d", "04n",  // Nubes rotas
            "09d", "09n",  // Lluvia
            "10d", "10n",  // Lluvia con sol
            "11d", "11n",  // Tormenta
            "13d", "13n",  // Nieve
            "50d", "50n"   // Niebla
        )
        
        iconosValidos.forEach { icon ->
            val state = WeatherUiState(icon = icon)
            assertEquals(icon, state.icon)
        }
    }

    @Test
    fun `temperatura puede ser negativa`() {
        val state = WeatherUiState(temperature = -5)
        assertEquals(-5, state.temperature)
    }

    @Test
    fun `humedad tiene rango de 0 a 100`() {
        val stateMin = WeatherUiState(humidity = 0)
        val stateMax = WeatherUiState(humidity = 100)
        
        assertEquals(0, stateMin.humidity)
        assertEquals(100, stateMax.humidity)
    }

    @Test
    fun `velocidad del viento puede ser decimal`() {
        val state = WeatherUiState(windSpeed = 15.75)
        assertEquals(15.75, state.windSpeed, 0.001)
    }

    @Test
    fun `estado transición de carga a datos`() {
        var state = WeatherUiState(isLoading = true)
        assertTrue(state.isLoading)
        
        state = state.copy(
            isLoading = false,
            cityName = "Santiago",
            temperature = 22
        )
        
        assertFalse(state.isLoading)
        assertEquals("Santiago", state.cityName)
        assertEquals(22, state.temperature)
    }

    @Test
    fun `estado transición de carga a error`() {
        var state = WeatherUiState(isLoading = true)
        assertTrue(state.isLoading)
        
        state = state.copy(
            isLoading = false,
            error = "Ciudad no encontrada"
        )
        
        assertFalse(state.isLoading)
        assertEquals("Ciudad no encontrada", state.error)
    }
}
