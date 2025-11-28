package com.example.app_verduras

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.*

/**
 * Pruebas unitarias para el cálculo de envío y distancias.
 * Estas funciones son réplicas de las usadas en PedidoScreenEnhanced.kt
 */
class ShippingCalculatorTest {

    companion object {
        private const val BASE_DELIVERY_COST = 2500.0
        private const val COST_PER_KM = 350.0
        private const val STORE_LAT = -33.4489 // Santiago centro
        private const val STORE_LON = -70.6693
        private const val EARTH_RADIUS_KM = 6371.0
    }

    /**
     * Fórmula de Haversine para calcular distancia entre dos puntos geográficos
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Calcula el costo de envío basado en la distancia
     */
    private fun calculateShippingCost(distanceKm: Double): Double {
        return BASE_DELIVERY_COST + (distanceKm * COST_PER_KM)
    }

    // ==================== Tests de distancia ====================

    @Test
    fun `distance between same point is zero`() {
        val distance = calculateDistance(STORE_LAT, STORE_LON, STORE_LAT, STORE_LON)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `distance Santiago to Providencia is approximately correct`() {
        // Providencia aproximadamente
        val providenciaLat = -33.4280
        val providenciaLon = -70.6100

        val distance = calculateDistance(STORE_LAT, STORE_LON, providenciaLat, providenciaLon)
        
        // La distancia debería ser aproximadamente 5-6 km
        assertTrue("Distancia esperada entre 4 y 8 km, obtenida: $distance", distance in 4.0..8.0)
    }

    @Test
    fun `distance Santiago to Las Condes is approximately correct`() {
        // Las Condes aproximadamente
        val lasCondesLat = -33.4180
        val lasCondesLon = -70.5800

        val distance = calculateDistance(STORE_LAT, STORE_LON, lasCondesLat, lasCondesLon)
        
        // La distancia debería ser aproximadamente 8-12 km
        assertTrue("Distancia esperada entre 7 y 12 km, obtenida: $distance", distance in 7.0..12.0)
    }

    @Test
    fun `distance is symmetric`() {
        val pointALat = -33.40
        val pointALon = -70.60
        val pointBLat = -33.50
        val pointBLon = -70.70

        val distanceAB = calculateDistance(pointALat, pointALon, pointBLat, pointBLon)
        val distanceBA = calculateDistance(pointBLat, pointBLon, pointALat, pointALon)

        assertEquals(distanceAB, distanceBA, 0.001)
    }

    @Test
    fun `distance is always positive`() {
        val distance = calculateDistance(-33.5, -70.7, -33.4, -70.5)
        assertTrue("La distancia debe ser positiva", distance >= 0)
    }

    @Test
    fun `long distance calculation is reasonable`() {
        // Santiago a Valparaíso (aprox 100km)
        val valparaisoLat = -33.0472
        val valparaisoLon = -71.6127

        val distance = calculateDistance(STORE_LAT, STORE_LON, valparaisoLat, valparaisoLon)
        
        // La distancia debería ser aproximadamente 90-120 km
        assertTrue("Distancia a Valparaíso esperada entre 80 y 130 km, obtenida: $distance", 
                   distance in 80.0..130.0)
    }

    // ==================== Tests de costo de envío ====================

    @Test
    fun `shipping cost for zero distance is base cost`() {
        val cost = calculateShippingCost(0.0)
        assertEquals(BASE_DELIVERY_COST, cost, 0.001)
    }

    @Test
    fun `shipping cost for 1km is base plus per km rate`() {
        val cost = calculateShippingCost(1.0)
        assertEquals(BASE_DELIVERY_COST + COST_PER_KM, cost, 0.001)
    }

    @Test
    fun `shipping cost for 5km is calculated correctly`() {
        val cost = calculateShippingCost(5.0)
        val expected = BASE_DELIVERY_COST + (5 * COST_PER_KM)
        assertEquals(expected, cost, 0.001)
    }

    @Test
    fun `shipping cost for 10km is calculated correctly`() {
        val cost = calculateShippingCost(10.0)
        val expected = BASE_DELIVERY_COST + (10 * COST_PER_KM) // 2500 + 3500 = 6000
        assertEquals(6000.0, cost, 0.001)
    }

    @Test
    fun `shipping cost increases linearly with distance`() {
        val cost5km = calculateShippingCost(5.0)
        val cost10km = calculateShippingCost(10.0)
        val cost15km = calculateShippingCost(15.0)

        val diff1 = cost10km - cost5km
        val diff2 = cost15km - cost10km

        assertEquals(diff1, diff2, 0.001)
    }

    @Test
    fun `shipping cost is always positive`() {
        val cost = calculateShippingCost(100.0)
        assertTrue("El costo de envío debe ser positivo", cost > 0)
    }

    // ==================== Tests de integración distancia + costo ====================

    @Test
    fun `full calculation from coordinates to shipping cost`() {
        // Simular pedido desde Providencia
        val providenciaLat = -33.4280
        val providenciaLon = -70.6100

        val distance = calculateDistance(STORE_LAT, STORE_LON, providenciaLat, providenciaLon)
        val cost = calculateShippingCost(distance)

        // Verificar que el costo es razonable (entre 4000 y 6000 CLP)
        assertTrue("Costo esperado entre 4000 y 6000, obtenido: $cost", cost in 4000.0..6000.0)
    }

    @Test
    fun `nearby delivery is cheaper than far delivery`() {
        // Cerca (2km)
        val nearLat = -33.4550
        val nearLon = -70.6600
        
        // Lejos (15km)
        val farLat = -33.5500
        val farLon = -70.5500

        val nearDistance = calculateDistance(STORE_LAT, STORE_LON, nearLat, nearLon)
        val farDistance = calculateDistance(STORE_LAT, STORE_LON, farLat, farLon)

        val nearCost = calculateShippingCost(nearDistance)
        val farCost = calculateShippingCost(farDistance)

        assertTrue("El envío cercano debe ser más barato que el lejano", nearCost < farCost)
    }
}
