package com.example.app_verduras.repository

import android.util.Log
import com.example.app_verduras.dal.UserDao
import com.example.app_verduras.Model.User
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
 * Repositorio de usuarios con sincronización híbrida Room + Firebase.
 * 
 * ARQUITECTURA:
 * - Room: Base de datos local para persistencia offline
 * - Firebase Firestore: Base de datos en la nube para sincronización entre dispositivos
 * 
 * FLUJO:
 * 1. Al registrar usuario: Guarda en Room + Firebase
 * 2. Al actualizar usuario: Actualiza en Room + Firebase
 * 3. Al cargar usuarios (Admin): Usa Firebase para ver TODOS los usuarios registrados
 */
class UserRepository(private val userDao: UserDao) {
    
    private val TAG = "UserRepository"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Un flujo que emite la lista completa de usuarios desde Room (local).
     */
    val allUsersLocal: Flow<List<User>> = userDao.getAllUsersFlow()
    
    /**
     * Flow de usuarios en tiempo real desde Firebase (para admin).
     * Esto permite ver TODOS los usuarios registrados en la app.
     */
    val allUsers: Flow<List<User>> = FirestoreService.getUsersFlow()
    
    // Estado de sincronización
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
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
                FirestoreService.getUsersFlow().collect { users ->
                    Log.d(TAG, "Firebase listener: ${users.size} usuarios recibidos")
                    
                    // Sincronizar con Room local (sin contraseña)
                    users.forEach { user ->
                        try {
                            // Verificar si el usuario ya existe localmente
                            val existingUser = userDao.getUserByEmail(user.email)
                            if (existingUser != null) {
                                // Mantener la contraseña local si existe
                                userDao.insert(user.copy(password = existingUser.password))
                            } else {
                                userDao.insert(user)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error sincronizando usuario ${user.email} a Room", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en Firebase listener", e)
            }
        }
    }

    /**
     * Función para actualizar un usuario en la base de datos con sincronización Firebase.
     */
    suspend fun update(user: User) {
        _syncStatus.value = SyncStatus.Syncing
        
        // Actualizar en Room
        userDao.update(user)
        
        // Sincronizar con Firebase
        val result = FirestoreService.updateUser(user)
        if (result.isSuccess) {
            _syncStatus.value = SyncStatus.Success("Usuario actualizado")
            Log.d(TAG, "Usuario ${user.email} sincronizado con Firebase")
        } else {
            _syncStatus.value = SyncStatus.Error("Error al sincronizar")
            Log.e(TAG, "Error sincronizando usuario con Firebase")
        }
    }
    
    /**
     * Registra un nuevo usuario con sincronización a Firebase.
     */
    suspend fun insert(user: User) {
        _syncStatus.value = SyncStatus.Syncing
        
        // Guardar en Room
        userDao.insert(user)
        Log.d(TAG, "Usuario ${user.email} guardado en Room")
        
        // Sincronizar con Firebase (sin contraseña)
        val result = FirestoreService.saveUser(user)
        if (result.isSuccess) {
            _syncStatus.value = SyncStatus.Success("Usuario registrado")
            Log.d(TAG, "Usuario ${user.email} sincronizado con Firebase")
        } else {
            _syncStatus.value = SyncStatus.Error("Error al sincronizar")
            Log.e(TAG, "Error sincronizando usuario con Firebase")
        }
    }
    
    /**
     * Obtiene un usuario por email desde Room.
     */
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
    
    /**
     * Sincroniza manualmente los usuarios desde Firebase a Room.
     */
    suspend fun syncFromFirebase() {
        _syncStatus.value = SyncStatus.Syncing
        
        val result = FirestoreService.getAllUsers()
        if (result.isSuccess) {
            val users = result.getOrNull() ?: emptyList()
            users.forEach { user ->
                val existingUser = userDao.getUserByEmail(user.email)
                if (existingUser != null) {
                    userDao.insert(user.copy(password = existingUser.password))
                } else {
                    userDao.insert(user)
                }
            }
            _syncStatus.value = SyncStatus.Success("${users.size} usuarios sincronizados")
            Log.d(TAG, "Sincronización completa: ${users.size} usuarios")
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
