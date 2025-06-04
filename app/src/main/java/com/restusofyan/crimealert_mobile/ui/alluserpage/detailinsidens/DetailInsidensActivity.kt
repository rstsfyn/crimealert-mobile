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

    private fun setupDetailInsiden() {
        binding.tvNewsTimestamp.text = intent.getStringExtra("incident_time")?.let { raw ->
            val tIndex = raw.indexOf('T')
            if (tIndex != -1 && raw.length >= tIndex + 6) raw.substring(tIndex + 1, tIndex + 6) else "--:--"
        } ?: "--:--"
        binding.tvDate.text = intent.getStringExtra("incident_date")?.substring(0, 10) ?: "--/--/----"

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