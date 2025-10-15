package com.example.app_verduras.repository

import com.example.app_verduras.Model.User
import com.example.app_verduras.dal.UserDao

class UserRepository(private val userDao: UserDao) {

    // Obtener usuario por email
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    // Insertar un nuevo usuario
    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    // Obtener todos los usuarios (opcional, si luego quieres listarlos)
    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }
}
