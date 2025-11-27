package com.example.app_verduras.dal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.app_verduras.Model.Pedido
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pedido: Pedido)

    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    suspend fun getPedidoById(pedidoId: Long): Pedido?

    @Query("SELECT * FROM pedidos WHERE userEmail = :userEmail ORDER BY createdAt DESC")
    suspend fun getPedidosByUserEmail(userEmail: String): List<Pedido>

    // Función que devuelve un Flow para observar cambios en tiempo real.
    @Query("SELECT * FROM pedidos ORDER BY createdAt DESC")
    fun obtenerTodosLosPedidosFlow(): Flow<List<Pedido>>

    @Query("SELECT * FROM pedidos WHERE userEmail = :userEmail ORDER BY createdAt DESC")
    fun getPedidosByUserEmailFlow(userEmail: String): Flow<List<Pedido>>

    @Update
    suspend fun update(pedido: Pedido)

    @Query("UPDATE pedidos SET estado = :nuevoEstado WHERE id = :pedidoId")
    suspend fun updatePedidoStatus(pedidoId: Long, nuevoEstado: String)

    @Delete
    suspend fun delete(pedido: Pedido)

    @Query("DELETE FROM pedidos")
    suspend fun deleteAll()
}
