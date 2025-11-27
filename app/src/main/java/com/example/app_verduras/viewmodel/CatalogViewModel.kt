package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CatalogState(
    val filteredProducts: List<Producto> = emptyList(),
    val categories: List<String> = emptyList(),
    val search: String = "",
    val selectedCategory: String? = null
)

class CatalogViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _search = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    // El estado ahora combina 3 flujos: productos del repo, búsqueda y categoría.
    val uiState: StateFlow<CatalogState> = combine(
        repository.getAll(), // Escucha el Flow de productos desde la base de datos
        _search,
        _selectedCategory
    ) { products, searchQuery, category ->
        val categories = products.mapNotNull { it.categoria }.distinct().sorted()
        val filteredProducts = products.filter { p ->
            (category == null || p.categoria == category) &&
                    (searchQuery.isEmpty() || p.nombre.contains(searchQuery, ignoreCase = true))
        }

        CatalogState(
            filteredProducts = filteredProducts,
            categories = categories,
            search = searchQuery,
            selectedCategory = category
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogState()
    )

    fun updateCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun updateSearch(query: String) {
        _search.value = query
    }

    // Factory para que el sistema sepa cómo crear el ViewModel con el repositorio
    class Factory(private val repository: ProductoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CatalogViewModel::class.java)) {
                return CatalogViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}