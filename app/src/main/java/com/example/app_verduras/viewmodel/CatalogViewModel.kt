package com.example.app_verduras.viewmodel

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.repository.RepositorioPruebas

data class CatalogState(
    val products: List<Producto> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val search: String = ""
) {
    val filteredProducts: List<Producto>
        get() = products.filter { p ->
            (selectedCategory == null || p.categoria == selectedCategory) &&
                    (search.isEmpty() || p.nombre.contains(search, ignoreCase = true))
        }
}

class CatalogViewModel : ViewModel() {
    private val repo = RepositorioPruebas()

    private val _state = MutableStateFlow(
        CatalogState(
            products = repo.getProducts(),
            categories = repo.getCategories()
        )
    )
    val uiState = _state.asStateFlow()

    fun updateCategory(cat: String?) {
        _state.value = _state.value.copy(selectedCategory = cat)
    }

    fun updateSearch(query: String) {
        _state.value = _state.value.copy(search = query)
    }
}
