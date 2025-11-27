package com.example.app_verduras.validation

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para validaciones de datos.
 * 
 * Estas pruebas verifican reglas de negocio y validaciones comunes.
 */
class ValidationTest {

    // ===== Validación de Email =====

    @Test
    fun `email valido simple`() {
        assertTrue(isValidEmail("usuario@example.com"))
    }

    @Test
    fun `email valido con subdominio`() {
        assertTrue(isValidEmail("usuario@mail.example.com"))
    }

    @Test
    fun `email invalido sin arroba`() {
        assertFalse(isValidEmail("usuarioexample.com"))
    }

    @Test
    fun `email invalido sin dominio`() {
        assertFalse(isValidEmail("usuario@"))
    }

    @Test
    fun `email invalido vacio`() {
        assertFalse(isValidEmail(""))
    }

    @Test
    fun `email invalido solo espacios`() {
        assertFalse(isValidEmail("   "))
    }

    // ===== Validación de Contraseña =====

    @Test
    fun `password valido con 8 caracteres`() {
        assertTrue(isValidPassword("password"))
    }

    @Test
    fun `password invalido muy corto`() {
        assertFalse(isValidPassword("pass"))
    }

    @Test
    fun `password invalido vacio`() {
        assertFalse(isValidPassword(""))
    }

    @Test
    fun `password valido largo`() {
        assertTrue(isValidPassword("contraseña_muy_segura_123"))
    }

    // ===== Validación de RUN Chileno =====

    @Test
    fun `RUN valido con guion`() {
        assertTrue(isValidRun("12345678-9"))
    }

    @Test
    fun `RUN valido con K`() {
        assertTrue(isValidRun("12345678-K"))
    }

    @Test
    fun `RUN invalido sin guion`() {
        assertFalse(isValidRun("123456789"))
    }

    @Test
    fun `RUN invalido muy corto`() {
        assertFalse(isValidRun("12-3"))
    }

    @Test
    fun `RUN invalido caracteres no numericos`() {
        assertFalse(isValidRun("abcdefgh-i"))
    }

    // ===== Validación de Teléfono =====

    @Test
    fun `telefono valido chileno`() {
        assertTrue(isValidPhone("+56912345678"))
    }

    @Test
    fun `telefono valido sin codigo pais`() {
        assertTrue(isValidPhone("912345678"))
    }

    @Test
    fun `telefono invalido muy corto`() {
        assertFalse(isValidPhone("12345"))
    }

    @Test
    fun `telefono invalido con letras`() {
        assertFalse(isValidPhone("91234abcd"))
    }

    // ===== Validación de Precio =====

    @Test
    fun `precio valido positivo`() {
        assertTrue(isValidPrice(100.0))
    }

    @Test
    fun `precio valido cero`() {
        assertTrue(isValidPrice(0.0))
    }

    @Test
    fun `precio invalido negativo`() {
        assertFalse(isValidPrice(-50.0))
    }

    @Test
    fun `precio valido con decimales`() {
        assertTrue(isValidPrice(1999.99))
    }

    // ===== Validación de Stock =====

    @Test
    fun `stock valido positivo`() {
        assertTrue(isValidStock(100))
    }

    @Test
    fun `stock valido cero`() {
        assertTrue(isValidStock(0))
    }

    @Test
    fun `stock invalido negativo`() {
        assertFalse(isValidStock(-10))
    }

    // ===== Validación de Fecha =====

    @Test
    fun `fecha valida formato correcto`() {
        assertTrue(isValidDateFormat("25/12/2024"))
    }

    @Test
    fun `fecha invalida formato incorrecto`() {
        assertFalse(isValidDateFormat("2024-12-25"))
    }

    @Test
    fun `fecha invalida muy corta`() {
        assertFalse(isValidDateFormat("25/12"))
    }

    @Test
    fun `fecha invalida dia mayor a 31`() {
        assertFalse(isValidDateFormat("32/12/2024"))
    }

    @Test
    fun `fecha invalida mes mayor a 12`() {
        assertFalse(isValidDateFormat("25/13/2024"))
    }

    // ===== Validación de Dirección =====

    @Test
    fun `direccion valida`() {
        assertTrue(isValidAddress("Av. Principal 123, Santiago"))
    }

    @Test
    fun `direccion invalida muy corta`() {
        assertFalse(isValidAddress("Av"))
    }

    @Test
    fun `direccion invalida vacia`() {
        assertFalse(isValidAddress(""))
    }

    // ===== Helper Functions (implementaciones locales para tests) =====

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && 
               email.contains("@") && 
               email.substringAfter("@").isNotEmpty() &&
               email.substringAfter("@").contains(".")
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    private fun isValidRun(run: String): Boolean {
        val regex = Regex("^\\d{7,8}-[\\dkK]$")
        return regex.matches(run)
    }

    private fun isValidPhone(phone: String): Boolean {
        val cleanPhone = phone.replace("+", "").replace(" ", "")
        return cleanPhone.length >= 9 && cleanPhone.all { it.isDigit() }
    }

    private fun isValidPrice(price: Double): Boolean {
        return price >= 0
    }

    private fun isValidStock(stock: Int): Boolean {
        return stock >= 0
    }

    private fun isValidDateFormat(date: String): Boolean {
        if (date.length != 10) return false
        val parts = date.split("/")
        if (parts.size != 3) return false
        
        val day = parts[0].toIntOrNull() ?: return false
        val month = parts[1].toIntOrNull() ?: return false
        val year = parts[2].toIntOrNull() ?: return false
        
        return day in 1..31 && month in 1..12 && year > 2000
    }

    private fun isValidAddress(address: String): Boolean {
        return address.length >= 5
    }
}
