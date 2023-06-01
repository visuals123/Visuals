package com.uc.ccs.visuals.screens.main

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.errors.ZeroResultsException
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.data.CsvDataRepository
import com.uc.ccs.visuals.databinding.FragmentMapBinding
import com.uc.ccs.visuals.factories.AdminDashboardViewModelFactory
import com.uc.ccs.visuals.screens.admin.AdminDashboardViewModel
import com.uc.ccs.visuals.screens.admin.LocalDBState
import com.uc.ccs.visuals.screens.main.adapter.ViewPagerAdapter
import com.uc.ccs.visuals.screens.main.models.MarkerInfo
import com.uc.ccs.visuals.screens.main.service.LocationTrackingService
import com.uc.ccs.visuals.utils.extensions.checkLocationPermissions
import com.uc.ccs.visuals.utils.extensions.hasLocationPermission
import com.uc.ccs.visuals.utils.extensions.requestLocationPermissions
import com.uc.ccs.visuals.utils.extensions.showConfirmationDialog
import com.uc.ccs.visuals.utils.extensions.toMarkerInfoList
import java.io.IOException
import java.util.Locale
import kotlin.math.ceil

class MapFragment : Fragment(), OnMapReadyCallback,
    TextToSpeech.OnInitListener, LocationTrackingService.LocationUpdateListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var isDirectionsMode = true

    private lateinit var binding: FragmentMapBinding

    private lateinit var viewModel: MapViewModel
    private lateinit var adminViewModel: AdminDashboardViewModel
    private lateinit var mapFragment: SupportMapFragment

    private var tts: TextToSpeech? = null

    private var counter = 0
    private var isServiceRegistered = false
    private var serviceIntent: Intent? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? LocationTrackingService.LocalBinder
            val locationTrackingService = binder?.getService()
            locationTrackingService?.setLocationUpdateListener(this@MapFragment)
            isServiceRegistered = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceRegistered = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Places.initialize(requireContext(), resources.getString(R.string.google_map_api_key))

        val repository = CsvDataRepository(requireContext())
        val viewModelFactory = AdminDashboardViewModelFactory(repository)

        adminViewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(AdminDashboardViewModel::class.java)
        viewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        binding = FragmentMapBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.onCreate(savedInstanceState)
        mapFragment.getMapAsync(this)

        binding.bottomNavView.background = null

        setupAutoComplete()

        tts = TextToSpeech(requireContext(), this)

        return binding.root
    }

    private fun setupAutoComplete() {
        autocompleteFragment = AutocompleteSupportFragment.newInstance()
        autocompleteFragment.apply {
            setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
            setCountries(listOf("PH"))
            setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    clearMap()

                    if (place.name.toString().trim().isNotBlank()) {
                        animateCamera(place.latLng, CAMERA_ZOOM)
                        addMarker(place.name, place.latLng)
                        setupDirectionButton(true, place.latLng)
                    } else {
                        setupDirectionButton(false, null)
                    }
                }

                override fun onError(status: Status) {
//                    Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                    autocompleteFragment.setText("")
                }
            })
        }
    }

    private fun clearMap() {
        if (::mMap.isInitialized) {
            mMap.clear()
        }
    }

    private fun setupDirectionButton(bool: Boolean, latLng: LatLng?) {
        with(binding) {
            with(viewModel) {
                fabDirections.apply {
                    isVisible = bool
                    setOnClickListener {
                        if (isDirectionsMode) {
                            showConfirmationDialog(
                                getString(R.string.dialog_title_start_ride),
                                getString(R.string.dialog_message_start_ride),
                                getString(R.string.dialog_positive_button_start_ride),
                                getString(R.string.dialog_negative_button_start_ride),
                                {
                                    animateFabIconChange(R.drawable.ic_close, true)
                                    if (latLng != null) {
                                        drawPathToDestination(latLng)

                                        if (checkLocationPermissions(LOCATION_PERMISSION_REQUEST_CODE)) {
                                            try {
                                                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                                    location?.let {
                                                        val currentLatLng = LatLng(
                                                            location.latitude,
                                                            location.longitude
                                                        )
                                                        animateCamera(
                                                            currentLatLng,
                                                            CAMERA_ZOOM
                                                        )
                                                        updateMapMarkers(currentLatLng)
                                                    }
                                                }
                                            } catch (securityException: SecurityException) {
                                                Log.e(
                                                    "setupDirectionButton",
                                                    "SecurityException: ${securityException.message}"
                                                )
                                            }
                                        }

                                        setupService()
                                    }
                                    isDirectionsMode = false
                                },
                                { }
                            )
                        } else {
                            showConfirmationDialog(
                                getString(R.string.dialog_title_end_ride),
                                getString(R.string.dialog_message_end_ride),
                                getString(R.string.dialog_positive_button_end_ride),
                                getString(R.string.dialog_negative_button_end_ride),
                                {
                                    animateFabIconChange(R.drawable.ic_direction, false)
                                    clearMap()
                                    unbindService()
                                    stopLocationUpdates()
                                    setMarkers(emptyList())
                                    setupViewPager(emptyList())

                                    autocompleteFragment.setText(getString(R.string.emptyString))
                                    isVisible = false
                                    isDirectionsMode = true
                                },
                                { }
                            )
                        }
                    }
                }
            }
        }
    }


    private fun animateFabIconChange(@DrawableRes newIconResId: Int, isDirectionsMode: Boolean) {
        with(binding) {
            val fadeOut = ObjectAnimator.ofFloat(fabDirections, "alpha", 1f, 0f)
            fadeOut.duration = 200
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fabDirections.setImageResource(newIconResId)
                    val backgroundColor = if (isDirectionsMode) R.color.md_red_300 else R.color.colorPrimary
                    fabDirections.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
                    fabDirections.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), backgroundColor))

                    val fadeIn = ObjectAnimator.ofFloat(fabDirections, "alpha", 0f, 1f)
                    fadeIn.duration = 200
                    fadeIn.start()
                }
            })
            fadeOut.start()
        }
    }

    private fun animateCamera(latLng: LatLng,zoom: Float) {
        if (::mMap.isInitialized) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        }
    }

    private fun addMarker(name: String?, latLng: LatLng?) {
        if (name != null && latLng != null && ::mMap.isInitialized) {
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            mMap.addMarker(markerOptions)
        }
    }

    private fun drawPathToDestination(destinationLatLng: LatLng) {
        val originLatLng = viewModel.currentLatLng.value

        val geoApiContext = GeoApiContext.Builder()
            .apiKey(getString(R.string.google_map_api_key))
            .build()

        val request: DirectionsApiRequest = DirectionsApi.newRequest(geoApiContext)
            .mode(TravelMode.DRIVING)
            .origin("${originLatLng?.latitude},${originLatLng?.longitude}")
            .destination("${destinationLatLng.latitude},${destinationLatLng.longitude}")

        try {
            val result: DirectionsResult = request.await()

            if (result.routes.isNotEmpty()) {
                val route = result.routes[0]
                val polylineOptions = PolylineOptions()
                    .addAll(route.overviewPolyline.decodePath().map {
                        LatLng(it.lat, it.lng)
                    })
                    .color(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    .width(POLYLINE_WIDTH)
                    .clickable(false)

                mMap.addPolyline(polylineOptions)
            } else {
                Toast.makeText(requireContext(), "No routes found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ZeroResultsException) {
            Log.e("Exception", "ZeroResultsException: ${e.message}")
            e.printStackTrace()
        } catch (e: ApiException) {
            Log.e("Exception", "ApiException: ${e.message}")
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e("Exception", "InterruptedException: ${e.message}")
            e.printStackTrace()
        } catch (e: IOException) {
            Log.e("Exception", "IOException: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
            .replace(R.id.cl_actv_container, autocompleteFragment)
            .commit()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    counter++
                    if(counter > ON_BACK_PRESSED_LIMIT_TO_FINISH)
                        requireActivity().finish()
                }
            }
        )

        with(binding) {
            setupObservers()
            setupViews()
        }
    }

    private fun FragmentMapBinding.setupObservers() {
        with(viewModel) {


            /**
             * [06/01/2023] Temporary Disabled: Changed Flow
             * */
            currentLatLng.observe(viewLifecycleOwner) {latLng ->
                //updateMapMarkers(latLng)
            }

            /**
             * [05/29/2023] Temporary Disabled: Currently using local db to show markers
             * */
            csvDataState.observe(viewLifecycleOwner) {state ->
                when (state) {
                    CsvDataState.onLoad -> {}
                    is CsvDataState.onSuccess -> {
                        setCsvDataFromFirestore(state.list)
                    }
                    is CsvDataState.onFailure -> {
                        Toast.makeText(requireContext(), state.e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            /**
             * [05/29/2023] Temporary Disabled: Currently using local db to show markers
             * */
            csvDataFromFirestore.observe(viewLifecycleOwner) { csvData ->
                val convertToMarkerInfoList = csvData.map { item ->
                    val latLng = item.position.split("-")
                    MarkerInfo(
                        id = item.id,
                        code = item.code,
                        title = item.title,
                        position = LatLng(latLng[0].toDouble(), latLng[1].toDouble()),
                        iconImageUrl = item.iconImageUrl,
                        description = item.description,
                        isWithinRadius = false,
                    )
                }
                setMarkers(convertToMarkerInfoList)
            }

            startARide.observe(viewLifecycleOwner) {
                if (it) {
                }
            }

            adminViewModel.insertCsvDataToLocalDbState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is LocalDBState.Success -> {
                        setAllMarkers(state.csvData.toMarkerInfoList())
                    }

                    is LocalDBState.Error -> {
                        Toast.makeText(requireContext(), state.exception.localizedMessage, Toast.LENGTH_SHORT).show()
                    }

                    LocalDBState.Loading -> {}
                }
            }

            markers.observe(viewLifecycleOwner) { markers ->
            }
        }
    }

    private fun updateMapMarkers(latLng: LatLng?) {
        with(viewModel) {
            if (::mMap.isInitialized) {

                val filterByRadius = latLng?.let {
                    filterMarkersByRadius(
                        it,
                        allMarkers.value ?: emptyList(),
                        DISTANCE_RADIUS
                    )
                }

                filterByRadius?.first?.map { markerInfo ->
                    val markerOptions = MarkerOptions()
                        .position(markerInfo.position)
                        .icon(markerInfo.icon)
                    mMap.addMarker(markerOptions)
                }

                val newMarkers = filterByRadius?.second ?: emptyList()
                setupViewPager(newMarkers)
                setMarkers(newMarkers)
            }
        }
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

        if (requireContext().hasLocationPermission()) {
            try {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = false

                // Inside your initialization/setup code
                locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    MAP_UPDATE_INTERVAL,
                ).apply {
                    setMinUpdateDistanceMeters(5f)
                    setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                    setWaitForAccurateLocation(true)
                }.build()

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        p0.lastLocation?.let { location ->
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            //viewModel.setCurrentLatLng(currentLatLng)
                            animateCamera(currentLatLng, CAMERA_ZOOM_DEFAULT)
                        }
                    }
                }

                mMap.setOnMarkerClickListener { marker ->
                    binding.bottomNavView.selectedItemId = R.id.menu_notif

                    /**
                     * will use once we need to trap the on markers clicked
                     *
                     * val customInfo = marker.title
                    if (customInfo != null) {
                    binding.bottomNavView.selectedItemId = R.id.menu_notif
                    }
                     */

                    true
                }

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

            } catch (securityException: SecurityException) {

                Log.e("onMapReady", "SecurityException: ${securityException.message}")
            }
        } else {
            requestLocationPermissions(LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
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
                animateCamera(currentLatLng, CAMERA_ZOOM)
            }
        }
    }

    private fun setupViewPager(markers: List<MarkerInfo>) {
        with(binding) {
            val withinRadius = markers.filter { it.isWithinRadius }.sortedBy { it.isWithinRadius }
            val adapter = ViewPagerAdapter(requireContext(),withinRadius ) {
                clViewpagerContainer.isVisible = false
            }

            viewPager.adapter = adapter

            val hasMultipleMarker = withinRadius.size > 1
            if (hasMultipleMarker) {
                speakOut(NotificationMessage.getRandomMessageForMultipleSigns(toDistanceString(withinRadius[0].distance)))
            } else {
                if(withinRadius.isNotEmpty()) {
                    val item = withinRadius[0]
                    speakOut("In ${toDistanceString(item.distance)}, theres a ${item.title}, ${item.description.toString()}")
                }
            }

            clViewpagerContainer.isVisible = markers.isNotEmpty()
            circleIndicator.isVisible = hasMultipleMarker
            circleIndicator.setViewPager(viewPager)
        }
    }

    private fun toDistanceString(distance: Double?) : String {
        val value = ceil(distance ?: 0.0).toInt().toString()
        return if (ceil(distance ?: 0.0).toInt() > 1) {
            value.plus(" meters")
        } else {
            value.plus(" meter")
        }
    }
    private fun filterMarkersByRadius(
        currentLatLng: LatLng,
        markers: List<MarkerInfo>,
        radius: Double
    ): Pair<List<MarkerOptions>, List<MarkerInfo>> {
        val filteredMarkers = mutableListOf<MarkerOptions>()
        val markerInfos: List<MarkerInfo> = markers.map { marker ->
            val position = marker.position
            val distance = calculateDistance(currentLatLng, position)
            val isWithinRadius = distance <= radius
            MarkerInfo(
                id = marker.id,
                title = marker.title,
                code = marker.code,
                position = position,
                description = marker.description,
                distance = distance,
                iconImageUrl = marker.iconImageUrl,
                iconBitmapDescriptor = marker.iconBitmapDescriptor,
                isWithinRadius = isWithinRadius
            )
        }

        markerInfos.forEach { markerInfo ->
            if (markerInfo.isWithinRadius) {
                filteredMarkers.add(
                    createMarkerOption(markerInfo)
                        .title("not within")
                )
            }
        }

        return Pair(filteredMarkers, markerInfos)
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

    private fun setupService() {
        serviceIntent = Intent(requireContext(), LocationTrackingService::class.java)
        requireContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent)
        }
    }

    private fun unbindService() {
        requireContext().unbindService(serviceConnection)
        requireContext().stopService(serviceIntent)
    }

    override fun speakOut(message: String) {
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

    override fun getMarkerMessageByLocation(latLng: LatLng): LocationTrackingService.NotificationContent? {
        return with(viewModel) {
            val filterByRadius = latLng?.let {
                filterMarkersByRadius(
                    it,
                    allMarkers.value ?: emptyList(),
                    DISTANCE_RADIUS
                )
            }

            filterByRadius?.first?.map { markerInfo ->
                val markerOptions = MarkerOptions()
                    .position(markerInfo.position)
                    .icon(markerInfo.icon)
                mMap.addMarker(markerOptions)
            }

            val newMarkers = filterByRadius?.second ?: emptyList()

            val withinRadius = newMarkers.filter { it.isWithinRadius }.sortedBy { it.isWithinRadius }

            val hasMultipleMarker = withinRadius.size > 1
            if (hasMultipleMarker) {
                speakOut(NotificationMessage.getRandomMessageForMultipleSigns(toDistanceString(withinRadius[0].distance)))
                LocationTrackingService.NotificationContent(
                    title = "Multiple road signs",
                    description = NotificationMessage.getRandomMessageForMultipleSigns(toDistanceString(withinRadius[0].distance))
                )
            } else {
                if(withinRadius.isNotEmpty()) {
                    val item = withinRadius[0]
                    speakOut("In ${toDistanceString(item.distance)}, theres a ${item.title}, ${item.description.toString()}")
                    LocationTrackingService.NotificationContent(
                        title = item.title,
                        description= "In ${toDistanceString(item.distance)}, theres a ${item.title}, ${item.description.toString()}"
                    )
                } else null
            }
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

    override fun getViewModel(): MapViewModel {
        return viewModel
    }

    override fun onLocationUpdate(location: Location) {
        val latlng = LatLng(location.latitude, location.longitude)
        updateMapMarkers(latlng)
    }

}

enum class TtsLanguage(val locale: Locale) {
    FILIPINO(Locale("fil", "PH")),
    US_ENGLISH(Locale("en", "US"))
}

enum class NotificationMessage(val template: String) {
    CAUTION("Caution: There's a multiple road sign distance away, please stay alert."),
    ATTENTION("Attention: Multiple road sign is distance ahead, watch out!"),
    ALERT("Alert: Be prepared for a multiple road sign distance nearby. Take necessary action."),
    NOTICE("Notice: Look out for a multiple road sign distance from your location. Ensure you're following the instructions."),
    WARNING("Attention: There are multiple road signs located distance ahead. Please stay alert and pay close attention to these signs as they contain important information. You can find more details on the screen below."),
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

const val DISTANCE_RADIUS = 3.0
const val POLYLINE_WIDTH = 10f
const val MAP_UPDATE_INTERVAL = 10000L
const val CAMERA_ZOOM = 20f
const val CAMERA_ZOOM_DEFAULT = 12f
const val CAMERA_ZOOM_DIRECTION = 16f
const val ON_BACK_PRESSED_LIMIT_TO_FINISH = 1