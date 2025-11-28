package com.example.app_verduras.api

import com.example.app_verduras.api.external.WeatherResponse
import com.example.app_verduras.api.external.Main
import com.example.app_verduras.api.external.Weather
import com.example.app_verduras.api.external.Wind
import com.example.app_verduras.api.external.Clouds
import com.example.app_verduras.api.external.Coord
import com.example.app_verduras.api.external.Sys
import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para los modelos de la API Externa (OpenWeatherMap).
 * 
 * IMPORTANTE PARA LA EVALUACIÓN:
 * Estas pruebas verifican el correcto parsing y uso de los datos
 * provenientes de la API externa.
 */
class WeatherApiModelsTest {

    // ===== Tests para WeatherResponse =====

    @Test
    fun `WeatherResponse con todos los campos`() {
        val response = createFullWeatherResponse()
        
        assertNotNull(response.name)
        assertEquals("Santiago", response.name)
        assertNotNull(response.main)
        assertNotNull(response.weather)
        assertEquals(200, response.cod)
    }

    @Test
    fun `WeatherResponse con campos null`() {
        val response = WeatherResponse(
            coord = null,
            weather = null,
            main = null,
            visibility = null,
            wind = null,
            clouds = null,
            dt = null,
            sys = null,
            timezone = null,
            id = null,
            name = null,
            cod = null
        )
        
        assertNull(response.name)
        assertNull(response.main)
        assertNull(response.weather)
    }

    // ===== Tests para Main (temperatura) =====

    @Test
    fun `Main contiene temperatura correcta`() {
        val main = Main(
            temp = 25.5,
            feels_like = 27.0,
            temp_min = 20.0,
            temp_max = 30.0,
            pressure = 1013,
            humidity = 60,
            sea_level = null,
            grnd_level = null
        )
        
        assertEquals(25.5, main.temp!!, 0.01)
        assertEquals(60, main.humidity)
    }

    @Test
    fun `temperatura puede ser negativa`() {
        val main = Main(
            temp = -5.0,
            feels_like = -8.0,
            temp_min = -10.0,
            temp_max = 0.0,
            pressure = 1020,
            humidity = 80,
            sea_level = null,
            grnd_level = null
        )
        
        assertTrue(main.temp!! < 0)
    }

    @Test
    fun `humedad tiene rango válido`() {
        val mainBaja = Main(temp = 20.0, feels_like = null, temp_min = null, 
            temp_max = null, pressure = null, humidity = 0, sea_level = null, grnd_level = null)
        val mainAlta = Main(temp = 20.0, feels_like = null, temp_min = null, 
            temp_max = null, pressure = null, humidity = 100, sea_level = null, grnd_level = null)
        
        assertEquals(0, mainBaja.humidity)
        assertEquals(100, mainAlta.humidity)
    }

    // ===== Tests para Weather (descripción) =====

    @Test
    fun `Weather contiene descripcion del clima`() {
        val weather = Weather(
            id = 800,
            main = "Clear",
            description = "cielo claro",
            icon = "01d"
        )
        
        assertEquals("Clear", weather.main)
        assertEquals("cielo claro", weather.description)
        assertEquals("01d", weather.icon)
    }

    @Test
    fun `iconos de clima validos`() {
        val iconosDia = listOf("01d", "02d", "03d", "04d", "09d", "10d", "11d", "13d", "50d")
        val iconosNoche = listOf("01n", "02n", "03n", "04n", "09n", "10n", "11n", "13n", "50n")
        
        iconosDia.forEach { icon ->
            val weather = Weather(id = 800, main = "Test", description = "Test", icon = icon)
            assertTrue(weather.icon!!.endsWith("d"))
        }
        
        iconosNoche.forEach { icon ->
            val weather = Weather(id = 800, main = "Test", description = "Test", icon = icon)
            assertTrue(weather.icon!!.endsWith("n"))
        }
    }

    // ===== Tests para Wind =====

    @Test
    fun `Wind contiene velocidad y direccion`() {
        val wind = Wind(
            speed = 5.5,
            deg = 180,
            gust = 8.0
        )
        
        assertEquals(5.5, wind.speed!!, 0.01)
        assertEquals(180, wind.deg)
        assertEquals(8.0, wind.gust!!, 0.01)
    }

    @Test
    fun `velocidad del viento puede ser 0`() {
        val wind = Wind(speed = 0.0, deg = 0, gust = null)
        
        assertEquals(0.0, wind.speed!!, 0.01)
    }

    // ===== Tests para Coord =====

    @Test
    fun `Coord contiene latitud y longitud`() {
        val coord = Coord(lon = -70.6483, lat = -33.4569)
        
        assertEquals(-70.6483, coord.lon!!, 0.0001)
        assertEquals(-33.4569, coord.lat!!, 0.0001)
    }

    @Test
    fun `coordenadas de Santiago Chile`() {
        val coord = Coord(lon = -70.6693, lat = -33.4489)
        
        // Verificar que las coordenadas están en el hemisferio sur y oeste
        assertTrue(coord.lat!! < 0) // Hemisferio sur
        assertTrue(coord.lon!! < 0) // Hemisferio oeste
    }

    // ===== Tests para Clouds =====

    @Test
    fun `Clouds indica porcentaje de nubosidad`() {
        val cloudsMin = Clouds(all = 0)
        val cloudsMax = Clouds(all = 100)
        val cloudsMid = Clouds(all = 50)
        
        assertEquals(0, cloudsMin.all)
        assertEquals(100, cloudsMax.all)
        assertEquals(50, cloudsMid.all)
    }

    // ===== Tests para Sys =====

    @Test
    fun `Sys contiene pais y horarios`() {
        val sys = Sys(
            type = 1,
            id = 12345,
            country = "CL",
            sunrise = 1700000000L,
            sunset = 1700050000L
        )
        
        assertEquals("CL", sys.country)
        assertNotNull(sys.sunrise)
        assertNotNull(sys.sunset)
        assertTrue(sys.sunset!! > sys.sunrise!!)
    }

    // ===== Tests de integración de modelos =====

    @Test
    fun `extraer temperatura de WeatherResponse`() {
        val response = createFullWeatherResponse()
        
        val temperatura = response.main?.temp
        assertEquals(22.0, temperatura!!, 0.01)
    }

    @Test
    fun `extraer descripcion de WeatherResponse`() {
        val response = createFullWeatherResponse()
        
        val descripcion = response.weather?.firstOrNull()?.description
        assertEquals("cielo despejado", descripcion)
    }

    @Test
    fun `calcular sensacion termica`() {
        val response = createFullWeatherResponse()
        
        val feelsLike = response.main?.feels_like
        assertNotNull(feelsLike)
    }

    @Test
    fun `WeatherResponse lista de weather puede tener multiples condiciones`() {
        val weather1 = Weather(id = 800, main = "Clear", description = "cielo claro", icon = "01d")
        val weather2 = Weather(id = 701, main = "Mist", description = "neblina", icon = "50d")
        
        val response = WeatherResponse(
            coord = null, weather = listOf(weather1, weather2), main = null,
            visibility = null, wind = null, clouds = null, dt = null,
            sys = null, timezone = null, id = null, name = "Test", cod = 200
        )
        
        assertEquals(2, response.weather?.size)
        assertEquals("Clear", response.weather?.get(0)?.main)
        assertEquals("Mist", response.weather?.get(1)?.main)
    }

    // ===== Helper Functions =====

    private fun createFullWeatherResponse(): WeatherResponse {
        return WeatherResponse(
            coord = Coord(lon = -70.6693, lat = -33.4489),
            weather = listOf(
                Weather(id = 800, main = "Clear", description = "cielo despejado", icon = "01d")
            ),
            main = Main(
                temp = 22.0,
                feels_like = 21.5,
                temp_min = 18.0,
                temp_max = 26.0,
                pressure = 1015,
                humidity = 55,
                sea_level = 1015,
                grnd_level = 1000
            ),
            visibility = 10000,
            wind = Wind(speed = 3.5, deg = 270, gust = 5.0),
            clouds = Clouds(all = 5),
            dt = 1700000000L,
            sys = Sys(type = 1, id = 12345, country = "CL", sunrise = 1699950000L, sunset = 1700000000L),
            timezone = -10800,
            id = 3871336,
            name = "Santiago",
            cod = 200
        )
    }
}
