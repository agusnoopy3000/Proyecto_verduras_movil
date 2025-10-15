package com.example.app_verduras.dal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.app_verduras.Model.Pedido

@Dao
interface PedidoDao {

    // --- CREATE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pedido: Pedido)

    // --- READ ---
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    suspend fun getPedidoById(pedidoId: Int): Pedido?

    @Query("SELECT * FROM pedidos WHERE userEmail = :userEmail ORDER BY fechaEntrega DESC")
    suspend fun getPedidosByUser(userEmail: String): List<Pedido>

    @Query("SELECT * FROM pedidos ORDER BY fechaEntrega DESC")
    suspend fun getAllPedidos(): List<Pedido>

    // --- UPDATE ---
    @Update
    suspend fun update(pedido: Pedido)

    @Query("UPDATE pedidos SET estado = :nuevoEstado WHERE id = :pedidoId")
    suspend fun updateEstado(pedidoId: Int, nuevoEstado: String)

    // --- DELETE ---
    @Delete
    suspend fun delete(pedido: Pedido)
}
