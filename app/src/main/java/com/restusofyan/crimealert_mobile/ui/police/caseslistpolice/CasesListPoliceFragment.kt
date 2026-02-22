package com.restusofyan.crimealert_mobile.ui.police.caseslistpolice

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.FragmentCaseslistPoliceBinding
import com.restusofyan.crimealert_mobile.databinding.FragmentNewsBinding
import com.restusofyan.crimealert_mobile.ui.adapter.CasesAdapter
import com.restusofyan.crimealert_mobile.ui.police.detailcasespolice.DetailCasesPoliceActivity
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import com.restusofyan.crimealert_mobile.ui.users.news.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CasesListPoliceFragment : Fragment() {

    private var _binding: FragmentCaseslistPoliceBinding? = null
    private val binding get() = _binding!!

    private lateinit var casesAdapter: CasesAdapter
    private val viewModel: NewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaseslistPoliceBinding.inflate(inflater, container, false)
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
            val intent = Intent(requireContext(), DetailCasesPoliceActivity::class.java).apply {
                putExtra("report_id", selectedNews.idReport)
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
            casesAdapter.updateData(data)
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
        _binding = null
    }
}
