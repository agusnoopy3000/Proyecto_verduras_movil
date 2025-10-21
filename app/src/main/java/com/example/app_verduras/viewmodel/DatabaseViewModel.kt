package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.repository.ProductoRepository
import kotlinx.coroutines.launch

// Este ViewModel se encarga de inicializar la base de datos de forma segura.
class DatabaseViewModel(private val productoRepository: ProductoRepository) : ViewModel() {

    /**
     * Llama a la lógica de inicialización del repositorio dentro de una corrutina.
     * Esto asegura que la operación de I/O no bloquee el hilo principal.
     */
    fun initializeDatabase() {
        viewModelScope.launch {
            productoRepository.initializeDatabaseIfNeeded()
        }
    }

    // Factory para crear una instancia de DatabaseViewModel con sus dependencias.
    class Factory(private val repository: ProductoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DatabaseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
