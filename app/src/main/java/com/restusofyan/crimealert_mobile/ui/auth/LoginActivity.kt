package com.restusofyan.crimealert_mobile.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.response.login.LoginResponse
import com.restusofyan.crimealert_mobile.ui.customview.EmailEditText
import com.restusofyan.crimealert_mobile.ui.customview.PasswordEditText
import com.restusofyan.crimealert_mobile.ui.police.PoliceMainActivity
import com.restusofyan.crimealert_mobile.ui.users.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EmailEditText
    private lateinit var passwordEditText: PasswordEditText
    private lateinit var registerTextView: android.widget.TextView
    private lateinit var loginButton: android.widget.Button

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.ed_login_email)
        passwordEditText = findViewById(R.id.ed_login_password)
        loginButton = findViewById(R.id.btnLogin)
        registerTextView = findViewById(R.id.register_account)

        setupKlikRegister()
        setupLoginButton()
        observeLogin()
    }

    private fun setupLoginButton() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (emailEditText.error != null || passwordEditText.error != null) {
                Toast.makeText(this, "Periksa kembali email dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Panggil login lewat ViewModel
            authViewModel.login(email, password)
        }
    }

    private fun observeLogin() {
        lifecycleScope.launchWhenStarted {
            authViewModel.loginResponse.collectLatest { response ->
                response?.let {
                    if (it.isSuccessful && it.body() != null) {
                        handleLoginSuccess(it.body()!!)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login gagal: ${it.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun handleLoginSuccess(loginResponse: LoginResponse) {
        val loginResult = loginResponse.loginResult
        if (loginResult != null) {
            // Simpan token dan role ke SharedPreferences
            val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("token", loginResult.token)
                putString("role", loginResult.role)
                putString("name", loginResult.name)
                putString("email", loginResult.email)
                apply()
            }

            Toast.makeText(this, "Selamat datang, ${loginResult.name}", Toast.LENGTH_SHORT).show()

            when (loginResult.role) {
                "masyarakat" -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                "polisi" -> {
                    startActivity(Intent(this, PoliceMainActivity::class.java))
                    finish()
                }
                else -> {
                    Toast.makeText(this, "Role tidak dikenali", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Login gagal, data user tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupKlikRegister() {
        val text = "Belum memiliki akun?, klik sini!"
        val spannable = SpannableString(text)
        val start = text.indexOf("klik sini!")
        val end = start + "klik sini!".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.BLUE
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        registerTextView.text = spannable
        registerTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}
