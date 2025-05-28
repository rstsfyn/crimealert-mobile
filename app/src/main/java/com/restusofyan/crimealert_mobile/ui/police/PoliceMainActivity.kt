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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PoliceMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPoliceMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val role = sharedPref.getString("role", null)

        Log.d("PoliceMainActivity", "Token loaded: $token")

        if (token.isNullOrEmpty() || role != "polisi") {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        supportActionBar?.hide()

        binding = ActivityPoliceMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_police_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home_police, R.id.navigation_caseslist_police)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Custom BottomNavigationView logic
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home_police, R.id.navigation_caseslist_police -> {
                    navController.navigate(item.itemId)
                    true
                }
                R.id.logout -> {
                    sharedPref.edit().clear().apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

}