package com.uc.ccs.visuals.screens.main.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.uc.ccs.visuals.screens.main.VehicleType

data class MarkerInfo(
    val id: String,
    val code: String,
    val title: String,
    val position: LatLng,
    val distance: Double? = null,
    val iconBitmapDescriptor: BitmapDescriptor? = null,
    val iconImageUrl: String? = null,
    val description: String? = null,
    val vehicleType: String,
    val isWithinRadius: Boolean
)