package com.example.app_verduras.viewmodel


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.app_verduras.repository.RepositorioPruebas

class HomeViewModel : ViewModel() {
    private val repo = RepositorioPruebas()
    private val _categories = MutableStateFlow(repo.getCategories())
    val categories = _categories.asStateFlow()
}
