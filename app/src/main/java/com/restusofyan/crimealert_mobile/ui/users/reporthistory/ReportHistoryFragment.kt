package com.restusofyan.crimealert_mobile.ui.users.reporthistory

import android.content.Context
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.model.CasesModel
import com.restusofyan.crimealert_mobile.databinding.FragmentNewsBinding
import com.restusofyan.crimealert_mobile.databinding.FragmentReportHistoryBinding
import com.restusofyan.crimealert_mobile.ui.adapter.CasesAdapter
import com.restusofyan.crimealert_mobile.ui.adapter.NewsAdapter
import com.restusofyan.crimealert_mobile.ui.police.detailcasespolice.DetailCasesPoliceActivity
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import com.restusofyan.crimealert_mobile.ui.users.news.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportHistoryFragment : Fragment() {

    private var _binding: FragmentReportHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var casesAdapter: CasesAdapter
    private val viewModel: ReportHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupButton()

        val token = ambilTokenSession()
        if (token != null) {
            viewModel.fetchReports(token)
        } else {
            Log.e("NewsFragment", "Token user tidak ditemukan")
        }
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

        binding.rvReporthistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = casesAdapter
        }
    }

    private fun setupObservers() {
        viewModel.reports.observe(viewLifecycleOwner) { data ->
            casesAdapter.updateData(data)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Log.e("NewsFragment", it)
            }
        }
    }

    private fun ambilTokenSession(): String? {
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    private fun setupButton() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
