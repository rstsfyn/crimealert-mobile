package com.restusofyan.crimealert_mobile.ui.police.detailcasespolice

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.databinding.ActivityDetailCasesPoliceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DetailCasesPoliceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailCasesPoliceBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var googleMap: GoogleMap? = null
    private lateinit var caseLocation: LatLng
    private var reportId: Int = 0
    private var currentStatus: String = ""

    @Inject
    lateinit var repository: CrimeAlertRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCasesPoliceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomSheet.bringToFront()

        setupButton()
        setupBottomSheet()
        setupDetailCasesData()
        setupStatusSpinner()
        setupMap()
    }

    private fun setupButton() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            peekHeight = 200
            isDraggable = true
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 0) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        })
    }

    private fun setupDetailCasesData() {
        binding.tvTitle.text = intent.getStringExtra("report_title")
        binding.tvDescription.text = intent.getStringExtra("report_description")
        binding.tvDate.text = intent.getStringExtra("report_date")
        binding.tvNewsTimestamp.text = intent.getStringExtra("report_timestamp")?.let { raw ->
            val tIndex = raw.indexOf('T')
            if (tIndex != -1 && raw.length >= tIndex + 6) raw.substring(tIndex + 1, tIndex + 6) else "--:--"
        } ?: "--:--"

        // Get report ID and current status
        reportId = intent.getIntExtra("report_id", 0)
        currentStatus = intent.getStringExtra("report_status") ?: "belum_ditangani"

        var imageUrl = intent.getStringExtra("report_image_url")
        imageUrl = imageUrl?.replace("localhost", "10.0.2.2")
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.bg_photoreport)
                .error(R.drawable.bg_photoreport)
                .into(binding.ivCaseImage)
        } else {
            binding.ivCaseImage.setImageResource(R.drawable.bg_photoreport)
        }
    }

    private fun setupStatusSpinner() {
        val statusOptions = listOf("belum_ditangani", "sedang_ditangani", "sudah_ditangani")
        val statusDisplayNames = listOf("Belum Ditangani", "Sedang Ditangani", "Sudah Ditangani")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusDisplayNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter

        // Set current status as selected
        val currentIndex = statusOptions.indexOf(currentStatus)
        if (currentIndex != -1) {
            binding.spinnerStatus.setSelection(currentIndex)
        }

        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = statusOptions[position]
                if (selectedStatus != currentStatus) {
                    showUpdateStatusDialog(selectedStatus, statusDisplayNames[position])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showUpdateStatusDialog(newStatus: String, displayName: String) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Perubahan Status")
            .setMessage("Apakah Anda yakin ingin mengubah status menjadi \"$displayName\"?")
            .setPositiveButton("Ya") { _, _ ->
                updateCaseStatus(newStatus)
            }
            .setNegativeButton("Batal") { _, _ ->
                // Reset spinner to current status
                val statusOptions = listOf("belum_ditangani", "sedang_ditangani", "sudah_ditangani")
                val currentIndex = statusOptions.indexOf(currentStatus)
                if (currentIndex != -1) {
                    binding.spinnerStatus.setSelection(currentIndex)
                }
            }
            .show()
    }

    private fun updateCaseStatus(newStatus: String) {
        lifecycleScope.launch {
            try {
                // Get token from SharedPreferences or wherever it's stored
                val token = getSharedPreferences("user_session", MODE_PRIVATE)
                    .getString("token", "") ?: ""

                if (token.isEmpty()) {
                    Toast.makeText(this@DetailCasesPoliceActivity,
                        "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = repository.updateReportStatus(token, reportId, newStatus)

                if (response.isSuccessful && response.body()?.error == false) {
                    currentStatus = newStatus
                    Toast.makeText(this@DetailCasesPoliceActivity,
                        "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DetailCasesPoliceActivity,
                        "Gagal memperbarui status: ${response.body()?.message}",
                        Toast.LENGTH_SHORT).show()

                    // Reset spinner to current status
                    val statusOptions = listOf("belum_ditangani", "sedang_ditangani", "sudah_ditangani")
                    val currentIndex = statusOptions.indexOf(currentStatus)
                    if (currentIndex != -1) {
                        binding.spinnerStatus.setSelection(currentIndex)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailCasesPoliceActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()

                // Reset spinner to current status
                val statusOptions = listOf("belum_ditangani", "sedang_ditangani", "sudah_ditangani")
                val currentIndex = statusOptions.indexOf(currentStatus)
                if (currentIndex != -1) {
                    binding.spinnerStatus.setSelection(currentIndex)
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap?.uiSettings?.apply {
            isMapToolbarEnabled = false
            isZoomControlsEnabled = true
        }

        updateCaseLocation()
        setupMarkerClickListener()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun updateCaseLocation() {
        val latitude = intent.getDoubleExtra("report_latitude", 0.0)
        val longitude = intent.getDoubleExtra("report_longitude", 0.0)
        caseLocation = LatLng(latitude, longitude)

        googleMap?.apply {
            addMarker(
                MarkerOptions()
                    .position(caseLocation)
                    .title("Lokasi Kasus")
            )
            moveCamera(CameraUpdateFactory.newLatLngZoom(caseLocation, 14f))
        }
    }

    private fun setupMarkerClickListener() {
        googleMap?.setOnMarkerClickListener { marker: Marker ->
            AlertDialog.Builder(this)
                .setTitle("Buka Google Maps")
                .setMessage("Apakah Anda ingin membuka lokasi kasus ini di Google Maps?")
                .setPositiveButton("Ya") { _, _ ->
                    val gmmIntentUri = Uri.parse("geo:${caseLocation.latitude},${caseLocation.longitude}?q=${caseLocation.latitude},${caseLocation.longitude}(Lokasi Kasus)")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    if (mapIntent.resolveActivity(packageManager) != null) {
                        startActivity(mapIntent)
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
            true
        }
    }
}