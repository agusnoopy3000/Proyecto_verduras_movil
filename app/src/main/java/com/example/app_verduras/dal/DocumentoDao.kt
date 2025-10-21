package com.example.app_verduras.dal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.app_verduras.Model.Documento
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(documento: Documento)

    @Query("SELECT * FROM documentos ORDER BY id DESC")
    fun getAllDocumentos(): Flow<List<Documento>>

}
