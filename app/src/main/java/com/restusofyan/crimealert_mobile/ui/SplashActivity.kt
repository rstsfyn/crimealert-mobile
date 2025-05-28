package com.restusofyan.crimealert_mobile.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.ui.users.MainActivity
import com.restusofyan.crimealert_mobile.ui.police.PoliceMainActivity
import com.restusofyan.crimealert_mobile.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val role = sharedPref.getString("role", null)

        if (token != null && role != null) {
            when (role) {
                "masyarakat" -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                "polisi" -> {
                    startActivity(Intent(this, PoliceMainActivity::class.java))
                }
                else -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
