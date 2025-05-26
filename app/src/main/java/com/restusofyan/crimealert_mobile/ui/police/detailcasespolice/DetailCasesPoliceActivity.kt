package com.restusofyan.crimealert_mobile.ui.police.detailcasespolice

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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.ActivityDetailCasesPoliceBinding

class DetailCasesPoliceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailCasesPoliceBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var googleMap: GoogleMap? = null
    private lateinit var caseLocation: LatLng

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

    private fun setupDetailCasesData() {
        binding.tvTitle.text = intent.getStringExtra("news_title")
        binding.tvDescription.text = intent.getStringExtra("news_description")
        binding.tvDate.text = intent.getStringExtra("news_date")
        binding.tvStatus.text = intent.getStringExtra("news_status")
        val imageUrl = intent.getStringExtra("news_image_url")
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

        googleMap?.uiSettings?.apply {
            isMapToolbarEnabled = false
            isZoomControlsEnabled = true
        }

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
