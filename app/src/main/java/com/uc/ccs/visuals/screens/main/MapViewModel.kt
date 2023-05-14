package com.uc.ccs.visuals.screens.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uc.ccs.visuals.screens.main.models.MarkerInfo

class MapViewModel : ViewModel() {

    private val _markers = MutableLiveData<List<MarkerInfo>>()
    val markers: LiveData<List<MarkerInfo>> = _markers

    fun setMarkers(markerOptionsList: List<MarkerInfo>) {
        _markers?.value = markerOptionsList
    }

}