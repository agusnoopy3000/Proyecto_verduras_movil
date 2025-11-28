package com.example.app_verduras.firebase

import android.util.Log
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.Model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Servicio centralizado para operaciones de Firestore.
 * Maneja la sincronización de pedidos y usuarios entre la app y Firebase.
 */
object FirestoreService {
    
    private const val TAG = "FirestoreService"
    
    // Colecciones de Firestore
    private const val COLLECTION_PEDIDOS = "pedidos"
    private const val COLLECTION_USERS = "users"
    
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    
    // ==================== PEDIDOS ====================
    
    /**
     * Guarda un pedido en Firestore.
     * Usa el ID del pedido como documento ID para facilitar actualizaciones.
     */
    suspend fun savePedido(pedido: Pedido): Result<Unit> {
        return try {
            val pedidoMap = pedidoToMap(pedido)
            firestore.collection(COLLECTION_PEDIDOS)
                .document(pedido.id.toString())
                .set(pedidoMap)
                .await()
            Log.d(TAG, "Pedido ${pedido.id} guardado en Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando pedido en Firestore", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los pedidos de Firestore (una sola vez).
     */
    suspend fun getAllPedidos(): Result<List<Pedido>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_PEDIDOS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val pedidos = snapshot.documents.mapNotNull { doc ->
                mapToPedido(doc.data, doc.id)
            }
            Log.d(TAG, "Obtenidos ${pedidos.size} pedidos de Firestore")
            Result.success(pedidos)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo pedidos de Firestore", e)
            Result.failure(e)
        }
    }
    
    /**
     * Flow de pedidos en tiempo real desde Firestore.
     * Emite una nueva lista cada vez que hay cambios en la colección.
     */
    fun getPedidosFlow(): Flow<List<Pedido>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = firestore
            .collection(COLLECTION_PEDIDOS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error en listener de pedidos", error)
                    return@addSnapshotListener
                }
                
                val pedidos = snapshot?.documents?.mapNotNull { doc ->
                    mapToPedido(doc.data, doc.id)
                } ?: emptyList()
                
                Log.d(TAG, "Pedidos actualizados en tiempo real: ${pedidos.size}")
                trySend(pedidos)
            }
        
        awaitClose {
            Log.d(TAG, "Cerrando listener de pedidos")
            listenerRegistration.remove()
        }
    }
    
    /**
     * Actualiza el estado de un pedido en Firestore.
     */
    suspend fun updatePedidoStatus(pedidoId: Long, nuevoEstado: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_PEDIDOS)
                .document(pedidoId.toString())
                .update("estado", nuevoEstado)
                .await()
            Log.d(TAG, "Estado de pedido $pedidoId actualizado a: $nuevoEstado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando estado de pedido", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza un pedido completo en Firestore.
     */
    suspend fun updatePedido(pedido: Pedido): Result<Unit> {
        return try {
            val pedidoMap = pedidoToMap(pedido)
            firestore.collection(COLLECTION_PEDIDOS)
                .document(pedido.id.toString())
                .set(pedidoMap)
                .await()
            Log.d(TAG, "Pedido ${pedido.id} actualizado en Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando pedido en Firestore", e)
            Result.failure(e)
        }
    }
    
    /**
     * Elimina un pedido de Firestore.
     */
    suspend fun deletePedido(pedidoId: Long): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_PEDIDOS)
                .document(pedidoId.toString())
                .delete()
                .await()
            Log.d(TAG, "Pedido $pedidoId eliminado de Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando pedido de Firestore", e)
            Result.failure(e)
        }
    }
    
    // ==================== USUARIOS ====================
    
    /**
     * Guarda un usuario en Firestore.
     */
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            val userMap = userToMap(user)
            firestore.collection(COLLECTION_USERS)
                .document(user.email)
                .set(userMap)
                .await()
            Log.d(TAG, "Usuario ${user.email} guardado en Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando usuario en Firestore", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los usuarios de Firestore (una sola vez).
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { doc ->
                mapToUser(doc.data, doc.id)
            }
            Log.d(TAG, "Obtenidos ${users.size} usuarios de Firestore")
            Result.success(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuarios de Firestore", e)
            Result.failure(e)
        }
    }
    
    /**
     * Flow de usuarios en tiempo real desde Firestore.
     */
    fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = firestore
            .collection(COLLECTION_USERS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error en listener de usuarios", error)
                    return@addSnapshotListener
                }
                
                val users = snapshot?.documents?.mapNotNull { doc ->
                    mapToUser(doc.data, doc.id)
                } ?: emptyList()
                
                Log.d(TAG, "Usuarios actualizados en tiempo real: ${users.size}")
                trySend(users)
            }
        
        awaitClose {
            Log.d(TAG, "Cerrando listener de usuarios")
            listenerRegistration.remove()
        }
    }
    
    /**
     * Actualiza un usuario en Firestore.
     */
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userMap = userToMap(user)
            firestore.collection(COLLECTION_USERS)
                .document(user.email)
                .set(userMap)
                .await()
            Log.d(TAG, "Usuario ${user.email} actualizado en Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando usuario en Firestore", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene un usuario por email desde Firestore.
     */
    suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            val doc = firestore.collection(COLLECTION_USERS)
                .document(email)
                .get()
                .await()
            
            val user = if (doc.exists()) {
                mapToUser(doc.data, doc.id)
            } else null
            
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuario $email de Firestore", e)
            Result.failure(e)
        }
    }
    
    // ==================== CONVERSIONES ====================
    
    private fun pedidoToMap(pedido: Pedido): Map<String, Any?> {
        return mapOf(
            "id" to pedido.id,
            "userEmail" to pedido.userEmail,
            "fechaEntrega" to pedido.fechaEntrega,
            "direccionEntrega" to pedido.direccionEntrega,
            "region" to pedido.region,
            "comuna" to pedido.comuna,
            "comentarios" to pedido.comentarios,
            "total" to pedido.total,
            "estado" to pedido.estado,
            "createdAt" to pedido.createdAt
        )
    }
    
    private fun mapToPedido(data: Map<String, Any?>?, documentId: String): Pedido? {
        if (data == null) return null
        
        return try {
            Pedido(
                id = (data["id"] as? Number)?.toLong() ?: documentId.toLongOrNull() ?: 0L,
                userEmail = data["userEmail"] as? String ?: "",
                fechaEntrega = data["fechaEntrega"] as? String,
                direccionEntrega = data["direccionEntrega"] as? String ?: "",
                region = data["region"] as? String,
                comuna = data["comuna"] as? String,
                comentarios = data["comentarios"] as? String,
                total = (data["total"] as? Number)?.toDouble() ?: 0.0,
                estado = data["estado"] as? String ?: "PENDIENTE",
                createdAt = data["createdAt"] as? String
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo documento a Pedido", e)
            null
        }
    }
    
    private fun userToMap(user: User): Map<String, Any?> {
        return mapOf(
            "email" to user.email,
            "nombre" to user.nombre,
            "apellido" to user.apellido,
            "run" to user.run,
            "direccion" to user.direccion,
            "telefono" to user.telefono,
            "rol" to user.rol,
            "createdAt" to user.createdAt
            // Nota: No guardamos la contraseña en Firestore por seguridad
        )
    }
    
    private fun mapToUser(data: Map<String, Any?>?, documentId: String): User? {
        if (data == null) return null
        
        return try {
            User(
                email = data["email"] as? String ?: documentId,
                nombre = data["nombre"] as? String ?: "",
                apellido = data["apellido"] as? String ?: "",
                password = "", // No se almacena en Firestore
                run = data["run"] as? String,
                direccion = data["direccion"] as? String,
                telefono = data["telefono"] as? String,
                rol = data["rol"] as? String ?: "USER",
                createdAt = data["createdAt"] as? String
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo documento a User", e)
            null
        }
    }
}
