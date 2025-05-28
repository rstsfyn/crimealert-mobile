package com.restusofyan.crimealert_mobile.ui.police.homepolice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.model.CasesModel
import com.restusofyan.crimealert_mobile.databinding.FragmentHomePoliceBinding
import com.restusofyan.crimealert_mobile.ui.adapter.CasesAdapter
import com.restusofyan.crimealert_mobile.ui.adapter.NewsAdapter
import com.restusofyan.crimealert_mobile.ui.police.detailcasespolice.DetailCasesPoliceActivity
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import com.restusofyan.crimealert_mobile.ui.users.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomePoliceFragment : Fragment() {

    private var _binding: FragmentHomePoliceBinding? = null
    private val binding get() = _binding!!

    private lateinit var casesAdapter: CasesAdapter
    private val viewModel: HomePoliceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePoliceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButton()
        setupObservers()
        displayUserInfo()
        ambilDataCases()
    }

    private fun displayUserInfo() {
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("name", "Ladhusing")
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
        casesAdapter = CasesAdapter(emptyList()) { selectedNews ->
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

        binding.rvReportpolice.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = casesAdapter
        }
    }

    private fun setupObservers() {
        viewModel.reports.observe(viewLifecycleOwner) { data ->
            data?.let {
                val listReports = it.filterNotNull().take(5)
                casesAdapter.updateData(listReports)
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

    private fun ambilDataCases() {
        val token = ambilTokenSession()
        if (token != null) {
            viewModel.fetchReports(token)
        } else {
            Log.e("NewsFragment", "Token user tidak ditemukan")
        }
    }


    private fun setupButton() {
//        binding.createreportpolice.setOnClickListener {
//            Log.d("HomeFragment", "Create Report clicked!")
//            findNavController().navigate(R.id.navigation_create_report_police)
//        }

        binding.casesHandledHistory.setOnClickListener {
            Log.d("HomeFragment", "Create Report clicked!")
            findNavController().navigate(R.id.navigation_cases_handled_history)
        }
    }

    private fun ambilTokenSession(): String? {
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
