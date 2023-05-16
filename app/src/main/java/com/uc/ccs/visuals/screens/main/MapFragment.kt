package com.uc.ccs.visuals.screens.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentMapBinding
import com.uc.ccs.visuals.screens.main.adapter.ViewPagerAdapter
import com.uc.ccs.visuals.screens.main.models.MarkerInfo
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback, TextToSpeech.OnInitListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private lateinit var binding: FragmentMapBinding

    private lateinit var viewModel: MapViewModel
    private lateinit var mapFragment: SupportMapFragment

    private var tts: TextToSpeech? = null

    private var counter = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Places.initialize(requireContext(), resources.getString(R.string.google_map_api_key))

        viewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        binding = FragmentMapBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.onCreate(savedInstanceState)
        mapFragment.getMapAsync(this)

        binding.bottomNavView.background = null

        // Initialize AutocompleteSupportFragment
        autocompleteFragment = AutocompleteSupportFragment.newInstance()
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, CAMERA_ZOOM))
            }

            override fun onError(status: Status) {
                Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })

        childFragmentManager.beginTransaction()
            .replace(R.id.cl_actv_container, autocompleteFragment)
            .commit()

        tts = TextToSpeech(requireContext(), this)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        childFragmentManager.beginTransaction()
            .replace(R.id.cl_actv_container, autocompleteFragment)
            .commit()

        // Adding a callback on back pressed to replace the standard up navigation with popBackStack
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    counter++
                    if(counter > ON_BACK_PRESSED_LIMIT_TO_FINISH)
                        requireActivity().finish()
                }
            }
        )

       binding.setupViews()
    }

    private fun FragmentMapBinding.setupViews() {
        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    true
                }
                R.id.menu_notif -> {
                    findNavController().navigate(R.id.action_mapFragment_to_primaryChangeAccountDialogFragment)
                    true
                }
                else -> {
                    findNavController().navigate(R.id.action_mapFragment_to_settingsDialogFragment)
                    true
                }
            }
        }

        fabMyLocation.setOnClickListener {
            enableMyLocation()
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, CAMERA_ZOOM_DEFAULT))
                    addMarksToMap(currentLatLng)
                }
            }

            mMap.setOnMarkerClickListener { marker ->

                /**
                 * will use once we need to trap the on markers clicked
                 *
                 * val customInfo = marker.title
                    if (customInfo != null) {
                        binding.bottomNavView.selectedItemId = R.id.menu_notif
                    }
                */

                binding.bottomNavView.selectedItemId = R.id.menu_notif

                true
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, CAMERA_ZOOM))
                addMarksToMap(currentLatLng)
            }
        }
    }

    private fun setupViewPager() {
        with(binding) {
            viewModel.markers.observe(viewLifecycleOwner) { markers ->
                val withinRadius = markers?.filter { it.isWithinRadius }?.sortedBy { it.isWithinRadius } ?: return@observe
                val adapter = ViewPagerAdapter(requireContext(),withinRadius ) {
                    clViewpagerContainer.isVisible = false
                }

                viewPager.adapter = adapter

                val hasMultipleMarker = withinRadius.size > 1
                if (hasMultipleMarker) {
                    speakOut(NotificationMessage.getRandomMessageForMultipleSigns("5"))
                } else {
                    speakOut(NotificationMessage.getRandomMessageForSingleSign("10","Stop"))
                }
                clViewpagerContainer.isVisible = hasMultipleMarker
                circleIndicator.isVisible = hasMultipleMarker
                circleIndicator.setViewPager(viewPager)
            }
        }
    }

    private fun addMarksToMap(currentLatLng: LatLng) {
        mMap.clear()
        val nearbyDistance5Meters = 5.0 // in meters
        val nearbyDistance10Meters = 10.0 // in meters

        val markerOptionsList = mutableListOf<MarkerOptions>()

        val markerInfos5Meters = generateMarkerInfosInDistance(currentLatLng, nearbyDistance5Meters, count = 3)
        val markerInfos10Meters = generateMarkerInfosInDistance(currentLatLng, nearbyDistance10Meters, count = 3)

        val markerInfos = mutableListOf<MarkerInfo>()
        markerInfos.addAll(markerInfos5Meters)
        markerInfos.addAll(markerInfos10Meters)

        markerInfos.map {
            val markerOptions = MarkerOptions()
                .position(it.position)
                .icon(it.iconBitmapDescriptor)

            markerOptionsList.add(markerOptions)
        }

        val (filteredMarkers,updatedMarkersInfo) = filterMarkersByRadius(currentLatLng, markerOptionsList, nearbyDistance5Meters)

        filteredMarkers.forEach { markerInfo ->
            val markerOptions = MarkerOptions()
                .position(markerInfo.position)
                .icon(markerInfo.icon)
            mMap.addMarker(markerOptions)
        }

        viewModel.setMarkers(updatedMarkersInfo)
        setupViewPager()
    }

    private fun filterMarkersByRadius(
        currentLatLng: LatLng,
        markers: List<MarkerOptions>,
        radius: Double
    ): Pair<List<MarkerOptions>,List<MarkerInfo>> {
        val filteredMarkers = mutableListOf<MarkerOptions>()
        val markerInfos: List<MarkerInfo> = markers.map { marker ->
            val position = marker.position
            val distance = calculateDistance(currentLatLng, position)
            val isWithinRadius = distance <= radius
            MarkerInfo(
                title = "Sample Title",
                position = position,
                distance = distance,
                iconBitmapDescriptor = marker.icon,
                isWithinRadius = isWithinRadius
            )
        }

        val markersWithinRadius = markerInfos.filter { it.isWithinRadius }

        if (markersWithinRadius.size == 1) {
            filteredMarkers.add(
                createMarkerOption(markersWithinRadius.first())
                    .title("within")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        } else if (markersWithinRadius.size > 1) {
            val averageLocation = calculateAverageLocation(markersWithinRadius.map { it.position })
            filteredMarkers.add(
                createMarkerOption(
                    MarkerInfo(
                        title = "Sample Title",
                        position = averageLocation,
                        distance = 0.0,
                        iconBitmapDescriptor = null,
                        isWithinRadius = true
                    )
                )
                    .title("within")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        }

        markerInfos.forEach { markerInfo ->
            if (!markerInfo.isWithinRadius) {
                filteredMarkers.add(
                    createMarkerOption(markerInfo)
                        .title("not within")
                )
            }
        }

        return Pair(filteredMarkers,markerInfos)
    }


    private fun calculateAverageLocation(positions: List<LatLng>): LatLng {
        val totalLat = positions.sumByDouble { it.latitude }
        val totalLng = positions.sumByDouble { it.longitude }
        val averageLat = totalLat / positions.size
        val averageLng = totalLng / positions.size
        return LatLng(averageLat, averageLng)
    }

    private fun createMarkerOption(markerInfo: MarkerInfo, icon: BitmapDescriptor? = null): MarkerOptions {
        return MarkerOptions()
            .position(markerInfo.position)
            .icon(icon ?: markerInfo.iconBitmapDescriptor)
    }

    private fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
        val radiusEarth = 6371e3 // Earth's radius in meters

        val lat1 = Math.toRadians(latLng1.latitude)
        val lng1 = Math.toRadians(latLng1.longitude)
        val lat2 = Math.toRadians(latLng2.latitude)
        val lng2 = Math.toRadians(latLng2.longitude)

        val deltaLat = lat2 - lat1
        val deltaLng = lng2 - lng1

        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return radiusEarth * c
    }

    private fun generateMarkerInfosInDistance(
        currentLatLng: LatLng,
        distance: Double,
        icon: BitmapDescriptor? = null,
        count: Int
    ): List<MarkerInfo> {
        val markerInfoList = mutableListOf<MarkerInfo>()

        for (i in 0 until count) {
            val offsetLatLng = calculateOffset(currentLatLng, distance, i * 360.0 / count)
            val markerInfo = MarkerInfo(
                title = "", // Set the title as needed
                position = offsetLatLng,
                distance = distance,
                iconBitmapDescriptor = BitmapDescriptorFactory.defaultMarker(),
                isWithinRadius = false // Set the value based on your criteria
            )

            markerInfoList.add(markerInfo)
        }

        return markerInfoList
    }

    private fun calculateOffset(latLng: LatLng, distance: Double, angle: Double): LatLng {
        val radiusEarth = 6371e3 // Earth's radius in meters

        val latRadians = Math.toRadians(latLng.latitude)
        val lngRadians = Math.toRadians(latLng.longitude)
        val angularDistance = distance / radiusEarth

        val latOffset = Math.asin(
            Math.sin(latRadians) * Math.cos(angularDistance) +
                    Math.cos(latRadians) * Math.sin(angularDistance) * Math.cos(Math.toRadians(angle))
        )

        val lngOffset = lngRadians + Math.atan2(
            Math.sin(Math.toRadians(angle)) * Math.sin(angularDistance) * Math.cos(latRadians),
            Math.cos(angularDistance) - Math.sin(latRadians) * Math.sin(latOffset)
        )

        val offsetLatDegrees = Math.toDegrees(latOffset)
        val offsetLngDegrees = Math.toDegrees(lngOffset)

        return LatLng(offsetLatDegrees, offsetLngDegrees)
    }

    private fun speakOut(message: String) {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String?) {
                // TTS is done speaking
                // You can perform any desired action here
            }

            override fun onError(utteranceId: String?) {
                // Error occurred while speaking, handle the error
            }

            override fun onStart(utteranceId: String?) {
                // TTS started speaking
                // You can perform any desired action here
            }
        })
        tts!!.speak(message, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    enableMyLocation()
                    onMapReady(mMap)
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(TtsLanguage.US_ENGLISH.locale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("tts","The Language not supported!")
            } else {
                Log.e("tts","The Language supported!")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
    }

}

enum class TtsLanguage(val locale: Locale) {
    FILIPINO(Locale("fil", "PH")),
    US_ENGLISH(Locale("en", "US"))
}

enum class NotificationMessage(val template: String) {
    CAUTION("Caution: There's a multiple road sign distance meters away, please stay alert."),
    ATTENTION("Attention: Multiple road sign is distance meters ahead, watch out!"),
    ALERT("Alert: Be prepared for a multiple road sign distance meters nearby. Take necessary action."),
    NOTICE("Notice: Look out for a multiple road sign distance meters from your location. Ensure you're following the instructions."),
    WARNING("Attention: There are multiple road signs located distance meters ahead. Please stay alert and pay close attention to these signs as they contain important information. You can find more details on the screen below."),
    SIGN1("Road Sign: signName is distance meters away. Please pay attention."),
    SIGN2("Road Sign: There's a signName road sign distance meters ahead. Take note of any instructions."),
    SIGN3("Road Sign: Look out for a signName road sign distance meters from your location. Follow the indicated directions.");

    companion object {
        private val singleSignMessages = listOf(SIGN1, SIGN2, SIGN3)
        private val multipleSignMessages = listOf(CAUTION, ATTENTION, ALERT, NOTICE, WARNING)

        fun getRandomMessageForSingleSign(distance: String, signName: String): String {
            val randomMessage = singleSignMessages.random()
            return randomMessage.template.replace("distance", distance).replace("signName", signName)
        }

        fun getRandomMessageForMultipleSigns(distance: String): String {
            val randomMessage = multipleSignMessages.random()
            return randomMessage.template.replace("distance", distance)
        }
    }
}


const val CAMERA_ZOOM = 20f
const val CAMERA_ZOOM_DEFAULT = 12f
const val ON_BACK_PRESSED_LIMIT_TO_FINISH = 1