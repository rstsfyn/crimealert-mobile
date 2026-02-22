package com.restusofyan.crimealert_mobile.ui.users.detailcases

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.databinding.ActivityDetailCasesBinding
import com.restusofyan.crimealert_mobile.ui.adapter.StatusHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class DetailCasesActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: ActivityDetailCasesBinding
    private var googleMap: GoogleMap? = null
    private var caseLocation: LatLng? = null
    private var reportId: Int = 0

    @Inject
    lateinit var repository: CrimeAlertRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCasesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomSheet.bringToFront()

        setupBottomSheet()
        setupDetailCasesData()
        setupButton()
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
                // Optional: handle state changes
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Optional: handle slide offset
            }
        })
    }

    private fun formatTimestampToGMT7(timestamp: String?): String {
        return try {
            if (timestamp.isNullOrEmpty()) return "--:--"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Assume input is UTC

            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("GMT+7") // Convert to GMT+7

            val date = inputFormat.parse(timestamp.replace("Z", ""))
            date?.let { outputFormat.format(it) } ?: "--:--"
        } catch (e: Exception) {
            // Fallback to original logic if parsing fails
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

    private fun setupDetailCasesData() {
        reportId = intent.getIntExtra("report_id", 0)
        
        if (reportId == 0) {
            Toast.makeText(this, "Invalid report ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchReportDetail()
    }

    private fun fetchReportDetail() {
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences("user_session", MODE_PRIVATE)
                    .getString("token", "") ?: ""

                if (token.isEmpty()) {
                    Toast.makeText(this@DetailCasesActivity, 
                        "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = repository.getReportDetail(token, reportId)

                if (response.isSuccessful && response.body()?.error == false) {
                    val reportData = response.body()?.data
                    reportData?.let { data ->
                        // Set basic info
                        binding.tvTitle.text = data.title ?: "No Title"
                        binding.tvDescription.text = data.description ?: "No Description"
                        binding.tvNewsTimestamp.text = formatTimestampToGMT7(data.createdAt)
                        binding.tvDate.text = formatDateToGMT7(data.createdAt)

                        // Set reporter info
                        binding.tvReporterName.text = data.user?.name ?: "Unknown"
                        
                        // Load reporter avatar
                        if (!data.user?.avatar.isNullOrEmpty()) {
                            Glide.with(this@DetailCasesActivity)
                                .load(data.user?.avatar)
                                .placeholder(R.drawable.bg_photoreport)
                                .error(R.drawable.bg_photoreport)
                                .into(binding.ivReporterAvatar)
                        } else {
                            binding.ivReporterAvatar.setImageResource(R.drawable.bg_photoreport)
                        }

                        // Load case image
                        if (!data.picture.isNullOrEmpty()) {
                            Glide.with(this@DetailCasesActivity)
                                .load(data.picture)
                                .placeholder(R.drawable.bg_photoreport)
                                .error(R.drawable.bg_photoreport)
                                .into(binding.ivCaseImage)
                        } else {
                            binding.ivCaseImage.setImageResource(R.drawable.bg_photoreport)
                        }

                        // Update map location
                        data.map?.let { mapData ->
                            caseLocation = LatLng(
                                mapData.latitude ?: 0.0,
                                mapData.longitude ?: 0.0
                            )
                            updateMapLocation()
                        }

                        // Setup status history
                        setupStatusHistory(data.statusHistory ?: emptyList())
                    }
                } else {
                    Toast.makeText(this@DetailCasesActivity,
                        "Gagal memuat detail laporan: ${response.body()?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailCasesActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DetailCasesActivity", "Error fetching report detail", e)
            }
        }
    }

    private fun setupStatusHistory(statusHistory: List<com.restusofyan.crimealert_mobile.data.response.casesreports.StatusHistoryItem>) {
        if (statusHistory.isEmpty()) {
            binding.tvStatusHistoryTitle.visibility = View.VISIBLE
            binding.tvEmptyStatusHistory.visibility = View.VISIBLE
            binding.rvStatusHistory.visibility = View.GONE
        } else {
            binding.tvStatusHistoryTitle.visibility = View.VISIBLE
            binding.tvEmptyStatusHistory.visibility = View.GONE
            binding.rvStatusHistory.visibility = View.VISIBLE
            
            val adapter = StatusHistoryAdapter(statusHistory)
            binding.rvStatusHistory.layoutManager = LinearLayoutManager(this)
            binding.rvStatusHistory.adapter = adapter
        }
    }

    private fun updateMapLocation() {
        caseLocation?.let { location ->
            googleMap?.apply {
                clear()
                addMarker(
                    MarkerOptions()
                        .position(location)
                        .title("Lokasi Kasus")
                )
                moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        updateMapLocation()
        setupMarkerClickListener()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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
}