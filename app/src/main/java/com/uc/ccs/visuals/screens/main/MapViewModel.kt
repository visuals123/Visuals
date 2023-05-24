package com.uc.ccs.visuals.screens.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.main.models.MarkerInfo
import com.uc.ccs.visuals.screens.settings.CsvData

class MapViewModel: ViewModel() {

    private val _markers = MutableLiveData<List<MarkerInfo>>()
    val markers: LiveData<List<MarkerInfo>> = _markers

    private val _csvDataFromFirestore = MutableLiveData<List<CsvData>>()
    val csvDataFromFirestore: LiveData<List<CsvData>> get() = _csvDataFromFirestore

    private val _csvDataState = MutableLiveData<CsvDataState>()
    val csvDataState: LiveData<CsvDataState> get() = _csvDataState

    private val _currentUser = MutableLiveData<UserItem>()
    val currentUser get() = _currentUser

    private val _currentLatLng = MutableLiveData<LatLng>()
    val currentLatLng: LiveData<LatLng> get() = _currentLatLng

    fun setMarkers(markerOptionsList: List<MarkerInfo>) {
        _markers.value = markerOptionsList
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

sealed class CsvDataState {
    object onLoad: CsvDataState()
    data class onSuccess(val list: List<CsvData>): CsvDataState()
    data class onFailure(val e: Exception): CsvDataState()

}