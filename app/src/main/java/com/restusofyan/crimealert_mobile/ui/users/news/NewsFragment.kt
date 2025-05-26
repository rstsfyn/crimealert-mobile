package com.restusofyan.crimealert_mobile.ui.users.news

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.model.CasesModel
import com.restusofyan.crimealert_mobile.databinding.FragmentHomeBinding
import com.restusofyan.crimealert_mobile.databinding.FragmentNewsBinding
import com.restusofyan.crimealert_mobile.databinding.FragmentNotificationsPoliceBinding
import com.restusofyan.crimealert_mobile.ui.adapter.CasesAdapter
import com.restusofyan.crimealert_mobile.ui.adapter.NewsAdapter
import com.restusofyan.crimealert_mobile.ui.customview.CustomDialogVoiceDetectionFragment
import com.restusofyan.crimealert_mobile.ui.police.detailcasespolice.DetailCasesPoliceActivity
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity

class NewsFragment : Fragment() {

    private lateinit var newsAdapter: NewsAdapter
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButton()
    }

    private fun setupRecyclerView() {
        binding.rvLatestnews.layoutManager = LinearLayoutManager(requireContext())
        val casesList = createDummyData()
        Log.d("NewsFragment", "Jumlah data: ${casesList.size}")

        newsAdapter = NewsAdapter(casesList) { selectedNews ->
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

        binding.rvLatestnews.adapter = newsAdapter
    }

    private fun setupButton() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun createDummyData(): List<CasesModel> {
        return listOf(
            CasesModel(
                id = 1,
                title = "Keributan di Jalan Magelang",
                description = "Ada segerombolan remaja membawa sajam di area Jalan Magelang, dan selain itu juga meresahkan warga.",
                imageUrl = "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg",
                timestamp = "23:59",
                date = "Kamis, 13 Maret 2025",
                status = "Sudah Ditangani",
                latitude = -7.747033,
                longitude = 110.353738
            ),
            CasesModel(
                id = 2,
                title = "Kecelakaan di Jalan Sudirman",
                description = "Kecelakaan beruntun melibatkan 3 kendaraan terjadi di Jalan Sudirman pagi ini, menyebabkan kemacetan parah.",
                imageUrl = "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg",
                timestamp = "08:30",
                date = "Jumat, 14 Maret 2025",
                status = "Sudah Ditangani",
                latitude = -7.782916,
                longitude = 110.367744
            ),
            CasesModel(
                id = 3,
                title = "Festival Kuliner di Malioboro",
                description = "Festival kuliner tahunan kembali digelar di sepanjang Jalan Malioboro dengan lebih dari 50 stan makanan tradisional.",
                imageUrl = "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg",
                timestamp = "12:45",
                date = "Sabtu, 15 Maret 2025",
                status = "Sudah Ditangani",
                latitude = -7.793083,
                longitude = 110.363633
            ),
            CasesModel(
                id = 4,
                title = "Banjir di Kawasan Bantul",
                description = "Hujan deras sepanjang malam menyebabkan banjir di beberapa area di Kabupaten Bantul, ratusan rumah terendam.",
                imageUrl = "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg",
                timestamp = "06:15",
                date = "Minggu, 16 Maret 2025",
                status = "Sudah Ditangani",
                latitude = -7.888063,
                longitude = 110.325110
            ),
            CasesModel(
                id = 5,
                title = "Kebakaran di Pasar Beringharjo",
                description = "Api menghanguskan sekitar 15 kios di Pasar Beringharjo semalam, kerugian ditaksir mencapai ratusan juta rupiah.",
                imageUrl = "https://pidjar.com/wp-content/uploads/2020/01/klithih-ilustrasi.jpg",
                timestamp = "22:10",
                date = "Senin, 17 Maret 2025",
                status = "Sudah Ditangani",
                latitude = -7.800601,
                longitude = 110.367152
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
