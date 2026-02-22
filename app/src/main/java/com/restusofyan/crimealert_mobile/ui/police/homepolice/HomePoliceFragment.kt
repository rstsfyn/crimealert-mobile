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
import com.restusofyan.crimealert_mobile.ui.alluserpage.crimepronearea.CrimeProneAreaActivity
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
        val userAvatar = sharedPref.getString("avatar", null)

        binding.tvName.text = userName ?: "User"

        Glide.with(this)
            .load(userAvatar)
            .placeholder(R.drawable.ladushing)
            .error(R.drawable.ladushing)
            .into(binding.ivAvatar)
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
            data?.let {
                val listReports = it.filterNotNull().take(5)
                casesAdapter.updateData(listReports)
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

    private fun ambilDataCases() {
        val token = ambilTokenSession()
        if (token != null) {
            viewModel.fetchReports(token)
        } else {
            Log.e("NewsFragment", "Token user tidak ditemukan")
        }
    }


    private fun setupButton() {
        binding.icNotification.setOnClickListener{
            findNavController().navigate(R.id.notificationPolice)
        }

        binding.incomingcases.setOnClickListener {
            Log.d("HomeFragment", "Create Incoming cases")
            findNavController().navigate(R.id.navigation_incoming_cases)
        }

        binding.casesHandledHistory.setOnClickListener {
            Log.d("HomeFragment", "Create Report clicked!")
            findNavController().navigate(R.id.navigation_cases_handled_history)
        }

        binding.tvTrendingcasesSeeall.setOnClickListener {
            Log.d("HomeFragment", "See all clicked!")
            findNavController().navigate(R.id.navigation_caseslist_police)
        }

        binding.cvCrimeProneArea.setOnClickListener {
            Log.d("HomeFragment", "Crime Prone Area clicked!")
            val intent = Intent(requireContext(), CrimeProneAreaActivity::class.java)
            startActivity(intent)
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
