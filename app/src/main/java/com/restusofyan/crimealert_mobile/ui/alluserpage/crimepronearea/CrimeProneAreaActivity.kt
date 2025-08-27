package com.restusofyan.crimealert_mobile.ui.alluserpage.crimepronearea

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.data.response.casesreports.ListReportsItem
import com.restusofyan.crimealert_mobile.databinding.ActivityCrimeProneAreaBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@AndroidEntryPoint
class CrimeProneAreaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: ActivityCrimeProneAreaBinding
    private var googleMap: GoogleMap? = null
    private var heatmapOverlay: TileOverlay? = null
    private var crimeReports: List<ListReportsItem> = emptyList()
    private var crimeLocationGroups: Map<LatLng, List<ListReportsItem>> = emptyMap()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: LatLng? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TAG = "CrimeProneAreaActivity"
    }

    @Inject
    lateinit var repository: CrimeAlertRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrimeProneAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Activity created, initializing components...")


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupBottomSheet()
        setupButton()
        setupMap()
        checkLocationPermission()
        loadCrimeData()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Location permission denied. Using default location.", Toast.LENGTH_SHORT).show()
                    setDefaultLocation()
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        Log.d(TAG, "Current location: ${currentLocation}")
                        moveToCurrentLocation()
                    } else {
                        Toast.makeText(this, "Unable to get current location. Using default.", Toast.LENGTH_SHORT).show()
                        setDefaultLocation()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting location", exception)
                    Toast.makeText(this, "Error getting location. Using default.", Toast.LENGTH_SHORT).show()
                    setDefaultLocation()
                }
        } else {
            setDefaultLocation()
        }
    }

    private fun setDefaultLocation() {
        // Fallback to Yogyakarta if GPS fails
        currentLocation = LatLng(-7.7956, 110.3695)
        Log.d(TAG, "Using default location: ${currentLocation}")
        moveToCurrentLocation()
    }

    private fun setupButton() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupMap() {
        Log.d(TAG, "Setting up map...")
        try {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
            if (mapFragment != null) {
                mapFragment.getMapAsync(this)
                Log.d(TAG, "Map fragment found, requesting map...")
            } else {
                Log.e(TAG, "Map fragment not found! Check if R.id.map_fragment exists in layout")
                Toast.makeText(this, "Map fragment not found. Please check layout.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map", e)
            Toast.makeText(this, "Error setting up map: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBottomSheet() {
        binding.bottomSheet.bringToFront()
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            peekHeight = 0
            isDraggable = true
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d(TAG, "Bottom sheet state changed: $newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Optional: handle slide offset
            }
        })
    }

    private fun loadCrimeData() {
        // Get token from SharedPreferences with correct key
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        Log.d(TAG, "Loading crime data with token: ${if(token.isNotEmpty()) "Token found" else "No token"}")

        if (token.isEmpty()) {
            Toast.makeText(this, "Authentication token not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Fetching reports from API...")
                val response = repository.getAllReports(token)
                Log.d(TAG, "API response: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    response.body()?.let { crimeResponse ->
                        crimeResponse.listReports?.filterNotNull()?.let { reports ->
                            Log.d(TAG, "Found ${reports.size} crime reports")
                            crimeReports = reports
                            processCrimeData()
                        }
                    }
                } else {
                    Log.e(TAG, "API request failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@CrimeProneAreaActivity,
                        "Failed to load crime data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading crime data", e)
                Toast.makeText(this@CrimeProneAreaActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processCrimeData() {
        Log.d(TAG, "Processing crime data...")
        // Group crimes by location (within 100m radius)
        val locationGroups = HashMap<LatLng, MutableList<ListReportsItem>>()
        val proximityThreshold = 0.001 // approximately 100 meters

        crimeReports.forEach { report ->
            report.map?.let { mapData ->
                val crimeLocation = LatLng(mapData.latitude ?: 0.0, mapData.longitude ?: 0.0)
                Log.d(TAG, "Processing crime at: $crimeLocation")

                // Find existing nearby location group
                val existingLocation = locationGroups.keys.find { existingLoc ->
                    val distance = calculateDistance(existingLoc, crimeLocation)
                    distance < proximityThreshold
                }

                if (existingLocation != null) {
                    locationGroups[existingLocation]?.add(report)
                } else {
                    locationGroups[crimeLocation] = mutableListOf(report)
                }
            }
        }

        crimeLocationGroups = locationGroups.mapValues { it.value.toList() }
        Log.d(TAG, "Created ${crimeLocationGroups.size} location groups")
        setupMapWithCrimeData()
    }

    private fun calculateDistance(loc1: LatLng, loc2: LatLng): Double {
        return Math.sqrt(
            Math.pow(loc1.latitude - loc2.latitude, 2.0) +
                    Math.pow(loc1.longitude - loc2.longitude, 2.0)
        )
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready!")
        googleMap = map

        googleMap?.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        }

        moveToCurrentLocation()
        setupMapWithCrimeData()

        Toast.makeText(this, "Map loaded successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun setupMapWithCrimeData() {
        googleMap?.let { map ->
            Log.d(TAG, "Setting up map with crime data...")
            heatmapOverlay?.remove()

            if (crimeLocationGroups.isNotEmpty()) {
                setupHeatmap()
                setupCrimeMarkers()
                setupMarkerClickListener()
                Log.d(TAG, "Crime data setup complete")
            } else {
                Log.d(TAG, "No crime data to display")
            }
        }
    }

    private fun setupHeatmap() {
        val heatmapData = mutableListOf<WeightedLatLng>()

        crimeLocationGroups.forEach { (location, reports) ->
            val weight = when {
                reports.size >= 5 -> 1.0
                reports.size >= 3 -> 0.7
                reports.size >= 2 -> 0.4
                else -> 0.2
            }
            heatmapData.add(WeightedLatLng(location, weight))
        }

        if (heatmapData.isNotEmpty()) {
            val provider = HeatmapTileProvider.Builder()
                .weightedData(heatmapData)
                .radius(50)
                .maxIntensity(1000.0)
                .gradient(createHeatmapGradient())
                .build()

            heatmapOverlay = googleMap?.addTileOverlay(
                TileOverlayOptions().tileProvider(provider)
            )
            Log.d(TAG, "Heatmap created with ${heatmapData.size} points")
        }
    }

    private fun createHeatmapGradient(): com.google.maps.android.heatmaps.Gradient {
        val colors = intArrayOf(
            android.graphics.Color.argb(0, 255, 0, 0),      // Transparent
            android.graphics.Color.argb(100, 255, 165, 0),   // Orange with transparency
            android.graphics.Color.argb(150, 255, 69, 0),    // Red-orange
            android.graphics.Color.argb(200, 255, 0, 0)      // Red
        )
        val startPoints = floatArrayOf(0.0f, 0.3f, 0.6f, 1.0f)

        return com.google.maps.android.heatmaps.Gradient(colors, startPoints)
    }

    private fun setupCrimeMarkers() {
        crimeLocationGroups.forEach { (location, reports) ->
            val markerTitle = when {
                reports.size >= 5 -> "High Crime Area (${reports.size} incidents)"
                reports.size >= 3 -> "Crime Prone Area (${reports.size} incidents)"
                else -> "Crime Incident (${reports.size} incident${if(reports.size > 1) "s" else ""})"
            }

            googleMap?.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(markerTitle)
                    .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                        com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                    ))
            )?.tag = reports
        }
        Log.d(TAG, "Added ${crimeLocationGroups.size} markers")
    }

    private fun moveToCurrentLocation() {
        googleMap?.let { map ->
            currentLocation?.let { location ->
                Log.d(TAG, "Moving camera to: $location")
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
            }
        }
    }

    private fun setupMarkerClickListener() {
        googleMap?.setOnMarkerClickListener { marker ->
            val reports = marker.tag as? List<ListReportsItem>
            if (reports != null && reports.isNotEmpty()) {
                displayBottomSheetForReports(reports, marker.position)
            }
            true
        }
    }

    private fun displayBottomSheetForReports(reports: List<ListReportsItem>, location: LatLng) {
        // Display the most recent report as the main content
        val mainReport = reports.maxByOrNull { report ->
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(report.createdAt?.replace("Z", "") ?: "") ?: Date(0)
            } catch (e: Exception) {
                Date(0)
            }
        } ?: reports.first()

        // Update bottom sheet content
        binding.tvTitle.text = if (reports.size > 1) {
            "${mainReport.title} (+${reports.size - 1} more incidents)"
        } else {
            mainReport.title
        }

        binding.tvDescription.text = mainReport.description

        val rawTimestamp = mainReport.createdAt
        binding.tvNewsTimestamp.text = formatTimestampToGMT7(rawTimestamp)
        binding.tvDate.text = formatDateToGMT7(rawTimestamp)

        // Load main report image
        val imageUrl = mainReport.picture
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.bg_photoreport)
                .error(R.drawable.bg_photoreport)
                .into(binding.ivCaseImage)
        } else {
            binding.ivCaseImage.setImageResource(R.drawable.bg_photoreport)
        }

        // Load reporter avatar
        val avatarReporter = mainReport.user?.avatar
        if (!avatarReporter.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatarReporter)
                .placeholder(R.drawable.bg_photoreport)
                .error(R.drawable.bg_photoreport)
                .into(binding.ivReporterAvatar)
        } else {
            binding.ivReporterAvatar.setImageResource(R.drawable.bg_photoreport)
        }

        binding.tvReporterName.text = mainReport.user?.name ?: "Unknown Reporter"

        // Show bottom sheet
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        // Update click listener for opening in Google Maps
        binding.root.setOnClickListener {
            openInGoogleMaps(location)
        }
    }

    private fun openInGoogleMaps(location: LatLng) {
        AlertDialog.Builder(this)
            .setTitle("Buka Google Maps")
            .setMessage("Apakah Anda ingin membuka lokasi ini di Google Maps?")
            .setPositiveButton("Ya") { _, _ ->
                val gmmIntentUri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Crime Prone Area)")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun formatTimestampToGMT7(timestamp: String?): String {
        return try {
            if (timestamp.isNullOrEmpty()) return "--:--"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("GMT+7")

            val date = inputFormat.parse(timestamp.replace("Z", ""))
            date?.let { outputFormat.format(it) } ?: "--:--"
        } catch (e: Exception) {
            val tIndex = timestamp?.indexOf('T') ?: -1
            if (tIndex != -1 && timestamp != null && timestamp.length >= tIndex + 6) {
                timestamp.substring(tIndex + 1, tIndex + 6)
            } else {
                "--:--"
            }
        }
    }

    private fun formatDateToGMT7(dateString: String?): String {
        return try {
            if (dateString.isNullOrEmpty()) return "--/--/----"
            val inputFormat = if (dateString.contains('T')) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("GMT+7")

            val cleanDateString = dateString.replace("Z", "")
            val date = inputFormat.parse(cleanDateString)
            date?.let { outputFormat.format(it) } ?: "--/--/----"
        } catch (e: Exception) {
            dateString?.substring(0, minOf(10, dateString.length)) ?: "--/--/----"
        }
    }
}