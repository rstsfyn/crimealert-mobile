package com.restusofyan.crimealert_mobile.ui.users.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.restusofyan.crimealert_mobile.databinding.FragmentNewsBinding
import com.restusofyan.crimealert_mobile.ui.adapter.NewsAdapter
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsFragment : Fragment() {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var newsAdapter: NewsAdapter
    private val viewModel: NewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
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
        newsAdapter = NewsAdapter(emptyList()) { selectedNews ->
            val intent = Intent(requireContext(), DetailCasesActivity::class.java).apply {
                putExtra("report_id", selectedNews.idReport)
            }
            startActivity(intent)
        }

        binding.rvLatestnews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun setupObservers() {
        viewModel.reports.observe(viewLifecycleOwner) { data ->
            newsAdapter.updateData(data)
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
