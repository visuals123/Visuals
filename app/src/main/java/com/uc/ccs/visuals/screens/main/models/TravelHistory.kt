package com.uc.ccs.visuals.screens.main.models

import com.google.android.gms.maps.model.LatLng
import com.uc.ccs.visuals.data.LocalHistory

data class TravelHistory(
    val id: String,
    val email: String,
    val startDestinationName: String,
    val endDestinationName: String,
    val startDestinationLatLng: LatLng,
    val endDestinationLatLng: LatLng
) {
    fun toLocalHistory(): LocalHistory {
        val startLatLng = "${startDestinationLatLng.latitude},${startDestinationLatLng.longitude}"
        val endLatLng = "${endDestinationLatLng.latitude},${endDestinationLatLng.longitude}"

        return LocalHistory(
            id = id,
            email = this.email,
            startDestinationName = this.startDestinationName,
            endDestinationName = this.endDestinationName,
            startDestinationLatLng = startLatLng,
            endDestinationLatLng = endLatLng
        )
    }
}