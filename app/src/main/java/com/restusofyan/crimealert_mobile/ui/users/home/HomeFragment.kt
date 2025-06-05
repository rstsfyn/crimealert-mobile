package com.restusofyan.crimealert_mobile.ui.users.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.FragmentHomeBinding
import com.restusofyan.crimealert_mobile.ui.adapter.NewsAdapter
import com.restusofyan.crimealert_mobile.ui.customview.CustomDialogVoiceDetectionFragment
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import com.restusofyan.crimealert_mobile.utils.ScreamDetectionManager
import com.restusofyan.crimealert_mobile.utils.VoiceDetectionService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var newsAdapter: NewsAdapter

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

    private val screamDetectionListener = object : ScreamDetectionManager.ScreamDetectionListener {
        override fun onScreamDetected(latitude: Double, longitude: Double) {
            activity?.runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    "🚨 Screaming detected!\nLocation shared: $latitude, $longitude",
                    Toast.LENGTH_LONG
                ).show()
            }
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
        displayUserInfo()
        ambilDataNews()

        ScreamDetectionManager.getInstance().addListener(screamDetectionListener)
    }

    private fun ambilDataNews() {
        val token = ambilTokenSession()
        if (token != null) {
            viewModel.fetchReports(token)
        } else {
            Log.e("NewsFragment", "Token user tidak ditemukan")
        }
    }

    private fun displayUserInfo() {
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("name", "User")
        val userAvatar = sharedPref.getString("avatar", null)

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
                putExtra("avatar_reporter", selectedNews.user?.avatar)
                putExtra("name_reporter", selectedNews.user?.name)
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
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Log.e("NewsFragment", it)
            }
        }
    }

    private fun setupButton() {
        binding.icNotification.setOnClickListener{
            findNavController().navigate(R.id.notificationUser)
        }

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
            startVoiceDetectionService()
            Toast.makeText(requireContext(), "Voice detection started", Toast.LENGTH_SHORT).show()
        }
        dialog.show(parentFragmentManager, CustomDialogVoiceDetectionFragment::class.java.simpleName)
    }

    private fun startVoiceDetectionService() {
        val serviceIntent = Intent(requireContext(), VoiceDetectionService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent)
        } else {
            requireContext().startService(serviceIntent)
        }
        Log.d("HomeFragment", "VoiceDetectionService started")
    }

    private fun ambilTokenSession(): String? {
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ScreamDetectionManager.getInstance().removeListener(screamDetectionListener)
        _binding = null
    }
}