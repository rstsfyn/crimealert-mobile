package com.restusofyan.crimealert_mobile.ui.police.caseshandled

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.model.CasesModel
import com.restusofyan.crimealert_mobile.databinding.FragmentCasesHandledHistoryBinding
import com.restusofyan.crimealert_mobile.databinding.FragmentReportHistoryBinding
import com.restusofyan.crimealert_mobile.ui.adapter.CasesAdapter
import com.restusofyan.crimealert_mobile.ui.adapter.CasesHandledAdapter
import com.restusofyan.crimealert_mobile.ui.police.detailcasespolice.DetailCasesPoliceActivity
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import com.restusofyan.crimealert_mobile.ui.users.news.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CasesHandledHistoryFragment : Fragment() {

    private var _binding: FragmentCasesHandledHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var casesHandledAdapter: CasesHandledAdapter
    private val viewModel: CasesHandledHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCasesHandledHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.findViewById<View>(R.id.nav_view)?.visibility =
            View.GONE

        setupRecyclerView()
        setupButton()
        setupObservers()

        val token = ambilTokenSession()
        if (token != null) {
            viewModel.fetchReports(token)
        } else {
            Log.e("NewsFragment", "Token user tidak ditemukan")
        }
    }

    private fun setupRecyclerView() {
        casesHandledAdapter = CasesHandledAdapter(emptyList()) { selectedNews ->
            val intent = Intent(requireContext(), DetailCasesPoliceActivity::class.java).apply {
                putExtra("report_id", selectedNews.idReport)
                putExtra("report_title", selectedNews.title)
                putExtra("report_description", selectedNews.description)
                putExtra("report_image_url", selectedNews.picture)
                putExtra("report_timestamp", selectedNews.createdAt)
                putExtra("report_date", selectedNews.createdAt?.substringBefore("T"))
                putExtra("report_status", selectedNews.statusKasus)
                putExtra("report_latitude", selectedNews.map?.latitude)
                putExtra("report_longitude", selectedNews.map?.longitude)
                putExtra("avatar_reporter", selectedNews.user?.avatar)
                putExtra("name_reporter", selectedNews.user?.name)
            }
            startActivity(intent)
        }

        binding.rvCaseshandledhistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = casesHandledAdapter
        }
    }

    private fun setupObservers() {
        viewModel.reports.observe(viewLifecycleOwner) { data ->
            casesHandledAdapter.updateData(data)
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

        (activity as? AppCompatActivity)?.findViewById<View>(R.id.nav_view)?.visibility =
            View.VISIBLE
    }
}