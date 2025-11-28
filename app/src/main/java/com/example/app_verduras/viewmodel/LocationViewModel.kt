package com.example.app_verduras.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class LocationViewModel : ViewModel() {

    private val _shippingCost = MutableStateFlow(0.0)
    val shippingCost = _shippingCost.asStateFlow()

    private val _locationEnabled = mutableStateOf(true)
    val locationEnabled: State<Boolean> = _locationEnabled

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation = _userLocation.asStateFlow()

    private val _userAddress = MutableStateFlow<String?>(null)
    val userAddress = _userAddress.asStateFlow()

    @SuppressLint("MissingPermission")
    fun getDeviceLocation(context: Context) {
        if (!hasLocationPermission(context)) {
            _shippingCost.value = 0.0
            _userLocation.value = null
            _userAddress.value = null
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                _shippingCost.value = 2500.0
                _userLocation.value = location
                // Geocodificación inversa
                getAddressFromLocation(context, location)
            } else {
                _shippingCost.value = 0.0
                _userLocation.value = null
                _userAddress.value = "No se pudo obtener la dirección"
            }
        }.addOnFailureListener {
            _shippingCost.value = 0.0
            _userLocation.value = null
            _userAddress.value = "Error al obtener la dirección"
        }
    }

    private fun getAddressFromLocation(context: Context, location: Location) {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                _userAddress.value = addresses.firstOrNull()?.getAddressLine(0)
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            _userAddress.value = addresses?.firstOrNull()?.getAddressLine(0)
        }
    }

    fun setLocationEnabled(enabled: Boolean, context: Context) {
        _locationEnabled.value = enabled
        if (enabled) {
            getDeviceLocation(context)
        } else {
            _shippingCost.value = 0.0
            _userLocation.value = null
            _userAddress.value = null
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
