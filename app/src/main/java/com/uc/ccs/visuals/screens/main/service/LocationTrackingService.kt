package com.uc.ccs.visuals.screens.main.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.uc.ccs.visuals.MapsActivity
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.screens.main.CAMERA_ZOOM
import com.uc.ccs.visuals.screens.main.DISTANCE_RADIUS
import com.uc.ccs.visuals.screens.main.MapViewModel
import com.uc.ccs.visuals.screens.main.models.MarkerInfo

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var locationUpdateListener: LocationUpdateListener? = null

    private val binder = LocalBinder()
    private var isServiceRunning = false

    private val notifiedMarkers: MutableSet<String> = mutableSetOf()

    inner class LocalBinder : Binder() {
        fun getService(): LocationTrackingService = this@LocationTrackingService
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning = true
        startForegroundService()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        locationUpdateListener = null
        stopLocationUpdates()
    }

    fun setLocationUpdateListener(listener: LocationUpdateListener) {
        locationUpdateListener = listener
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(10000).apply {
            setIntervalMillis(5000)
            setMinUpdateDistanceMeters(DISTANCE_RADIUS.toFloat())
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
    }

    private fun setupRequestLocationUpdates(locationCallback: LocationCallback) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized)
            fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = Color.RED
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, MapsActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Service is running...")
            .setSmallIcon(R.drawable.ic_map_mark)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_car_warning))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    val markerMessage = locationUpdateListener?.getMarkerMessageByLocation(latLng)
                    markerMessage?.let { message ->
                        val markerKey = message.title
                        if (!notifiedMarkers.contains(markerKey)) {
                            notifiedMarkers.add(markerKey)

                            val contentText = notification.apply {
                                setContentTitle(message.title)
                                setContentText(message.description)
                            }

                            notificationManager.notify(NOTIFICATION_ID, contentText.build())
                        }
                    }
                }
            }
        }

        setupRequestLocationUpdates(locationCallback)
        startForeground(NOTIFICATION_ID, notification.build())
    }

    companion object {
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"
        private const val CHANNEL_DESCRIPTION = "Foreground service channel for location tracking"
        private const val NOTIFICATION_ID = 1
    }

    interface LocationUpdateListener {
        fun onLocationUpdate(location: Location)
        fun getViewModel(): MapViewModel
        fun speakOut(message: String)
        fun getMarkerMessageByLocation(latLng: LatLng): NotificationContent?
        fun checkPermission(): Boolean
    }

    data class NotificationContent(var title:String, var description: String)
}
