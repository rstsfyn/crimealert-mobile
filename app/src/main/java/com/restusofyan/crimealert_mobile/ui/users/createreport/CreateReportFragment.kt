package com.restusofyan.crimealert_mobile.ui.users.createreport

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.restusofyan.crimealert_mobile.R

class CreateReportFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // Inisialisasi ViewModel
    private val viewModel: CreateReportViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_create_report, container, false)

        hideBottomNavigation()
        initializeViews(view)
        setupClickListeners(view)
        setupCameraLauncher()
        setupPermissionLauncher()

        // Cek jika ViewModel sudah menyimpan gambar
        viewModel.capturedImage?.let {
            imagePreview.setImageBitmap(it)
        }

        return view
    }

    private fun hideBottomNavigation() {
        (activity as? AppCompatActivity)?.findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    private fun initializeViews(view: View) {
        imagePreview = view.findViewById(R.id.imagePreview)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<View>(R.id.take_photo).setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        view.findViewById<Button>(R.id.upload).setOnClickListener {
            uploadReport()
        }
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    viewModel.capturedImage = it
                    imagePreview.setImageBitmap(it)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to take photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), "Camera permission is needed to take photos", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            cameraLauncher.launch(cameraIntent)
        } else {
            Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadReport() {
        Toast.makeText(requireContext(), "Upload clicked", Toast.LENGTH_SHORT).show()
        // TODO: Implement upload logic here
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as? AppCompatActivity)?.findViewById<View>(R.id.nav_view)?.visibility =
            View.VISIBLE
    }
}
