package com.example.app_verduras.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LocationState(
    val hasPermission: Boolean = false,
    val lastLocation: Location? = null,
    val shippingCost: Double = 0.0,
    val isPermissionRequestInProgress: Boolean = false
)

class LocationViewModel(private val application: Application) : AndroidViewModel(application) {

    private val _locationState = MutableStateFlow(LocationState())
    val locationState = _locationState.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    init {
        _locationState.update { it.copy(hasPermission = hasLocationPermission()) }
    }

    fun requestLocationPermission() {
        _locationState.update { it.copy(isPermissionRequestInProgress = true) }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _locationState.update {
            it.copy(
                hasPermission = isGranted,
                isPermissionRequestInProgress = false
            )
        }
        if (isGranted) {
            fetchLastLocation()
        }
    }

    @SuppressWarnings("MissingPermission")
    fun fetchLastLocation() {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val cost = calculateShippingCost(it)
                    _locationState.update { state ->
                        state.copy(lastLocation = it, shippingCost = cost)
                    }
                }
            }
        }
    }

    private fun calculateShippingCost(location: Location): Double {
        // Lógica de ejemplo: costo basado en la latitud.
        // Zona 1: Latitud > -34.6 (ej. norte de CABA) -> $150
        // Zona 2: Latitud <= -34.6 (ej. sur de CABA) -> $250
        return if (location.latitude > -34.60) 150.0 else 250.0
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

class LocationViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
