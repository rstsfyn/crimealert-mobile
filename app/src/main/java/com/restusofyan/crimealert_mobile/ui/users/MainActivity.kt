package com.restusofyan.crimealert_mobile.ui.users

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.databinding.ActivityMainBinding
import com.restusofyan.crimealert_mobile.ui.auth.LoginActivity
import com.restusofyan.crimealert_mobile.utils.SocketManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var socketManager: SocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val role = sharedPref.getString("role", null)

        if (token.isNullOrEmpty() || role != "masyarakat") {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_news, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        socketManager = SocketManager(this)
        socketManager.initializeSocket()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::socketManager.isInitialized) {
            socketManager.disconnect()
        }
    }
}