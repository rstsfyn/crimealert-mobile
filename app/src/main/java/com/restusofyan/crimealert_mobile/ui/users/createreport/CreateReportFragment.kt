package com.restusofyan.crimealert_mobile.ui.users.createreport

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.LocationServices
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.FragmentCreateReportBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateReportFragment : Fragment() {

    private lateinit var binding: FragmentCreateReportBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val createReportViewModel: CreateReportViewModel by viewModels()
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>


    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null
    private var currentLocation: Pair<Double, Double>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateReportBinding.inflate(inflater, container, false)

        hideBottomNavigation()
        initializeSharedPreferences()
        setupButtonListeners()
        setupObservers()
        setupLocationPermissionLauncher()

        restoreStateIfNeeded(savedInstanceState)
        getCurrentLocation()

        return binding.root
    }

    private fun initializeSharedPreferences() {
        sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
    }

    private fun restoreStateIfNeeded(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            selectedImageUri = bundle.getString("imageUri")?.let { Uri.parse(it) }
            currentPhotoPath = bundle.getString("photoPath")

            selectedImageUri?.let { uri ->
                binding.imagePreview.setImageURI(uri)
            }
        }
    }

    private fun hideBottomNavigation() {
        (activity as? androidx.appcompat.app.AppCompatActivity)?.findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    private fun setupButtonListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.takePhoto.setOnClickListener {
            takePhoto()
        }

        binding.upload.setOnClickListener {
            uploadReport()
        }
    }

    private fun setupObservers() {
        createReportViewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is CreateReportViewModel.UploadResult.Loading -> {
                    binding.upload.isEnabled = false
                    binding.upload.text = "Uploading..."
                }
                is CreateReportViewModel.UploadResult.Success -> {
                    binding.upload.isEnabled = true
                    binding.upload.text = "Upload Report"
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
                is CreateReportViewModel.UploadResult.Error -> {
                    binding.upload.isEnabled = true
                    binding.upload.text = "Upload Report"
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadReport() {
        val title = binding.titleInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()
        val token = sharedPref.getString("token", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Please log in again", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentPhotoPath == null) {
            Toast.makeText(requireContext(), "Please take a photo", Toast.LENGTH_SHORT).show()
            return
        }

        val compressedFile = createReportViewModel.compressImage(currentPhotoPath!!)
        if (compressedFile == null) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        val (latitude, longitude) = currentLocation ?: (null to null)
        createReportViewModel.uploadReport(
            token = token,
            title = title,
            description = description,
            file = compressedFile,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            captureImage()
        }
    }

    private fun captureImage() {
        val photoFile: File? = createImageFile()
        if (photoFile != null) {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            requestImageCapture.launch(intent)
        } else {
            Toast.makeText(requireContext(), "Failed to create image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

            File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (ex: IOException) {
            Log.e("CreateReportFragment", "Error creating image file", ex)
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
            Toast.makeText(requireContext(), "Photo capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLocationPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                captureImage()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission is required to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = Pair(location.latitude, location.longitude)
                    Log.d("CreateReportFragment", "Location obtained: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.d("CreateReportFragment", "Location is null")
                    Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("CreateReportFragment", "Failed to get location", exception)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
}