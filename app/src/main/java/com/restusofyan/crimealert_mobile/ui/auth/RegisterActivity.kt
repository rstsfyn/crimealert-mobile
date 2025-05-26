package com.restusofyan.crimealert_mobile.ui.auth

import android.content.Intent
import android.os.Bundle
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
}
