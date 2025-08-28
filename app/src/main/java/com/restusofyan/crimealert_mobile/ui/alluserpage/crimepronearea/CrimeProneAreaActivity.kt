package com.restusofyan.crimealert_mobile.ui.alluserpage.crimepronearea

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.gms.maps.model.*
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

@AndroidEntryPoint
class CrimeProneAreaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: ActivityCrimeProneAreaBinding
    private var googleMap: GoogleMap? = null
    private var heatmapOverlay: TileOverlay? = null
    private var crimeReports: List<ListReportsItem> = emptyList()
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupBottomSheet()
        setupButton()
        setupMap()
        checkLocationPermission()
        loadCrimeData()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(
                        this,
                        "Location permission denied. Using default location.",
                        Toast.LENGTH_SHORT
                    ).show()
                    setDefaultLocation()
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        moveToCurrentLocation()
                    } else {
                        setDefaultLocation()
                    }
                }
                .addOnFailureListener {
                    setDefaultLocation()
                }
        } else {
            setDefaultLocation()
        }
    }

    private fun setDefaultLocation() {
        currentLocation = LatLng(-7.7956, 110.3695) // Yogyakarta
        moveToCurrentLocation()
    }

    private fun setupButton() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setupBottomSheet() {
        binding.bottomSheet.bringToFront()
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            peekHeight = 0
            isDraggable = true
        }
    }

    private fun loadCrimeData() {
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "Authentication token not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = repository.getAllReports(token)
                if (response.isSuccessful) {
                    response.body()?.let { crimeResponse ->
                        crimeResponse.listReports?.filterNotNull()?.let { reports ->
                            crimeReports = reports
                            setupMapWithCrimeData()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@CrimeProneAreaActivity,
                        "Failed to load crime data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CrimeProneAreaActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        }

        moveToCurrentLocation()
        setupMapWithCrimeData()
    }

    private fun setupMapWithCrimeData() {
        googleMap?.let {
            heatmapOverlay?.remove()
            if (crimeReports.isNotEmpty()) {

                setupCrimeMarkers()
                setupCrimeZones() // <== tambahkan zona rawan
                setupMarkerClickListener()
            }
        }
    }

    private fun createClusterGradient(): com.google.maps.android.heatmaps.Gradient {
        val colors = intArrayOf(
            android.graphics.Color.argb(150, 255, 255, 0),
            android.graphics.Color.argb(255, 255, 0, 0)
        )
        val startPoints = floatArrayOf(0.3f, 1.0f)
        return com.google.maps.android.heatmaps.Gradient(colors, startPoints)
    }

    private fun setupCrimeMarkers() {
        crimeReports.forEach { report ->
            report.map?.let { mapData ->
                val location = LatLng(mapData.latitude ?: 0.0, mapData.longitude ?: 0.0)
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(report.title ?: "Crime Incident")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )?.tag = listOf(report)
            }
        }
    }

    private fun setupCrimeZones() {
        val usedReports = mutableSetOf<ListReportsItem>()
        val heatmapPoints = mutableListOf<WeightedLatLng>()

        crimeReports.forEach { report ->
            if (usedReports.contains(report)) return@forEach

            report.map?.let { mapData ->
                val location = LatLng(mapData.latitude ?: 0.0, mapData.longitude ?: 0.0)

                val nearbyReports = crimeReports.filter { other ->
                    other.map?.let {
                        val otherLoc = LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0)
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            location.latitude, location.longitude,
                            otherLoc.latitude, otherLoc.longitude,
                            results
                        )
                        results[0] <= 500
                    } ?: false
                }

                val count = nearbyReports.size
                if (count >= 3) { // minimal 3 untuk dianggap cluster
                    // hitung centroid cluster
                    val avgLat = nearbyReports.mapNotNull { it.map?.latitude }.average()
                    val avgLng = nearbyReports.mapNotNull { it.map?.longitude }.average()
                    val center = LatLng(avgLat, avgLng)

                    // tampilkan lingkaran zona rawan
                    googleMap?.addCircle(
                        CircleOptions()
                            .center(center)
                            .radius(500.0)
                            .strokeColor(android.graphics.Color.RED)
                            .fillColor(android.graphics.Color.argb(70, 255, 0, 0))
                            .strokeWidth(3f)
                    )

                    // tambahkan titik untuk heatmap sesuai kategori
                    val weight = when {
                        count >= 5 -> 5.0  // merah kuat
                        count >= 3 -> 3.0  // kuning
                        else -> 0.0
                    }
                    if (weight > 0.0) {
                        heatmapPoints.add(WeightedLatLng(center, weight))
                    }

                    usedReports.addAll(nearbyReports)
                }
            }
        }

        if (heatmapPoints.isNotEmpty()) {
            val provider = HeatmapTileProvider.Builder()
                .weightedData(heatmapPoints)
                .radius(40)
                .maxIntensity(5.0)
                .gradient(createClusterGradient())
                .build()

            heatmapOverlay = googleMap?.addTileOverlay(TileOverlayOptions().tileProvider(provider))
        }
    }



    private fun moveToCurrentLocation() {
        googleMap?.let { map ->
            currentLocation?.let { location ->
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

    private fun displayBottomSheetForReports(
        reports: List<ListReportsItem>,
        location: LatLng
    ) {
        val mainReport = reports.first()

        binding.tvTitle.text = mainReport.title
        binding.tvDescription.text = mainReport.description
        binding.tvNewsTimestamp.text = formatTimestampToGMT7(mainReport.createdAt)
        binding.tvDate.text = formatDateToGMT7(mainReport.createdAt)

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

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.root.setOnClickListener {
            openInGoogleMaps(location)
        }
    }

    private fun openInGoogleMaps(location: LatLng) {
        AlertDialog.Builder(this)
            .setTitle("Buka Google Maps")
            .setMessage("Apakah Anda ingin membuka lokasi ini di Google Maps?")
            .setPositiveButton("Ya") { _, _ ->
                val gmmIntentUri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Crime Incident)")
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
            "--:--"
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
            "--/--/----"
        }
    }
}
