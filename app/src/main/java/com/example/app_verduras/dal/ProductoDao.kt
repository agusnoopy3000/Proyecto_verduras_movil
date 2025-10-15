package com.example.app_verduras.dal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.app_verduras.Model.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    // --- NUEVA --- Devuelve un Flow para actualizaciones en vivo
    @Query("SELECT * FROM productos")
    fun getAllProductsFlow(): Flow<List<Producto>>

    // --- NUEVA --- Cuenta el total de productos
    @Query("SELECT COUNT(*) FROM productos")
    suspend fun count(): Int

    // --- NUEVA --- Inserta una lista de productos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<Producto>)

    @Query("SELECT * FROM productos")
    suspend fun getAllProducts(): List<Producto>

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun getProductByCode(codigo: String): Producto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(producto: Producto)

    @Query("DELETE FROM productos")
    suspend fun clearAll()
}