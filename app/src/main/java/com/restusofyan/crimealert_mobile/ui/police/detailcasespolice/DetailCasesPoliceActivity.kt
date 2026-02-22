package com.restusofyan.crimealert_mobile.ui.police.detailcasespolice

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.restusofyan.crimealert_mobile.ui.adapter.StatusHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DetailCasesPoliceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailCasesPoliceBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var googleMap: GoogleMap? = null
    private var caseLocation: LatLng? = null
    private var reportId: Int = 0
    private var currentStatus: String = ""
    private var selectedImageUri: Uri? = null
    private var isSpinnerInitialized = false
    private var photoUri: Uri? = null

    @Inject
    lateinit var repository: CrimeAlertRepository
    
    // Camera launcher for evidence photo
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && photoUri != null) {
            selectedImageUri = photoUri
            currentPhotoPreview?.let { preview ->
                preview.visibility = View.VISIBLE
                currentPhotoImageView?.let { imageView ->
                    Glide.with(this)
                        .load(photoUri)
                        .into(imageView)
                }
            }
        }
    }
    
    private var currentPhotoPreview: androidx.cardview.widget.CardView? = null
    private var currentPhotoImageView: android.widget.ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCasesPoliceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomSheet.bringToFront()

        setupButton()
        setupBottomSheet()
        setupDetailCasesData()
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

    private fun formatTimestampToGMT7(timestamp: String?): String {
        return try {
            if (timestamp.isNullOrEmpty()) return "--:--"

            // Parse ISO 8601 timestamp (assuming format like "2023-12-01T10:30:00Z" or similar)
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

            // Parse ISO 8601 date (assuming format like "2023-12-01T10:30:00Z" or "2023-12-01")
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
            // Fallback to original logic if parsing fails
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
                    Toast.makeText(this@DetailCasesPoliceActivity, 
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

                        // Set current status
                        currentStatus = data.statusKasus ?: "belum_ditangani"

                        // Set reporter info
                        binding.tvReporterName.text = data.user?.name ?: "Unknown"
                        
                        // Load reporter avatar
                        if (!data.user?.avatar.isNullOrEmpty()) {
                            Glide.with(this@DetailCasesPoliceActivity)
                                .load(data.user?.avatar)
                                .placeholder(R.drawable.bg_photoreport)
                                .error(R.drawable.bg_photoreport)
                                .into(binding.ivReporterAvatar)
                        } else {
                            binding.ivReporterAvatar.setImageResource(R.drawable.bg_photoreport)
                        }

                        // Load case image
                        if (!data.picture.isNullOrEmpty()) {
                            Glide.with(this@DetailCasesPoliceActivity)
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

                        // Setup spinner on first load, update selection on refresh
                        if (!isSpinnerInitialized) {
                            setupStatusSpinner()
                        } else {
                            updateSpinnerSelection()
                        }

                        // Setup status history
                        setupStatusHistory(data.statusHistory ?: emptyList())
                    }
                } else {
                    Toast.makeText(this@DetailCasesPoliceActivity,
                        "Gagal memuat detail laporan: ${response.body()?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailCasesPoliceActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DetailCasesPoliceActivity", "Error fetching report detail", e)
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
        val location = caseLocation ?: return // Return early if location not set yet
        
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

    private fun setupStatusSpinner() {
        val statusOptions = listOf("tidak_dapat_ditangani", "belum_ditangani", "sedang_ditangani", "sudah_ditangani")
        val statusDisplayNames = listOf("Tidak Dapat Ditangani", "Belum Ditangani", "Sedang Ditangani", "Sudah Ditangani")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusDisplayNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter

        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = statusOptions[position]
                
                // Only show dialog if spinner is already initialized and status actually changed
                if (isSpinnerInitialized && selectedStatus != currentStatus) {
                    showUpdateStatusDialog(selectedStatus, statusDisplayNames[position])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Set initial selection
        val currentIndex = statusOptions.indexOf(currentStatus)
        if (currentIndex != -1) {
            binding.spinnerStatus.setSelection(currentIndex)
        }
        
        // Mark spinner as initialized after setting initial selection
        isSpinnerInitialized = true
    }
    
    private fun updateSpinnerSelection() {
        val statusOptions = listOf("tidak_dapat_ditangani", "belum_ditangani", "sedang_ditangani", "sudah_ditangani")
        val currentIndex = statusOptions.indexOf(currentStatus)
        
        if (currentIndex != -1) {
            // Temporarily disable the flag to prevent dialog from showing
            isSpinnerInitialized = false
            binding.spinnerStatus.setSelection(currentIndex)
            // Re-enable the flag
            isSpinnerInitialized = true
        }
    }

    private fun showUpdateStatusDialog(newStatus: String, displayName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_status, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        // Initialize views
        val tvNewStatus = dialogView.findViewById<android.widget.TextView>(R.id.tv_new_status)
        val cardPhotoPreview = dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.card_photo_preview)
        val ivPhotoPreview = dialogView.findViewById<android.widget.ImageView>(R.id.iv_photo_preview)
        val btnRemovePhoto = dialogView.findViewById<android.widget.ImageButton>(R.id.btn_remove_photo)
        val btnSelectPhoto = dialogView.findViewById<android.widget.Button>(R.id.btn_select_photo)
        val etNotes = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_notes)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<android.widget.Button>(R.id.btn_confirm)
        
        tvNewStatus.text = "Status Baru: $displayName"
        
        // Reset selected image
        selectedImageUri = null
        photoUri = null
        cardPhotoPreview.visibility = View.GONE
        btnRemovePhoto.visibility = View.GONE // Hide remove button since photo is mandatory
        
        // Update button text
        btnSelectPhoto.text = "Ambil Foto Bukti (Wajib)"
        
        // Set current views for camera callback
        currentPhotoPreview = cardPhotoPreview
        currentPhotoImageView = ivPhotoPreview
        
        btnSelectPhoto.setOnClickListener {
            // Create image file
            val photoFile = File(cacheDir, "evidence_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                this@DetailCasesPoliceActivity,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )
            
            // Store URI for result handling
            photoUri = uri
            
            // Launch camera
            cameraLauncher.launch(uri)
        }
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
            selectedImageUri = null
            photoUri = null
            currentPhotoPreview = null
            currentPhotoImageView = null
            // Reset spinner to current status
            val statusOptions = listOf("tidak_dapat_ditangani", "belum_ditangani", "sedang_ditangani", "sudah_ditangani")
            val currentIndex = statusOptions.indexOf(currentStatus)
            if (currentIndex != -1) {
                binding.spinnerStatus.setSelection(currentIndex)
            }
        }
        
        btnConfirm.setOnClickListener {
            // Validate photo is taken
            if (selectedImageUri == null) {
                Toast.makeText(this, "Foto bukti wajib diambil!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val notes = etNotes.text?.toString()?.trim()
            dialog.dismiss()
            updateCaseStatus(newStatus, selectedImageUri, notes)
            // Clean up
            currentPhotoPreview = null
            currentPhotoImageView = null
        }
        
        dialog.show()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
    }

    private fun updateCaseStatus(newStatus: String, photoUri: Uri? = null, notes: String? = null) {
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences("user_session", MODE_PRIVATE)
                    .getString("token", "") ?: ""

                if (token.isEmpty()) {
                    Toast.makeText(this@DetailCasesPoliceActivity,
                        "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Prepare photo multipart if available
                var photoPart: MultipartBody.Part? = null
                photoUri?.let { uri ->
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()

                        val file = File(cacheDir, "evidence_${System.currentTimeMillis()}.jpg")
                        val outputStream = file.outputStream()
                        
                        // Compress to 50% quality to reduce file size significantly
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                        outputStream.flush()
                        outputStream.close()

                        val requestFile = file.asRequestBody("image/jpeg".toMediaType())
                        photoPart = MultipartBody.Part.createFormData("evidence_photo", file.name, requestFile)
                    } catch (e: Exception) {
                        Log.e("DetailCasesPoliceActivity", "Error preparing photo", e)
                        Toast.makeText(this@DetailCasesPoliceActivity,
                            "Gagal memproses foto", Toast.LENGTH_SHORT).show()
                    }
                }

                val response = repository.updateReportStatus(token, reportId, newStatus, photoPart, notes)

                if (response.isSuccessful && response.body()?.error == false) {
                    currentStatus = newStatus
                    Toast.makeText(this@DetailCasesPoliceActivity,
                        "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    
                    // Refresh report detail to get updated status history
                    fetchReportDetail()
                } else {
                    Toast.makeText(this@DetailCasesPoliceActivity,
                        "Gagal memperbarui status: ${response.body()?.message}",
                        Toast.LENGTH_SHORT).show()

                    val statusOptions = listOf("tidak_dapat_ditangani", "belum_ditangani", "sedang_ditangani", "sudah_ditangani")
                    val currentIndex = statusOptions.indexOf(currentStatus)
                    if (currentIndex != -1) {
                        binding.spinnerStatus.setSelection(currentIndex)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailCasesPoliceActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()

                val statusOptions = listOf("tidak_dapat_ditangani", "belum_ditangani", "sedang_ditangani", "sudah_ditangani")
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

        updateMapLocation()
        setupMarkerClickListener()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupMarkerClickListener() {
        googleMap?.setOnMarkerClickListener { marker: Marker ->
            val location = caseLocation
            if (location != null) {
                AlertDialog.Builder(this)
                    .setTitle("Buka Google Maps")
                    .setMessage("Apakah Anda ingin membuka lokasi kasus ini di Google Maps?")
                    .setPositiveButton("Ya") { _, _ ->
                        val gmmIntentUri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Lokasi Kasus)")
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