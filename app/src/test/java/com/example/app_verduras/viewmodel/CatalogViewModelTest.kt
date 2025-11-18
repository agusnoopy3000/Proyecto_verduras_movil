package com.example.app_verduras.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CatalogViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var catalogViewModel: CatalogViewModel
    private lateinit var fakeRepository: FakeProductoRepository

    @Before
    fun setup() {
        fakeRepository = FakeProductoRepository()
        catalogViewModel = CatalogViewModel(fakeRepository)
    }

    @Test
    fun `test para verificar la carga inicial de productos y categorías`() = runTest {
        val productosDePrueba = listOf(
            Producto(id = "1", nombre = "Tomate", precio = 1000.0, imagen = "img_tomate.png", categoria = "Verdura", stock = 10),
            Producto(id = "2", nombre = "Manzana", precio = 1200.0, imagen = "img_manzana.png", categoria = "Fruta", stock = 5),
            Producto(id = "3", nombre = "Lechuga", precio = 800.0, imagen = "img_lechuga.png", categoria = "Verdura", stock = 20)
        )
        fakeRepository.setProductos(productosDePrueba)
        catalogViewModel = CatalogViewModel(fakeRepository)

        val uiState = catalogViewModel.uiState.first()
        
        assertEquals(3, uiState.filteredProducts.size)
        assertEquals(listOf("Fruta", "Verdura"), uiState.categories.sorted())
    }

    @Test
    fun `test para filtrar productos por búsqueda`() = runTest {
        val productosDePrueba = listOf(
            Producto(id = "1", nombre = "Tomate Rojo", precio = 1000.0, imagen = "", categoria = "Verdura", stock = 10),
            Producto(id = "2", nombre = "Manzana Roja", precio = 1200.0, imagen = "", categoria = "Fruta", stock = 5),
            Producto(id = "3", nombre = "Pimentón Rojo", precio = 1500.0, imagen = "", categoria = "Verdura", stock = 8)
        )
        fakeRepository.setProductos(productosDePrueba)
        catalogViewModel = CatalogViewModel(fakeRepository)

        catalogViewModel.updateSearch("Rojo")

        var uiState = catalogViewModel.uiState.first()
        assertEquals(3, uiState.filteredProducts.size)

        catalogViewModel.updateSearch("Tomate")
        uiState = catalogViewModel.uiState.first()
        assertEquals(1, uiState.filteredProducts.size)
        assertEquals("Tomate Rojo", uiState.filteredProducts[0].nombre)
    }
}
