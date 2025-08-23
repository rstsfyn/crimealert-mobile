package com.restusofyan.crimealert_mobile.ui.customview

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.*
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.utils.SensitivityLevel
import com.restusofyan.crimealert_mobile.utils.VoiceDetectionService

class CustomDialogShareLocationFragment : DialogFragment() {

    var onYesClick: (() -> Unit)? = null
    var onNoClick: (() -> Unit)? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var selectedSensitivity: SensitivityLevel = SensitivityLevel.LOW

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.fragment_custom_dialog_share_location, null)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnYes = dialogView.findViewById<Button>(R.id.btn_yes)
        val btnNo = dialogView.findViewById<Button>(R.id.btn_no)

        btnYes.setOnClickListener {
            if (isLocationEnabled()) {
                shareCurrentLocation()
            } else {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }

            startVoiceDetectionServiceWithSensitivity()
            onYesClick?.invoke()
            dismiss()
        }

        btnNo.setOnClickListener {
            onNoClick?.invoke()
            dismiss()
        }

        return dialog
    }

    fun setSensitivity(sensitivity: SensitivityLevel) {
        selectedSensitivity = sensitivity
        Log.d("ShareLocationDialog", "Received sensitivity: ${sensitivity.displayName} (${sensitivity.threshold})")
    }

    private fun startVoiceDetectionServiceWithSensitivity() {
        Log.d("ShareLocationDialog", "Starting service with sensitivity: ${selectedSensitivity.displayName}")

        val serviceIntent = Intent(requireContext(), VoiceDetectionService::class.java).apply {
            putExtra(VoiceDetectionService.EXTRA_SENSITIVITY, selectedSensitivity.displayName)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent)
        } else {
            requireContext().startService(serviceIntent)
        }

        Log.d("ShareLocationDialog", "VoiceDetectionService started with ${selectedSensitivity.displayName} sensitivity")
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun shareCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Location", "Permission not granted")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("LocationShare", "Lat: $latitude, Lng: $longitude")
            } else {
                Log.e("LocationShare", "Last location not available, requesting update...")
                requestNewLocationData()
            }
        }.addOnFailureListener { exception ->
            Log.e("LocationShare", "Failed to get last location: ${exception.message}")
            requestNewLocationData()
        }
    }

    private fun requestNewLocationData() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                val latitude = location?.latitude
                val longitude = location?.longitude
                Log.d("LocationShare", "Updated Lat: $latitude, Lng: $longitude")
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Location", "Permission not granted for location updates")
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }
}