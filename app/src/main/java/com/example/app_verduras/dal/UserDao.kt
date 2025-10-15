package com.example.app_verduras.dal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.app_verduras.Model.User

@Dao
interface UserDao {
    // --- MEJORA: Nombre de función consistente con AuthViewModel ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // --- MEJORA: Estrategia de conflicto explícita ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}
