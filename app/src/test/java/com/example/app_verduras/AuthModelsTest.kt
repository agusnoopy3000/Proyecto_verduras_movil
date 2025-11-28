package com.example.app_verduras

import com.example.app_verduras.api.models.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para los modelos de autenticación.
 */
class AuthModelsTest {

    @Test
    fun `RegisterRequest creates correct object with all fields`() {
        val request = RegisterRequest(
            run = "12345678-9",
            nombre = "Juan",
            apellidos = "Pérez García",
            email = "juan@test.com",
            password = "Password123!",
            direccion = "Av. Principal 123",
            telefono = "+56912345678"
        )

        assertEquals("12345678-9", request.run)
        assertEquals("Juan", request.nombre)
        assertEquals("Pérez García", request.apellidos)
        assertEquals("juan@test.com", request.email)
        assertEquals("Password123!", request.password)
        assertEquals("Av. Principal 123", request.direccion)
        assertEquals("+56912345678", request.telefono)
    }

    @Test
    fun `RegisterRequest allows null optional fields`() {
        val request = RegisterRequest(
            run = "12345678-9",
            nombre = "Juan",
            apellidos = "Pérez",
            email = "juan@test.com",
            password = "Password123!"
        )

        assertNull(request.direccion)
        assertNull(request.telefono)
    }

    @Test
    fun `LoginRequest creates correct object`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "myPassword123"
        )

        assertEquals("test@example.com", request.email)
        assertEquals("myPassword123", request.password)
    }

    @Test
    fun `FirebaseSyncRequest creates correct object with token only`() {
        val request = FirebaseSyncRequest(
            firebaseIdToken = "firebase-token-123"
        )

        assertEquals("firebase-token-123", request.firebaseIdToken)
        assertNull(request.run)
        assertNull(request.nombre)
        assertNull(request.apellidos)
    }

    @Test
    fun `FirebaseSyncRequest creates correct object with all fields`() {
        val request = FirebaseSyncRequest(
            firebaseIdToken = "firebase-token-123",
            run = "12345678-9",
            nombre = "María",
            apellidos = "López",
            direccion = "Calle 123",
            telefono = "+56998765432"
        )

        assertEquals("firebase-token-123", request.firebaseIdToken)
        assertEquals("12345678-9", request.run)
        assertEquals("María", request.nombre)
        assertEquals("López", request.apellidos)
        assertEquals("Calle 123", request.direccion)
        assertEquals("+56998765432", request.telefono)
    }

    @Test
    fun `UserResponse stores all user data`() {
        val user = UserResponse(
            email = "user@test.com",
            nombre = "Carlos",
            apellido = "Soto",
            run = "11111111-1",
            direccion = "Av. Las Condes 500",
            telefono = "+56911111111",
            rol = "CLIENTE",
            createdAt = "2025-01-01T00:00:00"
        )

        assertEquals("user@test.com", user.email)
        assertEquals("Carlos", user.nombre)
        assertEquals("Soto", user.apellido)
        assertEquals("11111111-1", user.run)
        assertEquals("CLIENTE", user.rol)
    }

    @Test
    fun `AuthResponse contains token and user`() {
        val user = UserResponse(
            email = "admin@test.com",
            nombre = "Admin",
            apellido = "User",
            run = null,
            direccion = null,
            telefono = null,
            rol = "ADMIN",
            createdAt = null
        )
        
        val response = AuthResponse(
            token = "jwt-token-xyz",
            user = user
        )

        assertEquals("jwt-token-xyz", response.token)
        assertEquals("admin@test.com", response.user.email)
        assertEquals("ADMIN", response.user.rol)
    }
}
