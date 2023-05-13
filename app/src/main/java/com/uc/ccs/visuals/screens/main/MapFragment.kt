package com.uc.ccs.visuals.screens.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.uc.ccs.visuals.models.MarkerInfo

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private lateinit var binding: FragmentMapBinding

    private lateinit var viewModel: MapViewModel
    private lateinit var mapFragment: SupportMapFragment

    private var counter = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Places.initialize(requireContext(), resources.getString(R.string.google_map_api_key));

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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 12f))
            }

            override fun onError(status: Status) {
                Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })

        childFragmentManager.beginTransaction()
            .replace(R.id.cl_actv_container, autocompleteFragment)
            .commit()

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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, CAMERA_ZOOM))
                    addMarksToMap(currentLatLng)
                }
            }

            mMap.setOnMarkerClickListener { marker ->
                val customInfo = marker.title
                Log.d("qweqwe", "onMarkerClick: $customInfo")
                if (customInfo != null) {
                    binding.bottomNavView.selectedItemId = R.id.menu_notif
                }

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

    private fun addMarksToMap(currentLatLng: LatLng) {
        mMap.clear()
        val nearbyDistance5Meters = 5.0 // in meters
        val nearbyDistance10Meters = 10.0 // in meters

        val marks5Meters = generateMarksInDistance(currentLatLng, nearbyDistance5Meters, count = 3)
        val marks10Meters = generateMarksInDistance(currentLatLng, nearbyDistance10Meters, count = 1)

        val markers = mutableListOf<MarkerOptions>()
        markers.addAll(marks5Meters)
        markers.addAll(marks10Meters)

        val filteredMarkers = filterMarkersByRadius(currentLatLng, markers, nearbyDistance5Meters)

        filteredMarkers.forEach { markerOptions ->
            mMap.addMarker(markerOptions)
        }
    }

    private fun filterMarkersByRadius(
        currentLatLng: LatLng,
        markers: List<MarkerOptions>,
        radius: Double
    ): List<MarkerOptions> {
        val filteredMarkers = mutableListOf<MarkerOptions>()
        val markerInfos = markers.map { marker ->
            val position = marker.position
            val distance = calculateDistance(currentLatLng, position)
            val isWithinRadius = distance <= radius
            MarkerInfo(position, distance, marker.icon, isWithinRadius)
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
                createMarkerOption(MarkerInfo(averageLocation, 0.0, null, true))
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

        return filteredMarkers
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
            .icon(icon ?: markerInfo.icon)
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

    private fun generateMarksInDistance(
        currentLatLng: LatLng,
        distance: Double,
        icon: BitmapDescriptor? = null,
        count: Int
    ): List<MarkerOptions> {
        val markerOptionsList = mutableListOf<MarkerOptions>()

        for (i in 0 until count) {
            val offsetLatLng = calculateOffset(currentLatLng, distance, i * 360.0 / count)
            val markerOptions = MarkerOptions()
                .position(offsetLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker())

            markerOptionsList.add(markerOptions)
        }

        return markerOptionsList
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

}

const val CAMERA_ZOOM = 12f
const val ON_BACK_PRESSED_LIMIT_TO_FINISH = 1