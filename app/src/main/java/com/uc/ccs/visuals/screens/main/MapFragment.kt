package com.uc.ccs.visuals.screens.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentMapBinding
import com.uc.ccs.visuals.screens.utils.dpToPx

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
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
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
            }
        }
    }

}

const val CAMERA_ZOOM = 12f
const val ON_BACK_PRESSED_LIMIT_TO_FINISH = 1