package com.restusofyan.crimealert_mobile.ui.alluserpage.validateinsidens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.ActivityValidateInsidensBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ValidateInsidensActivity : AppCompatActivity() {

    private lateinit var binding: ActivityValidateInsidensBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var validateInsidensViewModel: ValidateInsidensViewModel

    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null
    private var currentLocation: Pair<Double, Double>? = null
    private var incidentId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityValidateInsidensBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        validateInsidensViewModel = ViewModelProvider(this)[ValidateInsidensViewModel::class.java]

        val incidentIdStr = intent.getStringExtra("incident_id")
        incidentId = incidentIdStr?.toIntOrNull() ?: 0
        Log.d("ValidateInsidensActivity", "Incident ID: $incidentId")

        setupButtonListeners()
        setupObservers()
        setupPermissionLaunchers()

        restoreStateIfNeeded(savedInstanceState)
        getCurrentLocation()
    }

    private fun setupButtonListeners() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.takePhoto.setOnClickListener {
            takePhoto()
        }

        binding.upload.setOnClickListener {
            uploadReport()
        }
    }

    private fun setupObservers() {
        validateInsidensViewModel.uploadResult.observe(this) { result ->
            when (result) {
                is ValidateInsidensViewModel.UploadResult.Loading -> {
                    binding.upload.isEnabled = false
                    binding.upload.text = "Uploading..."
                }
                is ValidateInsidensViewModel.UploadResult.Success -> {
                    binding.upload.isEnabled = true
                    binding.upload.text = "Upload Report"
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is ValidateInsidensViewModel.UploadResult.Error -> {
                    binding.upload.isEnabled = true
                    binding.upload.text = "Upload Report"
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun restoreStateIfNeeded(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            selectedImageUri = bundle.getString("imageUri")?.let { Uri.parse(it) }
            currentPhotoPath = bundle.getString("photoPath")
            incidentId = bundle.getInt("incidentId", 0)

            selectedImageUri?.let { uri ->
                binding.imagePreview.setImageURI(uri)
                binding.imagePreview.visibility = View.VISIBLE
            }
        }
    }

    private fun uploadReport() {
        val title = binding.titleInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()
        val token = sharedPref.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        if (incidentId == 0) {
            Toast.makeText(this, "Invalid incident ID", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentPhotoPath == null) {
            Toast.makeText(this, "Please take a photo", Toast.LENGTH_SHORT).show()
            return
        }

        val compressedFile = validateInsidensViewModel.compressImage(currentPhotoPath!!)
        if (compressedFile == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        val (latitude, longitude) = currentLocation ?: (null to null)
        validateInsidensViewModel.validateInsiden(
            token = token,
            title = title,
            description = description,
            file = compressedFile,
            latitude = latitude,
            longitude = longitude,
            incidentId = incidentId
        )
    }

    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            captureImage()
        }
    }

    private fun captureImage() {
        val photoFile: File? = createImageFile()
        if (photoFile != null) {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                photoFile
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            requestImageCapture.launch(intent)
        } else {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

            File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (ex: IOException) {
            Log.e("ValidateInsidensActivity", "Error creating image file", ex)
            null
        }
    }

    private val requestImageCapture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                val imageUri = Uri.fromFile(File(path))
                selectedImageUri = imageUri
                binding.imagePreview.setImageURI(imageUri)
                binding.imagePreview.visibility = View.VISIBLE
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            currentPhotoPath = null
            selectedImageUri = null
            Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPermissionLaunchers() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                captureImage()
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission is required to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = Pair(location.latitude, location.longitude)
                    Log.d("ValidateInsidensActivity", "Location obtained: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.d("ValidateInsidensActivity", "Location is null")
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("ValidateInsidensActivity", "Failed to get location", exception)
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedImageUri?.let {
            outState.putString("imageUri", it.toString())
        }
        outState.putString("photoPath", currentPhotoPath)
        outState.putInt("incidentId", incidentId)
    }
}
