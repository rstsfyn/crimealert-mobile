package com.restusofyan.crimealert_mobile.ui.users.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.model.CasesModel
import com.restusofyan.crimealert_mobile.databinding.FragmentHomeBinding
import com.restusofyan.crimealert_mobile.ui.adapter.NewsAdapter
import com.restusofyan.crimealert_mobile.ui.customview.CustomDialogVoiceDetectionFragment
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import com.restusofyan.crimealert_mobile.ui.users.news.NewsViewModel
import com.restusofyan.crimealert_mobile.utils.AudioClassificationHelper
import com.restusofyan.crimealert_mobile.utils.VoiceDetectionService
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.support.label.Category

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var newsAdapter: NewsAdapter
    private var audioHelper: AudioClassificationHelper? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedAudio = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val grantedLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

        if (grantedAudio && grantedLocation) {
            showVoiceDetectionDialog()
        } else {
            Toast.makeText(
                requireContext(),
                "Permission mikrofon dan lokasi diperlukan untuk deteksi suara",
                Toast.LENGTH_LONG
            ).show()
        }
    }

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
        setupObservers()
        setupButton()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        displayUserInfo()
        ambilDataNews()
    }

    private fun ambilDataNews() {
        val token = ambilTokenSession()
        if (token != null) {
            viewModel.fetchReports(token)
        } else {
            // Tangani kondisi token null, misal tampilkan pesan atau redirect ke login
            Log.e("NewsFragment", "Token user tidak ditemukan")
        }
    }

    private fun displayUserInfo() {
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("name", "User")
        var userAvatar = sharedPref.getString("avatar", null)

        userAvatar = userAvatar?.replace("localhost", "10.0.2.2")

        binding.tvName.text = userName ?: "User"

        Glide.with(this)
            .load(userAvatar)
            .placeholder(R.drawable.profilephoto)
            .error(R.drawable.profilephoto)
            .into(binding.ivAvatar)
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(emptyList()) { selectedNews ->
            val intent = Intent(requireContext(), DetailCasesActivity::class.java).apply {
                putExtra("news_id", selectedNews.idReport)
                putExtra("news_title", selectedNews.title)
                putExtra("news_description", selectedNews.description)
                putExtra("news_image_url", selectedNews.picture)
                putExtra("news_timestamp", selectedNews.createdAt)
                putExtra("news_date", selectedNews.createdAt?.substringBefore("T"))
                putExtra("news_status", selectedNews.statusKasus)
                putExtra("news_latitude", selectedNews.map?.latitude)
                putExtra("news_longitude", selectedNews.map?.longitude)
            }
            startActivity(intent)
        }

        binding.rvTrendingcases.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun setupObservers() {
        viewModel.reports.observe(viewLifecycleOwner) { data ->
            data?.let {
                val listReports = it.filterNotNull().take(5)
                newsAdapter.updateData(listReports)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Log.e("NewsFragment", it)
                // Tampilkan Snackbar / Toast jika perlu
            }
        }
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
            if (hasPermissions()) {
                showVoiceDetectionDialog()
            } else {
                // Request both RECORD_AUDIO and ACCESS_FINE_LOCATION permissions
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }

        binding.tvTrendingcasesSeeall.setOnClickListener {
            Log.d("HomeFragment", "See all clicked!")
            findNavController().navigate(R.id.navigation_news)
        }
    }

    private fun hasPermissions(): Boolean {
        val recordAudioGranted = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val locationGranted = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return recordAudioGranted && locationGranted
    }

    private fun showVoiceDetectionDialog() {
        val dialog = CustomDialogVoiceDetectionFragment()
        dialog.onYesClick = {
            setupAudioHelper()
            audioHelper?.startAudioClassification()
            Toast.makeText(requireContext(), "Voice detection started", Toast.LENGTH_SHORT).show()
            startVoiceDetectionService()
        }
        dialog.show(parentFragmentManager, CustomDialogVoiceDetectionFragment::class.java.simpleName)
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

    private fun stopVoiceDetectionService() {
        val serviceIntent = Intent(requireContext(), VoiceDetectionService::class.java)
        requireContext().stopService(serviceIntent)
    }

    private fun handleScreamDetected() {
        audioHelper?.stopAudioClassification()
        stopVoiceDetectionService()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Permission lokasi tidak diberikan", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("ScreamDetected", "Location captured: Lat: $latitude, Lng: $longitude")

                saveScreamLocation(latitude, longitude)

                Toast.makeText(requireContext(), "Screaming detected!\nLocation shared!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveScreamLocation(latitude: Double, longitude: Double) {
        // Simulasi penyimpanan ke server / Firebase / database lokal
        Log.d("ScreamDetected", "Saving scream location to database...")
        Log.d("ScreamDetected", "Saved location: Latitude = $latitude, Longitude = $longitude")

        // TODO: Implementasi penyimpanan ke Firebase atau API di sini
    }

    private fun ambilTokenSession(): String? {
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        audioHelper?.stopAudioClassification()
    }
}
