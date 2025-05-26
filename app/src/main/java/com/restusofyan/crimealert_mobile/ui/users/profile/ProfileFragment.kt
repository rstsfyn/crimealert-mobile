package com.restusofyan.crimealert_mobile.ui.users.profile

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.FragmentProfileBinding
import com.restusofyan.crimealert_mobile.ui.auth.LoginActivity
import com.restusofyan.crimealert_mobile.utils.VoiceDetectionService
import kotlin.math.log

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchVoiceDetection = binding.root.findViewById<Switch>(R.id.switchVoiceDetection)

        switchVoiceDetection.isChecked = isServiceRunning(VoiceDetectionService::class.java)
        switchVoiceDetection.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startVoiceDetectionService()
            } else {
                stopVoiceDetectionService()
            }
        }
        logout()
    }

    private fun startVoiceDetectionService() {
        val startServiceIntent = Intent(requireContext(), VoiceDetectionService::class.java)
        requireActivity().startService(startServiceIntent)
    }

    private fun stopVoiceDetectionService() {
        val stopServiceIntent = Intent(requireContext(), VoiceDetectionService::class.java)
        requireActivity().stopService(stopServiceIntent)
    }

    private fun logout() {
        binding.btnLogout.setOnClickListener{
            hapusSession()
        }
    }


    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        return activityManager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hapusSession() {
        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
