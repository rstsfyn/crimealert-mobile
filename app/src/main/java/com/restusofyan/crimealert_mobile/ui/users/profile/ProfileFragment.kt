package com.restusofyan.crimealert_mobile.ui.users.profile

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.FragmentProfileBinding
import com.restusofyan.crimealert_mobile.ui.auth.LoginActivity
import com.restusofyan.crimealert_mobile.utils.VoiceDetectionService
import dagger.hilt.android.AndroidEntryPoint
import com.restusofyan.crimealert_mobile.ui.customview.CustomDialogLogoutFragment

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = ambilTokenSession()
        if (token != null) {
            viewModel.loadMyProfile(token)
        }
        observeProfile()
        logout()
        setupSwitch()
        setupButton()
    }

    override fun onResume() {
        super.onResume()
        updateSwitchState()
    }

    private fun updateSwitchState() {
        val switchVoiceDetection = binding.root.findViewById<Switch>(R.id.switchVoiceDetection)
        switchVoiceDetection.isChecked = isServiceRunning(VoiceDetectionService::class.java)
    }

    private fun setupSwitch() {
        val switchVoiceDetection = binding.root.findViewById<Switch>(R.id.switchVoiceDetection)
        
        switchVoiceDetection.isChecked = isServiceRunning(VoiceDetectionService::class.java)

        switchVoiceDetection.setOnCheckedChangeListener { _, isChecked ->
            Log.d("ProfileFragment", "Switch changed to: $isChecked")
            if (isChecked) {
                startVoiceDetectionService()
                Toast.makeText(requireContext(), "Voice detection enabled", Toast.LENGTH_SHORT).show()
            } else {
                stopVoiceDetectionService()
                Toast.makeText(requireContext(), "Voice detection disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeProfile() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.textName.text = it.name ?: "Nama tidak ditemukan"
                binding.textEmail.text = it.email ?: "Email tidak ditemukan"
                binding.tvPhone.text = it.phone ?: "-"
                val avatarUrl = it.avatar?.replace("localhost", "10.0.2.2")
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.profilephoto)
                    .error(R.drawable.profilephoto)
                    .into(binding.profileImage)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startVoiceDetectionService() {
        try {
            val startServiceIntent = Intent(requireContext(), VoiceDetectionService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                requireActivity().startForegroundService(startServiceIntent)
            } else {
                requireActivity().startService(startServiceIntent)
            }
            Log.d("ProfileFragment", "VoiceDetectionService start requested")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error starting VoiceDetectionService", e)
            Toast.makeText(requireContext(), "Failed to start voice detection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopVoiceDetectionService() {
        try {
            val stopServiceIntent = Intent(requireContext(), VoiceDetectionService::class.java)
            val stopped = requireActivity().stopService(stopServiceIntent)
            Log.d("ProfileFragment", "VoiceDetectionService stop requested, result: $stopped")

            if (!stopped) {
                Log.w("ProfileFragment", "Service didn't stop gracefully, checking if still running...")
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (isServiceRunning(VoiceDetectionService::class.java)) {
                        Log.w("ProfileFragment", "Service still running after stop request")
                    }
                }, 1000)
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error stopping VoiceDetectionService", e)
            Toast.makeText(requireContext(), "Error stopping voice detection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        val dialog = CustomDialogLogoutFragment()
        dialog.onYesClick = {
            if (isServiceRunning(VoiceDetectionService::class.java)) {
                stopVoiceDetectionService()
            }
            hapusSession()
        }
        dialog.onNoClick = {
            Toast.makeText(requireContext(), "Logout dibatalkan", Toast.LENGTH_SHORT).show()
        }
        dialog.show(parentFragmentManager, "CustomDialogLogoutFragment")
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        return try {
            val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            activityManager.getRunningServices(Int.MAX_VALUE)
                .any { it.service.className == serviceClass.name }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error checking if service is running", e)
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun ambilTokenSession(): String? {
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    private fun hapusSession() {
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupButton() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}