package com.example.app_verduras.repository

import android.util.Log
import com.example.app_verduras.dal.PedidoDao
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.firebase.FirestoreService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Repositorio de pedidos con sincronización híbrida Room + Firebase.
 * 
 * ARQUITECTURA:
 * - Room: Base de datos local para persistencia offline
 * - Firebase Firestore: Base de datos en la nube para sincronización entre dispositivos
 * 
 * FLUJO:
 * 1. Al crear pedido: Guarda en Room + Firebase
 * 2. Al actualizar estado (Admin): Actualiza en Room + Firebase
 * 3. Al cargar pedidos (Admin): Usa Firebase para ver TODOS los pedidos
 * 4. Al cargar pedidos (Cliente): Usa Room local filtrado por email
 */
class PedidoRepository(private val pedidoDao: PedidoDao) {
    
    private val TAG = "PedidoRepository"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Flujo de datos local desde Room (para clientes)
    val todosLosPedidosLocal: Flow<List<Pedido>> = pedidoDao.obtenerTodosLosPedidosFlow()
    
    // Flujo de datos desde Firebase (para admin - todos los pedidos de todos los dispositivos)
    private val _pedidosFirebase = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidosFirebase: StateFlow<List<Pedido>> = _pedidosFirebase.asStateFlow()
    
    // Estado de sincronización
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    // Flujo combinado para el Admin (prioriza Firebase)
    val todosLosPedidos: Flow<List<Pedido>> = FirestoreService.getPedidosFlow()
    
    init {
        // Iniciar escucha de cambios en Firebase
        startFirebaseListener()
    }
    
    /**
     * Inicia el listener de Firebase para recibir actualizaciones en tiempo real.
     */
    private fun startFirebaseListener() {
        scope.launch {
            try {
                FirestoreService.getPedidosFlow().collect { pedidos ->
                    _pedidosFirebase.value = pedidos
                    Log.d(TAG, "Firebase listener: ${pedidos.size} pedidos recibidos")
                    
                    // Sincronizar con Room local
                    pedidos.forEach { pedido ->
                        try {
                            pedidoDao.insert(pedido)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error sincronizando pedido ${pedido.id} a Room", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en Firebase listener", e)
            }
        }
    }

    // Obtener los pedidos para un usuario específico (usado por el cliente).
    suspend fun getPedidosByUserEmail(email: String): List<Pedido> {
        return pedidoDao.getPedidosByUserEmail(email)
    }

    /**
     * Insertar un nuevo pedido con sincronización a Firebase.
     */
    suspend fun insert(pedido: Pedido) {
        _syncStatus.value = SyncStatus.Syncing
        
        // Primero guardar en Room (local)
        pedidoDao.insert(pedido)
        Log.d(TAG, "Pedido ${pedido.id} guardado en Room")
        
        // Luego sincronizar con Firebase
        val result = FirestoreService.savePedido(pedido)
        if (result.isSuccess) {
            _syncStatus.value = SyncStatus.Success("Pedido sincronizado")
            Log.d(TAG, "Pedido ${pedido.id} sincronizado con Firebase")
        } else {
            _syncStatus.value = SyncStatus.Error("Error al sincronizar pedido")
            Log.e(TAG, "Error sincronizando pedido ${pedido.id} con Firebase")
        }
    }

    /**
     * Actualizar un pedido completo con sincronización a Firebase.
     */
    suspend fun update(pedido: Pedido) {
        _syncStatus.value = SyncStatus.Syncing
        
        // Actualizar en Room
        pedidoDao.update(pedido)
        
        // Sincronizar con Firebase
        val result = FirestoreService.updatePedido(pedido)
        if (result.isSuccess) {
            _syncStatus.value = SyncStatus.Success("Pedido actualizado")
        } else {
            _syncStatus.value = SyncStatus.Error("Error al actualizar pedido")
        }
    }

    /**
     * Actualizar solo el estado de un pedido (usado por el Admin).
     * Sincroniza automáticamente con Firebase.
     */
    suspend fun updatePedidoStatus(id: Long, nuevoEstado: String) {
        _syncStatus.value = SyncStatus.Syncing
        
        // Actualizar en Room
        pedidoDao.updatePedidoStatus(id, nuevoEstado)
        Log.d(TAG, "Estado de pedido $id actualizado en Room a: $nuevoEstado")
        
        // Sincronizar con Firebase
        val result = FirestoreService.updatePedidoStatus(id, nuevoEstado)
        if (result.isSuccess) {
            _syncStatus.value = SyncStatus.Success("Estado actualizado")
            Log.d(TAG, "Estado sincronizado con Firebase")
        } else {
            _syncStatus.value = SyncStatus.Error("Error al sincronizar estado")
            Log.e(TAG, "Error sincronizando estado con Firebase")
        }
    }

    /**
     * Eliminar un pedido con sincronización a Firebase.
     */
    suspend fun delete(pedido: Pedido) {
        // Eliminar de Room
        pedidoDao.delete(pedido)
        
        // Eliminar de Firebase
        FirestoreService.deletePedido(pedido.id)
    }
    
    /**
     * Sincroniza manualmente los pedidos desde Firebase a Room.
     * Útil para forzar una actualización.
     */
    suspend fun syncFromFirebase() {
        _syncStatus.value = SyncStatus.Syncing
        
        val result = FirestoreService.getAllPedidos()
        if (result.isSuccess) {
            val pedidos = result.getOrNull() ?: emptyList()
            pedidos.forEach { pedido ->
                pedidoDao.insert(pedido)
            }
            _syncStatus.value = SyncStatus.Success("${pedidos.size} pedidos sincronizados")
            Log.d(TAG, "Sincronización completa: ${pedidos.size} pedidos")
        } else {
            _syncStatus.value = SyncStatus.Error("Error al sincronizar")
            Log.e(TAG, "Error sincronizando desde Firebase")
        }
    }
    
    /**
     * Estados posibles de sincronización.
     */
    sealed class SyncStatus {
        object Idle : SyncStatus()
        object Syncing : SyncStatus()
        data class Success(val message: String) : SyncStatus()
        data class Error(val message: String) : SyncStatus()
    }
}
