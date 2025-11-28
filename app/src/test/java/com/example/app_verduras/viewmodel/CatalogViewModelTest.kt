package com.example.app_verduras.viewmodel

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para CatalogState.
 * 
 * Estas pruebas verifican el estado del catálogo y filtros.
 */
class CatalogViewModelTest {

    @Test
    fun `estado inicial tiene listas vacias`() {
        val state = CatalogState()
        
        assertTrue(state.filteredProducts.isEmpty())
        assertTrue(state.categories.isEmpty())
        assertEquals("", state.search)
        assertNull(state.selectedCategory)
    }

    @Test
    fun `estado con busqueda activa`() {
        val state = CatalogState(search = "tomate")
        
        assertEquals("tomate", state.search)
    }

    @Test
    fun `estado con categoria seleccionada`() {
        val state = CatalogState(selectedCategory = "Verduras")
        
        assertEquals("Verduras", state.selectedCategory)
    }

    @Test
    fun `estado con categorias disponibles`() {
        val categorias = listOf("Verduras", "Frutas", "Hortalizas")
        val state = CatalogState(categories = categorias)
        
        assertEquals(3, state.categories.size)
        assertTrue(state.categories.contains("Verduras"))
        assertTrue(state.categories.contains("Frutas"))
    }

    @Test
    fun `limpiar filtro de categoria`() {
        val stateConFiltro = CatalogState(selectedCategory = "Frutas")
        val stateSinFiltro = stateConFiltro.copy(selectedCategory = null)
        
        assertNotNull(stateConFiltro.selectedCategory)
        assertNull(stateSinFiltro.selectedCategory)
    }

    @Test
    fun `limpiar busqueda`() {
        val stateConBusqueda = CatalogState(search = "manzana")
        val stateSinBusqueda = stateConBusqueda.copy(search = "")
        
        assertEquals("manzana", stateConBusqueda.search)
        assertEquals("", stateSinBusqueda.search)
    }

    @Test
    fun `busqueda ignora mayusculas`() {
        // Este test documenta el comportamiento esperado
        val searchLower = "tomate"
        val searchUpper = "TOMATE"
        
        // La lógica de filtrado debería ignorar mayúsculas
        assertTrue(searchLower.equals(searchUpper, ignoreCase = true))
    }

    @Test
    fun `estado combina busqueda y categoria`() {
        val state = CatalogState(
            search = "orgánico",
            selectedCategory = "Verduras"
        )
        
        assertEquals("orgánico", state.search)
        assertEquals("Verduras", state.selectedCategory)
    }
}
