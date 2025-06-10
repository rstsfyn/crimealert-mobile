package com.restusofyan.crimealert_mobile.ui.alluserpage.detailinsidens

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.ActivityDetailInsidensBinding
import com.restusofyan.crimealert_mobile.ui.alluserpage.validateinsidens.ValidateInsidensActivity
import java.text.SimpleDateFormat
import java.util.*

class DetailInsidensActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDetailInsidensBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var googleMap: GoogleMap? = null
    private var caseLocation: LatLng? = null
    private var incidentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailInsidensBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomSheet.bringToFront()

        incidentId = intent.getStringExtra("incident_id")

        setupBottomSheet()
        setupButton()
        setupDetailInsiden()
        setupMap()
    }

    private fun setupButton() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnValidateIncident?.setOnClickListener {
            navigateToValidateIncident()
        }
    }

    private fun navigateToValidateIncident() {
        incidentId?.let { id ->
            val intent = Intent(this, ValidateInsidensActivity::class.java).apply {
                putExtra("incident_id", id)
            }
            startActivity(intent)
            finish()
        } ?: run {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Unable to validate this incident. Incident ID is missing.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        updatedInsidensLocation()
        setupMarkerClickListener()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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

    private fun setupDetailInsiden() {
        val rawTimestamp = intent.getStringExtra("incident_time")
        binding.tvNewsTimestamp.text = formatTimestampToGMT7(rawTimestamp)

        val rawDate = intent.getStringExtra("incident_date")
        binding.tvDate.text = formatDateToGMT7(rawDate)

        var avatarReporter = intent.getStringExtra("incident_reporteravatar")
        if (!avatarReporter.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatarReporter)
                .placeholder(R.drawable.bg_photoreport)
                .error(R.drawable.bg_photoreport)
                .into(binding.ivReporterAvatar)
        } else {
            binding.ivReporterAvatar.setImageResource(R.drawable.bg_photoreport)
        }

        binding.tvReporterName.text = intent.getStringExtra("incident_reportername")
    }

    private fun updatedInsidensLocation() {
        val latitude = intent.getDoubleExtra("incident_latitude", 0.0)
        val longitude = intent.getDoubleExtra("incident_longitude", 0.0)
        caseLocation = LatLng(latitude, longitude)

        googleMap?.apply {
            addMarker(
                MarkerOptions()
                    .position(caseLocation!!)
                    .title("Lokasi Kasus")
            )
            moveCamera(CameraUpdateFactory.newLatLngZoom(caseLocation!!, 14f))
        }
    }

    private fun setupMarkerClickListener() {
        googleMap?.setOnMarkerClickListener { marker ->
            caseLocation?.let {
                AlertDialog.Builder(this)
                    .setTitle("Buka Google Maps")
                    .setMessage("Apakah Anda ingin membuka lokasi kasus ini di Google Maps?")
                    .setPositiveButton("Ya") { _, _ ->
                        val gmmIntentUri = Uri.parse("geo:${it.latitude},${it.longitude}?q=${it.latitude},${it.longitude}(Lokasi Kasus)")
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
            true
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            peekHeight = 200
            isDraggable = true
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Optional: handle state changes
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Optional: handle slide offset
            }
        })
    }
}