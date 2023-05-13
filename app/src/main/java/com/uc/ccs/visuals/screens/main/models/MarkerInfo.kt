package com.uc.ccs.visuals.screens.main.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng

data class MarkerInfo(
    val position: LatLng,
    val distance: Double,
    val icon: BitmapDescriptor?,
    val isWithinRadius: Boolean
)