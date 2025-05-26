package com.restusofyan.crimealert_mobile.ui.users.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.model.CasesModel
import com.restusofyan.crimealert_mobile.databinding.FragmentHomeBinding
import com.restusofyan.crimealert_mobile.ui.adapter.NewsAdapter
import com.restusofyan.crimealert_mobile.ui.customview.CustomDialogVoiceDetectionFragment
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import com.restusofyan.crimealert_mobile.utils.AudioClassificationHelper
import org.tensorflow.lite.support.label.Category
import com.restusofyan.crimealert_mobile.utils.VoiceDetectionService

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var newsAdapter: NewsAdapter
    private var audioHelper: AudioClassificationHelper? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButton()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun setupRecyclerView() {
        binding.rvTrendingcases.layoutManager = LinearLayoutManager(requireContext())
        val newsList = createDummyData()
        Log.d("HomeFragment", "Jumlah data: ${newsList.size}")

        newsAdapter = NewsAdapter(newsList) { selectedNews ->
            val intent = Intent(requireContext(), DetailCasesActivity::class.java).apply {
                putExtra("news_id", selectedNews.id)
                putExtra("news_title", selectedNews.title)
                putExtra("news_description", selectedNews.description)
                putExtra("news_image_url", selectedNews.imageUrl)
                putExtra("news_timestamp", selectedNews.timestamp)
                putExtra("news_date", selectedNews.date)
                putExtra("news_status", selectedNews.status)
                putExtra("news_latitude", selectedNews.latitude)
                putExtra("news_longitude", selectedNews.longitude)
            }
            startActivity(intent)
        }

        binding.rvTrendingcases.adapter = newsAdapter
    }

    private fun setupButton() {
        binding.createreport.setOnClickListener {
            Log.d("HomeFragment", "Create Report clicked!")
            findNavController().navigate(R.id.createReportFragment)
        }

        binding.reporthistory.setOnClickListener {
            Log.d("HomeFragment", "Report History clicked!")
            findNavController().navigate(R.id.reportHistoryFragment)
        }

        binding.voiceDetection.setOnClickListener {
            val dialog = CustomDialogVoiceDetectionFragment()

            dialog.onYesClick = {
                setupAudioHelper()
                audioHelper?.startAudioClassification()
                Toast.makeText(requireContext(), "Voice detection started", Toast.LENGTH_SHORT).show()

                // Start Voice Detection Service (Notifikasi Berjalan)
                startVoiceDetectionService()
            }

            dialog.show(parentFragmentManager, CustomDialogVoiceDetectionFragment::class.java.simpleName)
        }
    }

    private fun setupAudioHelper() {
        audioHelper = AudioClassificationHelper(
            context = requireContext(),
            classifierListener = object : AudioClassificationHelper.ClassifierListener {
                override fun onError(error: String) {
                    Log.e("AudioClassifier", error)
                }

                override fun onResults(results: List<Category>, inferenceTime: Long) {
                    for (category in results) {
                        Log.d("AudioClassifier", "Label: ${category.label}, Score: ${category.score}")
                        if (category.label == "scream" && category.score > 0.9) {
                            Log.d("ScreamDetected", "Screaming sound detected with score ${category.score}")
                            audioHelper?.stopAudioClassification()

                            handleScreamDetected()
                            break
                        }
                    }
                }
            }
        )
    }

    private fun startVoiceDetectionService() {
        val serviceIntent = Intent(requireContext(), VoiceDetectionService::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent)
        } else {
            requireContext().startService(serviceIntent)
        }
    }


    private fun handleScreamDetected() {
        audioHelper?.stopAudioClassification()
        stopVoiceDetectionService()

        // Meminta izin lokasi jika belum diberikan
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity(), OnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("ScreamDetected", "Location captured: Lat: $latitude, Lng: $longitude")

                // Simulasikan penyimpanan ke server / Firebase / database lokal
                saveScreamLocation(latitude, longitude)

                // Feedback ke pengguna
                Toast.makeText(requireContext(), "Screaming detected!\nLocation shared!", Toast.LENGTH_LONG).show()
            } else {
                Log.e("ScreamDetected", "Failed to get location")
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveScreamLocation(latitude: Double, longitude: Double) {
        // Di sini kamu bisa menyimpan ke Firebase, Room, atau API
        // Untuk sekarang kita log sebagai simulasi penyimpanan

        Log.d("ScreamDetected", "Saving scream location to database...")
        Log.d("ScreamDetected", "Saved location: Latitude = $latitude, Longitude = $longitude")

        // Contoh: Kirim ke Firebase atau API di sini
    }

    private fun stopVoiceDetectionService() {
        val serviceIntent = Intent(requireContext(), VoiceDetectionService::class.java)
        requireContext().stopService(serviceIntent)
    }

    private fun createDummyData(): List<CasesModel> {
        return listOf(
            CasesModel(1, "Keributan di Jalan Magelang", "Ada segerombolan remaja membawa sajam...", "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg", "23:59", "Kamis, 13 Maret 2025", "Sudah Ditangani", -7.747033, 110.353738),
            CasesModel(2, "Kecelakaan di Jalan Sudirman", "Kecelakaan beruntun melibatkan 3 kendaraan...", "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg", "08:30", "Jumat, 14 Maret 2025", "Sudah Ditangani", -7.782916, 110.367744),
            CasesModel(3, "Festival Kuliner di Malioboro", "Festival kuliner tahunan kembali digelar...", "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg", "12:45", "Sabtu, 15 Maret 2025", "Sudah Ditangani", -7.793083, 110.363633),
            CasesModel(4, "Banjir di Kawasan Bantul", "Hujan deras sepanjang malam menyebabkan banjir...", "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg", "06:15", "Minggu, 16 Maret 2025", "Sudah Ditangani", -7.888063, 110.325110),
            CasesModel(5, "Kebakaran di Pasar Beringharjo", "Api menghanguskan sekitar 15 kios...", "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg", "22:10", "Senin, 17 Maret 2025", "Sudah Ditangani", -7.800601, 110.367152)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        audioHelper?.stopAudioClassification()
    }
}
