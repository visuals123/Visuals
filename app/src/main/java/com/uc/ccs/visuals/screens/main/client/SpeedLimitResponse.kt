package com.uc.ccs.visuals.screens.main.client

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.uc.ccs.visuals.R
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

data class SpeedLimitResponse(
    @SerializedName("speedLimits") val speedLimits: List<SpeedLimit>
)

data class SpeedLimit(
    @SerializedName("speedLimit") val speedLimit: Double
)

class RoadsAPIClient(private val context: Context) {

    private val apiKey = context.getString(R.string.google_map_api_key)

    suspend fun getSpeedLimit(latLng: LatLng): Double? {
        val url = "https://roads.googleapis.com/v1/snapToRoads?path=${latLng.latitude},${latLng.longitude}&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val speedLimitResponse = Gson().fromJson(responseBody, SpeedLimitResponse::class.java)
                if (speedLimitResponse.speedLimits.isNotEmpty()) {
                    val speedLimit = speedLimitResponse.speedLimits.first().speedLimit
                    return speedLimit
                }
            }
        } catch (e: Exception) {
            Log.e("SpeedLimit", "Error getting speed limit", e)
        }

        return null
    }
}