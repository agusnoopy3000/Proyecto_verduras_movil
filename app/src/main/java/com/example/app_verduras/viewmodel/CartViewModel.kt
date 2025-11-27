
package com.example.app_verduras.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.api.RetrofitClient
import com.example.app_verduras.api.models.OrderItemRequest
import com.example.app_verduras.api.models.OrderRequest
import com.example.app_verduras.api.models.OrderResponse
import com.example.app_verduras.dal.PedidoDao
import com.example.app_verduras.dal.UserDao
import com.example.app_verduras.repository.ProductoRepository
import com.example.app_verduras.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Modelos del carrito ---
data class CartItem(val product: Producto, val qty: Int)
data class CartState(val items: List<CartItem> = emptyList()) {
    val total: Double get() = items.sumOf { it.product.precio * it.qty }
}

sealed class OrderProcessingState {
    object Idle : OrderProcessingState()
    object Processing : OrderProcessingState()
    data class Success(val order: OrderResponse? = null) : OrderProcessingState()
    data class Error(val message: String) : OrderProcessingState()
}

// --- ViewModel principal del carrito ---
class CartViewModel(
    private val productoRepository: ProductoRepository,
    private val pedidoDao: PedidoDao,
    private val userDao: UserDao
) : ViewModel() {

    private val _cartState = MutableStateFlow(CartState())
    val cartState = _cartState.asStateFlow()

    private val _orderProcessingState = MutableStateFlow<OrderProcessingState>(OrderProcessingState.Idle)
    val orderProcessingState = _orderProcessingState.asStateFlow()

    private val _userAddress = MutableStateFlow<String?>(null)
    val userAddress = _userAddress.asStateFlow()

    // Lista de pedidos del usuario
    private val _myOrders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val myOrders = _myOrders.asStateFlow()

    private val _ordersLoading = MutableStateFlow(false)
    val ordersLoading = _ordersLoading.asStateFlow()

    private val apiService = RetrofitClient.apiService

    init {
        loadUserAddress()
    }

    private fun loadUserAddress() {
        viewModelScope.launch {
            val userEmail = SessionManager.currentUser?.email
            if (userEmail != null) {
                val user = userDao.findByEmail(userEmail)
                _userAddress.value = user?.direccion ?: SessionManager.currentUser?.direccion
            }
        }
    }

    fun addToCart(productId: String) {
        viewModelScope.launch {
            val product = productoRepository.getById(productId)
            product?.let { foundProduct ->
                val currentItems = _cartState.value.items.toMutableList()
                val index = currentItems.indexOfFirst { it.product.id == productId }
                if (index >= 0) {
                    val updatedItem = currentItems[index].copy(qty = currentItems[index].qty + 1)
                    currentItems[index] = updatedItem
                } else {
                    currentItems.add(CartItem(product = foundProduct, qty = 1))
                }
                _cartState.value = CartState(currentItems)
            }
        }
    }

    fun increase(productId: String) = modifyQty(productId, +1)
    fun decrease(productId: String) = modifyQty(productId, -1)

    private fun modifyQty(id: String, delta: Int) {
        val current = _cartState.value.items.toMutableList()
        val index = current.indexOfFirst { it.product.id == id }
        if (index >= 0) {
            val item = current[index]
            val newQty = (item.qty + delta).coerceAtLeast(0)
            if (newQty == 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(qty = newQty)
            }
            _cartState.value = CartState(current)
        }
    }

    fun remove(productId: String) {
        val currentItems = _cartState.value.items.toMutableList()
        currentItems.removeAll { it.product.id == productId }
        _cartState.value = CartState(currentItems)
    }

    /**
     * Confirma el pedido usando la API de Huerto Hogar.
     * IMPORTANTE: productoId debe ser el CÓDIGO del producto (ej: "VH-001"), NO el id numérico.
     * 
     * @param deliveryAddress Dirección de entrega
     * @param deliveryDate Fecha de entrega en formato dd/MM/yyyy (se convierte a ISO yyyy-MM-dd)
     * @param finalTotal Total del pedido
     * @param region Región (opcional)
     * @param comuna Comuna (opcional)
     * @param comentarios Comentarios adicionales (opcional)
     */
    fun confirmOrder(
        deliveryAddress: String,
        deliveryDate: String,
        finalTotal: Double,
        region: String? = null,
        comuna: String? = null,
        comentarios: String? = null
    ) {
        if (_orderProcessingState.value is OrderProcessingState.Processing) return
        if (_cartState.value.items.isEmpty()) return

        viewModelScope.launch {
            _orderProcessingState.value = OrderProcessingState.Processing

            try {
                // Convertir fecha de dd/MM/yyyy a yyyy-MM-dd (formato ISO)
                val isoDate = convertToIsoDate(deliveryDate)
                
                // Crear items del pedido usando el CÓDIGO del producto
                val orderItems = _cartState.value.items.map { cartItem ->
                    OrderItemRequest(
                        productoId = cartItem.product.codigo,  // ¡IMPORTANTE: Usar código, no id!
                        cantidad = cartItem.qty
                    )
                }

                val orderRequest = OrderRequest(
                    direccionEntrega = deliveryAddress,
                    fechaEntrega = isoDate,
                    region = region,
                    comuna = comuna,
                    comentarios = comentarios,
                    items = orderItems
                )

                Log.d("CartViewModel", "Creando pedido: $orderRequest")

                val response = apiService.createOrder(orderRequest)

                if (response.isSuccessful && response.body() != null) {
                    val orderResponse = response.body()!!
                    
                    Log.d("CartViewModel", "Pedido creado exitosamente: ID=${orderResponse.id}")
                    
                    // Guardar pedido en base de datos local
                    val localPedido = Pedido(
                        id = orderResponse.id,
                        userEmail = SessionManager.currentUser?.email ?: "",
                        fechaEntrega = orderResponse.fechaEntrega,
                        direccionEntrega = orderResponse.direccionEntrega,
                        region = orderResponse.region,
                        comuna = orderResponse.comuna,
                        comentarios = orderResponse.comentarios,
                        total = orderResponse.total,
                        estado = orderResponse.estado,
                        createdAt = orderResponse.createdAt
                    )
                    pedidoDao.insert(localPedido)
                    
                    // Limpiar carrito
                    _cartState.value = CartState()
                    
                    _orderProcessingState.value = OrderProcessingState.Success(orderResponse)
                    
                    delay(100L)
                    _orderProcessingState.value = OrderProcessingState.Idle
                    
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Datos del pedido inválidos o producto no encontrado."
                        401 -> "Sesión expirada. Por favor, inicia sesión nuevamente."
                        else -> "Error al crear el pedido: ${response.message()}"
                    }
                    Log.e("CartViewModel", "Error al crear pedido: ${response.code()} - ${response.message()}")
                    _orderProcessingState.value = OrderProcessingState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Excepción al crear pedido: ${e.message}", e)
                _orderProcessingState.value = OrderProcessingState.Error(
                    "Error de conexión. Verifica tu internet."
                )
            }
        }
    }

    /**
     * Convierte una fecha de formato dd/MM/yyyy a formato ISO yyyy-MM-dd
     */
    private fun convertToIsoDate(dateStr: String): String? {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            date?.let { outputFormat.format(it) }
        } catch (e: Exception) {
            Log.w("CartViewModel", "Error al convertir fecha: $dateStr", e)
            null
        }
    }

    /**
     * Carga los pedidos del usuario desde la API
     */
    fun loadMyOrders() {
        viewModelScope.launch {
            _ordersLoading.value = true
            try {
                val response = apiService.getMyOrders()
                if (response.isSuccessful && response.body() != null) {
                    _myOrders.value = response.body()!!
                    Log.d("CartViewModel", "Pedidos cargados: ${_myOrders.value.size}")
                } else {
                    Log.e("CartViewModel", "Error al cargar pedidos: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Excepción al cargar pedidos: ${e.message}", e)
            } finally {
                _ordersLoading.value = false
            }
        }
    }

    fun dismissError() {
        _orderProcessingState.value = OrderProcessingState.Idle
    }

    class Factory(
        private val productoRepository: ProductoRepository,
        private val pedidoDao: PedidoDao,
        private val userDao: UserDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
                return CartViewModel(productoRepository, pedidoDao, userDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
