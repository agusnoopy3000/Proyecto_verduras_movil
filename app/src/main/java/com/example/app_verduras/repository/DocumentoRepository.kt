package com.example.app_verduras.repository

import com.example.app_verduras.Model.Documento
import com.example.app_verduras.dal.DocumentoDao
import kotlinx.coroutines.flow.Flow

class DocumentoRepository(private val documentoDao: DocumentoDao) {

    val todosLosDocumentos: Flow<List<Documento>> = documentoDao.getAllDocumentos()

    suspend fun insert(documento: Documento) {
        documentoDao.insert(documento)
    }
}
