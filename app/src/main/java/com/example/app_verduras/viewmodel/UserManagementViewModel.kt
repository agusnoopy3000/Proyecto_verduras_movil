package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.User
import com.example.app_verduras.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de usuarios del administrador.
 * 
 * FUNCIONALIDADES:
 * - Ver TODOS los usuarios registrados (desde Firebase en tiempo real)
 * - Actualizar información de usuarios
 * - Ver estado de sincronización
 */
class UserManagementViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Usuarios en tiempo real desde Firebase (TODOS los usuarios)
    val users: StateFlow<List<User>> = userRepository.allUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Estado de sincronización
    val syncStatus: StateFlow<UserRepository.SyncStatus> = userRepository.syncStatus
    
    // Indicador de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Filtro de rol actual
    private val _filtroRol = MutableStateFlow<String?>(null)
    val filtroRol: StateFlow<String?> = _filtroRol.asStateFlow()

    private val _eventChannel = Channel<UiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()
    
    init {
        // Sincronizar usuarios al iniciar
        refreshUsers()
    }
    
    /**
     * Fuerza una sincronización de usuarios desde Firebase.
     */
    fun refreshUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.syncFromFirebase()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Filtra los usuarios por rol.
     */
    fun setFiltroRol(rol: String?) {
        _filtroRol.value = rol
    }

    /**
     * Actualiza un usuario.
     */
    fun updateUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.update(user)
                _eventChannel.send(UiEvent.ShowSnackbar("Usuario actualizado y sincronizado"))
                _eventChannel.send(UiEvent.DismissEditModal)
            } catch (e: Exception) {
                _eventChannel.send(UiEvent.ShowSnackbar("Error al actualizar: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object DismissEditModal : UiEvent()
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserManagementViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
