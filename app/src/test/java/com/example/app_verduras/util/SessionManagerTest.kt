package com.example.app_verduras.util

import com.example.app_verduras.Model.User
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias para SessionManager.
 * 
 * Estas pruebas verifican la correcta gestión de sesión del usuario.
 */
class SessionManagerTest {

    @Before
    fun setUp() {
        // Asegurar estado limpio antes de cada test
        SessionManager.logout()
    }

    @After
    fun tearDown() {
        // Limpiar después de cada test
        SessionManager.logout()
    }

    @Test
    fun `currentUser es null inicialmente`() {
        assertNull(SessionManager.currentUser)
    }

    @Test
    fun `login establece currentUser`() {
        val user = createTestUser()
        
        SessionManager.login(user)
        
        assertNotNull(SessionManager.currentUser)
        assertEquals("test@example.com", SessionManager.currentUser?.email)
    }

    @Test
    fun `logout limpia currentUser`() {
        val user = createTestUser()
        SessionManager.login(user)
        assertNotNull(SessionManager.currentUser)
        
        SessionManager.logout()
        
        assertNull(SessionManager.currentUser)
    }

    @Test
    fun `login reemplaza usuario anterior`() {
        val user1 = createTestUser(email = "user1@example.com", nombre = "Usuario 1")
        val user2 = createTestUser(email = "user2@example.com", nombre = "Usuario 2")
        
        SessionManager.login(user1)
        assertEquals("user1@example.com", SessionManager.currentUser?.email)
        
        SessionManager.login(user2)
        assertEquals("user2@example.com", SessionManager.currentUser?.email)
    }

    @Test
    fun `verificar datos de usuario logueado`() {
        val user = User(
            email = "completo@test.com",
            nombre = "Juan",
            apellido = "Pérez",
            password = "secret123",
            run = "12345678-9",
            direccion = "Av. Principal 123",
            telefono = "+56912345678",
            rol = "ADMIN",
            createdAt = "2024-01-01"
        )
        
        SessionManager.login(user)
        
        val currentUser = SessionManager.currentUser
        assertNotNull(currentUser)
        assertEquals("completo@test.com", currentUser?.email)
        assertEquals("Juan", currentUser?.nombre)
        assertEquals("Pérez", currentUser?.apellido)
        assertEquals("ADMIN", currentUser?.rol)
        assertEquals("12345678-9", currentUser?.run)
    }

    @Test
    fun `usuario USER tiene rol por defecto`() {
        val user = createTestUser()
        
        SessionManager.login(user)
        
        assertEquals("USER", SessionManager.currentUser?.rol)
    }

    @Test
    fun `usuario ADMIN tiene rol admin`() {
        val admin = createTestUser(rol = "ADMIN")
        
        SessionManager.login(admin)
        
        assertEquals("ADMIN", SessionManager.currentUser?.rol)
    }

    @Test
    fun `verificar si hay sesion activa`() {
        assertNull(SessionManager.currentUser) // Sin sesión
        
        SessionManager.login(createTestUser())
        assertNotNull(SessionManager.currentUser) // Con sesión
        
        SessionManager.logout()
        assertNull(SessionManager.currentUser) // Sin sesión nuevamente
    }

    @Test
    fun `multiples logout no causan error`() {
        SessionManager.logout()
        SessionManager.logout()
        SessionManager.logout()
        
        assertNull(SessionManager.currentUser)
    }

    @Test
    fun `login despues de logout funciona`() {
        SessionManager.login(createTestUser(email = "first@test.com"))
        SessionManager.logout()
        SessionManager.login(createTestUser(email = "second@test.com"))
        
        assertEquals("second@test.com", SessionManager.currentUser?.email)
    }

    // ===== Helper Functions =====

    private fun createTestUser(
        email: String = "test@example.com",
        nombre: String = "Test",
        apellido: String = "User",
        rol: String = "USER"
    ): User {
        return User(
            email = email,
            nombre = nombre,
            apellido = apellido,
            password = "password123",
            rol = rol
        )
    }
}
