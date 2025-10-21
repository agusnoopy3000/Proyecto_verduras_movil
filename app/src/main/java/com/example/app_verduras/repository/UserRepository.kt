package com.example.app_verduras.repository

import com.example.app_verduras.dal.UserDao
import com.example.app_verduras.Model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    /**
     * Un flujo que emite la lista completa de usuarios cada vez que hay un cambio en la tabla.
     */
    val allUsers: Flow<List<User>> = userDao.getAllUsersFlow()

    /**
     * Función para actualizar un usuario en la base de datos.
     * La implementaremos en el siguiente paso.
     */
    suspend fun update(user: User) {
        userDao.update(user)
    }
}
