package com.restusofyan.crimealert_mobile.ui.users.detailcases

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.restusofyan.crimealert_mobile.databinding.ActivityDetailCasesBinding

class DetailCasesActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: ActivityDetailCasesBinding
    private var googleMap: GoogleMap? = null
    private var caseLocation: LatLng? = null

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

    private fun setupDetailCasesData() {
        binding.tvTitle.text = intent.getStringExtra("news_title")
        binding.tvDescription.text = intent.getStringExtra("news_description")
        binding.tvDate.text = intent.getStringExtra("news_date")
        binding.tvNewsTimestamp.text = intent.getStringExtra("news_timestamp")?.let { raw ->
            val tIndex = raw.indexOf('T')
            if (tIndex != -1 && raw.length >= tIndex + 6) raw.substring(tIndex + 1, tIndex + 6) else "--:--"
        } ?: "--:--"
        var imageUrl = intent.getStringExtra("news_image_url")
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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        updateCaseLocation()
        setupMarkerClickListener()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun updateCaseLocation() {
        val latitude = intent.getDoubleExtra("news_latitude", 0.0)
        val longitude = intent.getDoubleExtra("news_longitude", 0.0)
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
            true // Return true to indicate we've consumed the event
        }
    }

}
