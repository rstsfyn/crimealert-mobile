package com.restusofyan.crimealert_mobile.ui.alluserpage.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.restusofyan.crimealert_mobile.data.notification.NotificationItem
import com.restusofyan.crimealert_mobile.data.notification.NotificationType
import com.restusofyan.crimealert_mobile.data.repository.NotificationRepository
import com.restusofyan.crimealert_mobile.databinding.FragmentNotificationsBinding
import com.restusofyan.crimealert_mobile.ui.adapter.NotificationAdapter
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import com.restusofyan.crimealert_mobile.ui.alluserpage.detailinsidens.DetailInsidensActivity
import com.restusofyan.crimealert_mobile.ui.police.detailcasespolice.DetailCasesPoliceActivity

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationRepository: NotificationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRepository()
        setupRecyclerView()
        setupClickListeners()
        loadNotifications()
        hideBottomNavigation()
        setupButton()
    }

    private fun setupButton() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun initRepository() {
        notificationRepository = NotificationRepository(requireContext())
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            context = requireContext(),
            notifications = emptyList(),
            onNotificationClick = { notification ->
                handleNotificationClick(notification)
            }
        )

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabClearAll.setOnClickListener {
            showClearAllDialog()
        }
    }

    private fun loadNotifications() {
        val notifications = notificationRepository.getNotifications()

        if (notifications.isEmpty()) {
            showEmptyState()
        } else {
            showNotifications(notifications)
        }
    }

    private fun showNotifications(notifications: List<NotificationItem>) {
        binding.apply {
            rvNotifications.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            fabClearAll.visibility = View.VISIBLE
        }

        notificationAdapter.updateNotifications(notifications)
    }

    private fun showEmptyState() {
        binding.apply {
            rvNotifications.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
            fabClearAll.visibility = View.GONE
        }
    }

    private fun handleNotificationClick(notification: NotificationItem) {
        notificationRepository.markAsRead(notification.id)
        val intent = when (notification.type) {
            NotificationType.INCIDENT -> {
                Intent(requireContext(), DetailInsidensActivity::class.java).apply {
                    putExtra("incident_id", notification.id)
                    putExtra("incident_title", notification.title)
                    putExtra("incident_latitude", notification.latitude)
                    putExtra("incident_longitude", notification.longitude)
                    putExtra("incident_time", notification.timestamp)
                    putExtra("incident_date", notification.timestamp)
                    putExtra("incident_description", notification.description)
                }
            }

            NotificationType.REPORT -> {
                val userRole = getUserRole()

                if (userRole == "polisi") {
                    Intent(requireContext(), DetailCasesPoliceActivity::class.java).apply {
                        putExtra("report_title", notification.title)
                        putExtra("report_description", notification.description)
                        putExtra("report_date", notification.timestamp)
                        putExtra("report_timestamp", notification.timestamp)
                        putExtra("report_latitude", notification.latitude)
                        putExtra("report_longitude", notification.longitude)
                        putExtra("status_kasus", notification.statusKasus)
                        putExtra("report_image_url", notification.imageUrl)
                    }
                } else {
                    Intent(requireContext(), DetailCasesActivity::class.java).apply {
                        putExtra("news_title", notification.title)
                        putExtra("news_description", notification.description)
                        putExtra("news_date", notification.timestamp)
                        putExtra("news_timestamp", notification.timestamp)
                        putExtra("news_latitude", notification.latitude)
                        putExtra("news_longitude", notification.longitude)
                        putExtra("news_image_url", notification.imageUrl)
                    }
                }
            }
        }

        startActivity(intent)
        loadNotifications()
    }

    private fun hideBottomNavigation() {
        (activity as? androidx.appcompat.app.AppCompatActivity)?.findViewById<View>(com.restusofyan.crimealert_mobile.R.id.nav_view)?.visibility = View.GONE
    }

    private fun getUserRole(): String? {
        return requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            .getString("role", null)
    }

    private fun showClearAllDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Clear All Notifications")
            .setMessage("Are you sure you want to clear all notifications? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                notificationRepository.clearAll()
                loadNotifications()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}