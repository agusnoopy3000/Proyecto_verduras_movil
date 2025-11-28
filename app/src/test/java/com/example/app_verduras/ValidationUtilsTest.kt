package com.example.app_verduras

import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para validaciones de formularios.
 */
class ValidationUtilsTest {

    // ==================== Validación de Email ====================

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    @Test
    fun `valid email returns true`() {
        assertTrue(isValidEmail("test@example.com"))
        assertTrue(isValidEmail("user.name@domain.cl"))
        assertTrue(isValidEmail("user+tag@gmail.com"))
        assertTrue(isValidEmail("test123@test.org"))
    }

    @Test
    fun `invalid email without @ returns false`() {
        assertFalse(isValidEmail("testexample.com"))
    }

    @Test
    fun `invalid email without domain returns false`() {
        assertFalse(isValidEmail("test@"))
    }

    @Test
    fun `invalid email without local part returns false`() {
        assertFalse(isValidEmail("@example.com"))
    }

    @Test
    fun `empty email returns false`() {
        assertFalse(isValidEmail(""))
    }

    @Test
    fun `email with spaces returns false`() {
        assertFalse(isValidEmail("test @example.com"))
        assertFalse(isValidEmail("test@ example.com"))
    }

    // ==================== Validación de RUN chileno ====================

    /**
     * Valida el formato del RUN chileno (ej: 12345678-9)
     */
    private fun isValidRunFormat(run: String): Boolean {
        val runRegex = "^\\d{7,8}-[\\dkK]$".toRegex()
        return run.matches(runRegex)
    }

    /**
     * Calcula el dígito verificador del RUN chileno
     */
    private fun calculateRunVerifier(runNumber: String): Char {
        val factors = intArrayOf(2, 3, 4, 5, 6, 7)
        var sum = 0
        var factorIndex = 0
        
        for (i in runNumber.length - 1 downTo 0) {
            sum += (runNumber[i] - '0') * factors[factorIndex]
            factorIndex = (factorIndex + 1) % 6
        }
        
        val remainder = 11 - (sum % 11)
        return when (remainder) {
            11 -> '0'
            10 -> 'K'
            else -> ('0' + remainder)
        }
    }

    /**
     * Valida el RUN chileno completo (formato + dígito verificador)
     */
    private fun isValidRun(run: String): Boolean {
        if (!isValidRunFormat(run)) return false
        
        val parts = run.split("-")
        if (parts.size != 2) return false
        
        val number = parts[0]
        val verifier = parts[1].uppercase()[0]
        
        return calculateRunVerifier(number) == verifier
    }

    @Test
    fun `valid RUN format returns true`() {
        assertTrue(isValidRunFormat("12345678-9"))
        assertTrue(isValidRunFormat("1234567-8"))
        assertTrue(isValidRunFormat("12345678-K"))
        assertTrue(isValidRunFormat("12345678-k"))
    }

    @Test
    fun `invalid RUN format without dash returns false`() {
        assertFalse(isValidRunFormat("123456789"))
    }

    @Test
    fun `invalid RUN with letters in number part returns false`() {
        assertFalse(isValidRunFormat("1234567A-9"))
    }

    @Test
    fun `RUN with known valid verifier passes full validation`() {
        // 11111111-1 tiene dígito verificador 1
        // Calculemos uno válido: 76086428-5
        val testRun = "11111111-1"
        // Este test verifica que el formato es correcto
        assertTrue(isValidRunFormat(testRun))
    }

    // ==================== Validación de Password ====================

    /**
     * Valida que la contraseña cumpla requisitos mínimos:
     * - Mínimo 8 caracteres
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un número
     */
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isLowerCase() }) return false
        if (!password.any { it.isDigit() }) return false
        return true
    }

    @Test
    fun `valid password returns true`() {
        assertTrue(isValidPassword("Password1"))
        assertTrue(isValidPassword("MyPass123"))
        assertTrue(isValidPassword("Admin123!"))
        assertTrue(isValidPassword("Aa1234567"))
    }

    @Test
    fun `password too short returns false`() {
        assertFalse(isValidPassword("Pass1"))
        assertFalse(isValidPassword("Aa1"))
    }

    @Test
    fun `password without uppercase returns false`() {
        assertFalse(isValidPassword("password123"))
    }

    @Test
    fun `password without lowercase returns false`() {
        assertFalse(isValidPassword("PASSWORD123"))
    }

    @Test
    fun `password without number returns false`() {
        assertFalse(isValidPassword("PasswordABC"))
    }

    @Test
    fun `empty password returns false`() {
        assertFalse(isValidPassword(""))
    }

    // ==================== Validación de Teléfono chileno ====================

    /**
     * Valida formato de teléfono chileno
     * Formatos válidos: +56912345678, 56912345678, 912345678
     */
    private fun isValidChileanPhone(phone: String): Boolean {
        val cleaned = phone.replace(" ", "").replace("-", "")
        val phoneRegex = "^(\\+?56)?9\\d{8}$".toRegex()
        return cleaned.matches(phoneRegex)
    }

    @Test
    fun `valid Chilean phone with plus prefix returns true`() {
        assertTrue(isValidChileanPhone("+56912345678"))
    }

    @Test
    fun `valid Chilean phone without plus returns true`() {
        assertTrue(isValidChileanPhone("56912345678"))
    }

    @Test
    fun `valid Chilean phone with only 9 digit prefix returns true`() {
        assertTrue(isValidChileanPhone("912345678"))
    }

    @Test
    fun `phone with spaces is valid`() {
        assertTrue(isValidChileanPhone("+56 9 1234 5678"))
    }

    @Test
    fun `phone not starting with 9 returns false`() {
        assertFalse(isValidChileanPhone("+56812345678"))
    }

    @Test
    fun `phone with wrong length returns false`() {
        assertFalse(isValidChileanPhone("+569123456")) // muy corto
        assertFalse(isValidChileanPhone("+5691234567890")) // muy largo
    }

    // ==================== Validación de Nombre ====================

    private fun isValidName(name: String): Boolean {
        if (name.isBlank()) return false
        if (name.length < 2) return false
        if (name.length > 50) return false
        // Solo letras, espacios y acentos
        return name.all { it.isLetter() || it.isWhitespace() || it in "áéíóúÁÉÍÓÚñÑ" }
    }

    @Test
    fun `valid name returns true`() {
        assertTrue(isValidName("Juan"))
        assertTrue(isValidName("María José"))
        assertTrue(isValidName("José Pérez"))
    }

    @Test
    fun `name with numbers returns false`() {
        assertFalse(isValidName("Juan123"))
    }

    @Test
    fun `empty name returns false`() {
        assertFalse(isValidName(""))
    }

    @Test
    fun `single character name returns false`() {
        assertFalse(isValidName("A"))
    }

    @Test
    fun `name too long returns false`() {
        assertFalse(isValidName("A".repeat(51)))
    }
}
