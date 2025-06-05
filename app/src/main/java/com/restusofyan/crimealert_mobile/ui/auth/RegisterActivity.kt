package com.restusofyan.crimealert_mobile.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.ui.customview.EmailEditText
import com.restusofyan.crimealert_mobile.ui.customview.PasswordEditText
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope


@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var loginTextView: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val edName = findViewById<TextInputEditText>(R.id.ed_register_name)
        val edPhone = findViewById<TextInputEditText>(R.id.ed_phonenumber_register)
        val edEmail = findViewById<EmailEditText>(R.id.ed_login_email)
        val edPassword = findViewById<PasswordEditText>(R.id.ed_login_password)
        val edConfirmPassword = findViewById<PasswordEditText>(R.id.ed_confirm_password)
        val btnRegister = findViewById<Button>(R.id.btnLogin)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        loginTextView = findViewById(R.id.klik_login)

        setupKlikLogin()

        backButton.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val name = edName.text.toString()
            val phone = edPhone.text.toString()
            val email = edEmail.text.toString()
            val password = edPassword.text.toString()
            val confirmPassword = edConfirmPassword.text.toString()

            if (password != confirmPassword) {
                Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.register(name, phone, email, password)
        }

        lifecycleScope.launchWhenStarted {
            authViewModel.registerResponse.collect { response ->
                response?.let {
                    if (it.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registrasi berhasil, silakan login", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = it.errorBody()?.string() ?: "Terjadi kesalahan"
                        Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupKlikLogin() {
        val text = "Sudah punya akun?, klik sini!"
        val spannable = SpannableString(text)
        val start = text.indexOf("klik sini!")
        val end = start + "klik sini!".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.BLUE
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginTextView.text = spannable
        loginTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}
