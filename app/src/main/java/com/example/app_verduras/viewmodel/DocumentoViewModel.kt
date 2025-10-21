package com.example.app_verduras.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Documento
import com.example.app_verduras.repository.DocumentoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DocumentoViewModel(
    private val application: Application,
    private val documentoRepository: DocumentoRepository
) : ViewModel() {

    val documentos: Flow<List<Documento>> = documentoRepository.todosLosDocumentos

    fun addDocumento(uri: Uri, displayName: String) {
        viewModelScope.launch {
            // Copia el archivo al almacenamiento interno de la app
            val internalUri = saveFileToInternalStorage(uri, displayName)
            internalUri?.let {
                val documento = Documento(nombre = displayName, uri = it.toString())
                documentoRepository.insert(documento)
            }
        }
    }

    private fun saveFileToInternalStorage(uri: Uri, displayName: String): Uri? {
        return try {
            val inputStream = application.contentResolver.openInputStream(uri)
            // Crea un archivo en el directorio de archivos interno de la app
            val file = File(application.filesDir, displayName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file) // Devuelve el URI del archivo copiado
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    class Factory(private val application: Application, private val repository: DocumentoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DocumentoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DocumentoViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
