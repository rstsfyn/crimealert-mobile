package com.restusofyan.crimealert_mobile.ui.users.createreport

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import com.restusofyan.crimealert_mobile.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateReportFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val viewModel: CreateReportViewModel by viewModels()

    private var currentPhotoPath: String? = null
    private var photoURI: Uri? = null

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

        // Jika ViewModel sudah simpan foto path, tampilkan foto
        currentPhotoPath?.let {
            setPic(it)
        }

        return view
    }

    private fun hideBottomNavigation() {
        (activity as? androidx.appcompat.app.AppCompatActivity)?.findViewById<View>(R.id.nav_view)?.visibility = View.GONE
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
                // Foto sudah disimpan ke file, tampilkan
                currentPhotoPath?.let {
                    setPic(it)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to take photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
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
        // Buat file foto dulu
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                it
            )
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
                cameraLauncher.launch(cameraIntent)
            } else {
                Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Membuat file gambar baru dengan nama unik
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun setPic(photoPath: String) {
        // Atur ukuran gambar supaya pas di ImageView untuk menghemat memory
        val targetW: Int = imagePreview.width
        val targetH: Int = imagePreview.height

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(photoPath, bmOptions)

        val photoW: Int = bmOptions.outWidth
        val photoH: Int = bmOptions.outHeight

        // Hitung scale down factor
        val scaleFactor: Int = if (targetW > 0 && targetH > 0) {
            Math.min(photoW / targetW, photoH / targetH)
        } else {
            1
        }

        val bmOptions2 = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }

        val bitmap = BitmapFactory.decodeFile(photoPath, bmOptions2)
        imagePreview.setImageBitmap(bitmap)

        // Simpan bitmap ke ViewModel juga kalau perlu (optional)
        // viewModel.capturedImage = bitmap
    }

    private fun uploadReport() {
        Toast.makeText(requireContext(), "Upload clicked", Toast.LENGTH_SHORT).show()
        // TODO: Implement upload logic here, termasuk upload file gambar di currentPhotoPath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
}
