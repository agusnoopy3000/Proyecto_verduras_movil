package com.example.app_verduras.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Documento
import com.example.app_verduras.repository.DocumentoRepository
import com.example.app_verduras.util.S3Uploader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentoViewModel(
    private val application: Application,
    private val documentoRepository: DocumentoRepository
) : ViewModel() {

    val documentos: Flow<List<Documento>> = documentoRepository.todosLosDocumentos

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess = _uploadSuccess.asStateFlow()

    // Inyectamos nuestro S3Uploader
    private val s3Uploader = S3Uploader(application)

    fun addDocumento(uri: Uri, displayName: String) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadSuccess.value = false // Reset before starting
            try {
                // 1. Subir el archivo a S3
                val fileUrl = s3Uploader.uploadFile(uri, displayName)

                if (fileUrl != null) {
                    // 2. Si la subida es exitosa, guardar la URL en la base de datos local
                    val documento = Documento(nombre = displayName, uri = fileUrl)
                    documentoRepository.insert(documento)
                    _uploadSuccess.value = true // Set success state
                } else {
                    // Opcional: Manejar el error de subida (mostrar un mensaje, etc.)
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun dismissUploadSuccessDialog() {
        _uploadSuccess.value = false
    }

    // El método saveFileToInternalStorage ya no es necesario, lo hemos reemplazado por la subida a S3.

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
