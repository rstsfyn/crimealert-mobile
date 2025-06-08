package com.restusofyan.crimealert_mobile.ui.police

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.ActivityPoliceMainBinding
import com.restusofyan.crimealert_mobile.ui.auth.LoginActivity
import android.util.Log
import androidx.activity.enableEdgeToEdge
import com.restusofyan.crimealert_mobile.ui.customview.CustomDialogLogoutFragment
import com.restusofyan.crimealert_mobile.utils.SocketManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PoliceMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPoliceMainBinding
    private lateinit var socketManager: SocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val role = sharedPref.getString("role", null)

        Log.d("PoliceMainActivity", "Token loaded: $token")

        if (token.isNullOrEmpty() || role != "polisi") {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityPoliceMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_police_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home_police, R.id.navigation_caseslist_police)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home_police, R.id.navigation_caseslist_police -> {
                    navController.navigate(item.itemId)
                    true
                }
                R.id.logout -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }

        socketManager = SocketManager(this)
        socketManager.initializeSocket()
    }
    private fun showLogoutDialog() {
        val dialog = CustomDialogLogoutFragment()
        dialog.onYesClick = {
            val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        dialog.onNoClick = {
            Log.d("PoliceMainActivity", "Logout dibatalkan oleh user.")
        }
        dialog.show(supportFragmentManager, "LogoutDialog")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::socketManager.isInitialized) {
            socketManager.disconnect()
        }
    }
}