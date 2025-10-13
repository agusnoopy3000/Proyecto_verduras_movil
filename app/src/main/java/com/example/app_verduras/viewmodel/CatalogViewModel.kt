package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.repository.RepositorioPruebas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// --- CAMBIOS EN CatalogState ---
// 1. Renombramos `products` a `filteredProducts` para que coincida con la UI
// 2. Añadimos `selectedCategory` para que la UI sepa qué mostrar
data class CatalogState(
    val filteredProducts: List<Producto> = emptyList(),
    val categories: List<String> = emptyList(),
    val search: String = "",
    val selectedCategory: String? = null
)

class CatalogViewModel : ViewModel() {

    private val repo = RepositorioPruebas()

    private val _search = MutableStateFlow("")
    // 3. Añadimos un flujo para la categoría seleccionada desde la UI
    private val _selectedCategory = MutableStateFlow<String?>(null)


    // 4. `uiState` ahora combina la búsqueda y la categoría seleccionada
    val uiState: StateFlow<CatalogState> = combine(
        _search,
        _selectedCategory
    ) { searchQuery, category ->
        val products = repo.getProducts()
        val filteredProducts = products.filter { p ->
            (category == null || p.categoria == category) &&
                    (searchQuery.isEmpty() || p.nombre.contains(searchQuery, ignoreCase = true))
        }

        CatalogState(
            filteredProducts = filteredProducts,
            categories = repo.getCategories(),
            search = searchQuery,
            selectedCategory = category
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogState()
    )

    // 5. Añadimos la función para que la UI actualice la categoría
    fun updateCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun updateSearch(query: String) {
        _search.value = query
    }
}
