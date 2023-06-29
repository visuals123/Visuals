package com.uc.ccs.visuals.screens.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.model.DirectionsRoute
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.main.models.MarkerInfo
import com.uc.ccs.visuals.screens.settings.CsvData

class MapViewModel: ViewModel() {

    /**
     * Nearby markers
     * */
    private val _markers = MutableLiveData<List<MarkerInfo>>()
    val markers: LiveData<List<MarkerInfo>> = _markers

    private val _nearbyMarkers = MutableLiveData<List<MarkerInfo>>()
    val nearbyMarkers: LiveData<List<MarkerInfo>> = _nearbyMarkers

    private val _incomingMarkers = MutableLiveData<List<MarkerInfo>>()
    val incomingMarkers: LiveData<List<MarkerInfo>> = _incomingMarkers

    private val _allMarkers = MutableLiveData<List<MarkerInfo>>()
    val allMarkers: LiveData<List<MarkerInfo>> = _allMarkers

    private val _allMarkersUntouched = MutableLiveData<List<MarkerInfo>>()
    val allMarkersUntouched: LiveData<List<MarkerInfo>> = _allMarkersUntouched

    private val _currentDirection = MutableLiveData<DirectionsRoute>()
    val currentDirection: LiveData<DirectionsRoute> = _currentDirection

    private val _currentDestination = MutableLiveData<LatLng>()
    val currentDestination: LiveData<LatLng> = _currentDestination

    private val _currentDestinationName = MutableLiveData<String>()
    val currentDestinationName: LiveData<String> = _currentDestinationName

    private val _currentSpeed = MutableLiveData<String>()
    val currentSpeed: LiveData<String> = _currentSpeed

    private val _cacheStartingPositon = MutableLiveData<LatLng>()
    val cachedStartingPositon: LiveData<LatLng> = _cacheStartingPositon

    private val _csvDataFromFirestore = MutableLiveData<List<CsvData>>()
    val csvDataFromFirestore: LiveData<List<CsvData>> get() = _csvDataFromFirestore

    private val _csvDataState = MutableLiveData<CsvDataState>()
    val csvDataState: LiveData<CsvDataState> get() = _csvDataState

    private val _currentUser = MutableLiveData<UserItem>()
    val currentUser get() = _currentUser

    private val _currentLatLng = MutableLiveData<LatLng>()
    val currentLatLng: LiveData<LatLng> get() = _currentLatLng

    private val _startARide = MutableLiveData<Boolean>(false)
    val startARide: LiveData<Boolean> get() = _startARide

    private val _signType = MutableLiveData<VehicleType>()
    val signType: LiveData<VehicleType> = _signType

    fun setMarkers(markerOptionsList: List<MarkerInfo>) {
        _markers.value = markerOptionsList
    }

    fun setNearbyMarkers(markers: List<MarkerInfo>) {
        _nearbyMarkers.value = markers
    }

    fun setMarkersByVehicleType(vehicleType: VehicleType) {
        _markers.value = when(vehicleType) {
            VehicleType.CAR -> {
                _signType.value = VehicleType.CAR
                _nearbyMarkers.value?.filter { it.vehicleType == VehicleType.CAR.value }
            }
            VehicleType.MOTORCYCLE -> {
                _signType.value = VehicleType.MOTORCYCLE
                _nearbyMarkers.value
            }
        }
    }

    fun filterSignByType(list: MutableList<MarkerInfo>, vehicleType: VehicleType): List<MarkerInfo>? {
        return  when(vehicleType) {
            VehicleType.CAR -> {
                list.filter { it.vehicleType == VehicleType.CAR.value }
            }

            VehicleType.MOTORCYCLE -> {
                list
            }
        }
    }

    fun setIncomingMarkers(markerOptionsList: List<MarkerInfo>) {
        _incomingMarkers.value = markerOptionsList
    }

    fun setCacheStartingPosition(position: LatLng) {
        _cacheStartingPositon.postValue(position)
    }

    fun setCurrentDestination(destination: LatLng) {
        _currentDestination.postValue(destination)
    }

    fun setCurrentDestinationName(destName: String) {
        _currentDestinationName.postValue(destName)
    }

    fun setCurrentSpeed(speed: String) {
        _currentSpeed.postValue(speed)
    }

    fun setCurrentDirection(directionsRoute: DirectionsRoute) {
        _currentDirection.value = directionsRoute
    }

    fun setStartARide(boolean: Boolean) {
        _startARide.value = boolean
    }

    fun setAllMarkers(markerOptionsList: List<MarkerInfo>) {
        _allMarkers.value = markerOptionsList
    }

    fun setAllMarkersUntouched(markerOptionsList: List<MarkerInfo>) {
        _allMarkersUntouched.value = markerOptionsList
    }

    fun setCurrentUser(user: UserItem) {
        _currentUser.value = user
    }

    fun setCsvDataFromFirestore(csvData: List<CsvData>) {
        _csvDataFromFirestore?.value = csvData
    }

    fun setCsvDataState(state: CsvDataState){
        _csvDataState.value = state
    }

    fun setCurrentLatLng(latLng: LatLng) {
        _currentLatLng.value = latLng
    }

    fun getCurrentUser(): UserItem? = currentUser.value

    fun retrieveMarkersFromFirestore() {

    }

}

enum class VehicleType(val value: String) {
    CAR("Regular"),
    MOTORCYCLE("Motorcycle")
}

sealed class CsvDataState {
    object onLoad: CsvDataState()
    data class onSuccess(val list: List<CsvData>): CsvDataState()
    data class onFailure(val e: Exception): CsvDataState()

}