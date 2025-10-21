package com.example.app_verduras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_verduras.Model.User
import com.example.app_verduras.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UserManagementViewModel(private val userRepository: UserRepository) : ViewModel() {

    val users: Flow<List<User>> = userRepository.allUsers

    private val _eventChannel = Channel<UiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    fun updateUser(user: User) {
        viewModelScope.launch {
            userRepository.update(user)
            _eventChannel.send(UiEvent.ShowSnackbar("Cambios guardados correctamente"))
            _eventChannel.send(UiEvent.DismissEditModal)
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
