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
    val selectedCategory: String? = null,
    val suggestedProducts: List<Producto> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = true // Nuevo: para mostrar shimmer
)

class CatalogViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _search = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())

    // El estado ahora combina 3 flujos: productos del repo, búsqueda y categoría.
    val uiState: StateFlow<CatalogState> = combine(
        repository.getAll(), // Escucha el Flow de productos desde la base de datos
        _search,
        _selectedCategory,
        _recentSearches
    ) { products, searchQuery, category, recentSearches ->
        val categories = products.mapNotNull { it.categoria }.distinct().sorted()
        val filteredProducts = products.filter { p ->
            (category == null || p.categoria == category) &&
                    (searchQuery.isEmpty() || p.nombre.contains(searchQuery, ignoreCase = true) ||
                     p.descripcion?.contains(searchQuery, ignoreCase = true) == true)
        }
        
        // Productos sugeridos: mostrar productos de otras categorías o populares
        val suggestedProducts = if (searchQuery.isEmpty() && category == null) {
            products.shuffled().take(4)
        } else if (category != null) {
            // Si hay categoría seleccionada, sugerir de otras categorías
            products.filter { it.categoria != category }.shuffled().take(4)
        } else {
            // Si está buscando, sugerir productos similares
            products.filter { p ->
                !filteredProducts.contains(p) &&
                (p.categoria == filteredProducts.firstOrNull()?.categoria ||
                 p.nombre.split(" ").any { word -> 
                     searchQuery.split(" ").any { it.length > 2 && word.contains(it, ignoreCase = true) }
                 })
            }.take(4)
        }

        CatalogState(
            filteredProducts = filteredProducts,
            categories = categories,
            search = searchQuery,
            selectedCategory = category,
            suggestedProducts = suggestedProducts,
            recentSearches = recentSearches.take(5),
            isSearching = searchQuery.isNotEmpty(),
            isLoading = false // Ya tenemos datos, no está cargando
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
    
    fun addToRecentSearches(query: String) {
        if (query.isNotBlank() && query.length >= 2) {
            val current = _recentSearches.value.toMutableList()
            current.remove(query)
            current.add(0, query)
            _recentSearches.value = current.take(5)
        }
    }
    
    fun clearSearch() {
        _search.value = ""
    }
    
    fun selectRecentSearch(query: String) {
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