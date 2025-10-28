package com.example.app_verduras.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationViewModel : ViewModel() {

    private val _shippingCost = MutableStateFlow(0.0)
    val shippingCost = _shippingCost.asStateFlow()

    // El estado ahora es observable y su valor por defecto es 'true'
    private val _locationEnabled = mutableStateOf(true)
    val locationEnabled: State<Boolean> = _locationEnabled

    @SuppressLint("MissingPermission")
    fun getDeviceLocation(context: Context) {
        if (!hasLocationPermission(context)) {
            _shippingCost.value = 0.0
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Si se encuentra la ubicación, se establece el costo de envío.
                _shippingCost.value = 2500.0
            } else {
                _shippingCost.value = 0.0
            }
        }.addOnFailureListener {
            _shippingCost.value = 0.0
        }
    }

    fun setLocationEnabled(enabled: Boolean, context: Context) {
        _locationEnabled.value = enabled
        if (enabled) {
            // Si se activa, intenta calcular el costo.
            getDeviceLocation(context)
        } else {
            // Si se desactiva, el costo es cero.
            _shippingCost.value = 0.0
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
