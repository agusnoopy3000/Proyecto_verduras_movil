package com.example.app_verduras.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documentos")
data class Documento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val uri: String // Almacenará el URI del archivo copiado en el almacenamiento interno
)
