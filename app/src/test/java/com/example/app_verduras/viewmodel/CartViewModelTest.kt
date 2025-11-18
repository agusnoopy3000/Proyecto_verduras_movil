package com.example.app_verduras.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.dal.PedidoDao
import com.example.app_verduras.dal.UserDao
import com.example.app_verduras.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class CartViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var cartViewModel: CartViewModel
    private lateinit var fakeRepository: FakeProductoRepository
    private lateinit var fakePedidoDao: PedidoDao
    private lateinit var fakeUserDao: UserDao

    @Before
    fun setup() {
        fakeRepository = FakeProductoRepository()
        fakePedidoDao = mock(PedidoDao::class.java)
        fakeUserDao = mock(UserDao::class.java)
        cartViewModel = CartViewModel(fakeRepository, fakePedidoDao, fakeUserDao)
    }

    @Test
    fun `test para agregar un producto al carrito`() = runTest {
        val productoDePrueba = Producto(
            id = "1", 
            nombre = "Tomate", 
            precio = 1000.0, 
            imagen = "img_tomate.png", 
            categoria = "Verdura", 
            descripcion = "Tomate fresco y jugoso",
            stock = 10
        )
        fakeRepository.setProductos(listOf(productoDePrueba))

        cartViewModel.addToCart("1")

        val cartState = cartViewModel.cartState.first()
        assertEquals(1, cartState.items.size)
        assertEquals("Tomate", cartState.items[0].product.nombre)
        assertEquals(1, cartState.items[0].qty)
        assertEquals(1000.0, cartState.total, 0.0)
    }
}
