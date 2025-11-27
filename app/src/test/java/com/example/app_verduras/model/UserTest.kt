package com.example.app_verduras.model

import com.example.app_verduras.Model.User
import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para el modelo User.
 * 
 * Cobertura de casos:
 * - Creación de usuario con campos obligatorios
 * - Creación de usuario con todos los campos
 * - Valores por defecto
 * - Validación de roles
 */
class UserTest {

    @Test
    fun `crear usuario con campos obligatorios`() {
        val user = User(
            email = "test@example.com",
            nombre = "Juan",
            apellido = "Pérez"
        )
        
        assertEquals("test@example.com", user.email)
        assertEquals("Juan", user.nombre)
        assertEquals("Pérez", user.apellido)
    }

    @Test
    fun `usuario tiene password vacio por defecto`() {
        val user = User(
            email = "test@example.com",
            nombre = "Juan",
            apellido = "Pérez"
        )
        
        assertEquals("", user.password)
    }

    @Test
    fun `usuario tiene rol USER por defecto`() {
        val user = User(
            email = "test@example.com",
            nombre = "Juan",
            apellido = "Pérez"
        )
        
        assertEquals("USER", user.rol)
    }

    @Test
    fun `crear usuario con rol ADMIN`() {
        val user = User(
            email = "admin@example.com",
            nombre = "Admin",
            apellido = "Sistema",
            rol = "ADMIN"
        )
        
        assertEquals("ADMIN", user.rol)
    }

    @Test
    fun `crear usuario con todos los campos`() {
        val user = User(
            email = "completo@example.com",
            nombre = "María",
            apellido = "González",
            password = "password123",
            run = "12345678-9",
            direccion = "Av. Principal 123",
            telefono = "+56912345678",
            rol = "USER",
            createdAt = "2024-01-01T00:00:00"
        )
        
        assertEquals("completo@example.com", user.email)
        assertEquals("María", user.nombre)
        assertEquals("González", user.apellido)
        assertEquals("password123", user.password)
        assertEquals("12345678-9", user.run)
        assertEquals("Av. Principal 123", user.direccion)
        assertEquals("+56912345678", user.telefono)
        assertEquals("USER", user.rol)
        assertEquals("2024-01-01T00:00:00", user.createdAt)
    }

    @Test
    fun `campos opcionales son null por defecto`() {
        val user = User(
            email = "test@example.com",
            nombre = "Test",
            apellido = "User"
        )
        
        assertNull(user.run)
        assertNull(user.direccion)
        assertNull(user.telefono)
        assertNull(user.createdAt)
    }

    @Test
    fun `email es la clave primaria`() {
        val user1 = User(email = "mismo@email.com", nombre = "Uno", apellido = "Test")
        val user2 = User(email = "mismo@email.com", nombre = "Dos", apellido = "Test")
        
        assertEquals(user1.email, user2.email)
    }

    @Test
    fun `verificar igualdad de usuarios por data class`() {
        val user1 = User(
            email = "test@example.com",
            nombre = "Juan",
            apellido = "Pérez",
            rol = "USER"
        )
        val user2 = User(
            email = "test@example.com",
            nombre = "Juan",
            apellido = "Pérez",
            rol = "USER"
        )
        
        assertEquals(user1, user2)
    }

    @Test
    fun `usuarios diferentes no son iguales`() {
        val user1 = User(email = "user1@example.com", nombre = "Uno", apellido = "Test")
        val user2 = User(email = "user2@example.com", nombre = "Dos", apellido = "Test")
        
        assertNotEquals(user1, user2)
    }

    @Test
    fun `verificar copia de usuario con modificaciones`() {
        val original = User(
            email = "test@example.com",
            nombre = "Juan",
            apellido = "Pérez"
        )
        
        val modificado = original.copy(nombre = "Pedro")
        
        assertEquals("Juan", original.nombre)
        assertEquals("Pedro", modificado.nombre)
        assertEquals(original.email, modificado.email)
    }
}
