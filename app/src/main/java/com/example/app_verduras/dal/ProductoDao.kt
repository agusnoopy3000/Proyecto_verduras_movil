package com.example.app_verduras.dal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.app_verduras.Model.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    // --- Devuelve un Flow para actualizaciones en vivo
    @Query("SELECT * FROM productos")
    fun getAllProductsFlow(): Flow<List<Producto>>

    // --- Devuelve las categorías únicas
    @Query("SELECT DISTINCT categoria FROM productos")
    suspend fun getDistinctCategorias(): List<String>

    // --- Cuenta el total de productos
    @Query("SELECT COUNT(*) FROM productos")
    suspend fun count(): Int

    // --- Inserta una lista de productos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<Producto>)

    @Query("SELECT * FROM productos")
    suspend fun getAllProducts(): List<Producto>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): Producto?

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun getProductByCode(codigo: String): Producto?

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun getProductByCodigo(codigo: String): Producto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(producto: Producto)

    @Query("DELETE FROM productos")
    suspend fun clearAll()

    @Query("DELETE FROM productos")
    suspend fun deleteAll()

    @Update
    suspend fun update(product: Producto)

    // Búsqueda por nombre o descripción
    @Query("SELECT * FROM productos WHERE nombre LIKE :query OR descripcion LIKE :query")
    suspend fun searchProducts(query: String): List<Producto>
}