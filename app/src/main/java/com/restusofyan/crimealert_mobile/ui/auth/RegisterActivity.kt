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
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.ui.customview.EmailEditText
import com.restusofyan.crimealert_mobile.ui.customview.PasswordEditText
import com.restusofyan.crimealert_mobile.utils.GoogleSignInHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var loginTextView: android.widget.TextView
    private lateinit var googleRegisterButton: LinearLayout
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

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
        googleRegisterButton = findViewById(R.id.btn_registerwithgoogle)

        setupGoogleSignInLauncher()
        setupKlikLogin()
        setupGoogleRegisterButton()

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

        lifecycleScope.launchWhenStarted {
            authViewModel.googleRegisterResponse.collect { response ->
                response?.let {
                    if (it.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registrasi Google berhasil, silakan login", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = it.errorBody()?.string() ?: "Terjadi kesalahan pada registrasi Google"
                        Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        }
    }

    private fun setupGoogleRegisterButton() {
        googleRegisterButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val googleSignInClient = GoogleSignInHelper.getGoogleSignInClient(this)

        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }


    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken
            val email = account.email
            val displayName = account.displayName
            val photoUrl = account.photoUrl?.toString()

            if (idToken != null && email != null && displayName != null) {
                authViewModel.googleRegister(idToken, email, displayName, photoUrl)
            } else {
                Toast.makeText(this, "Failed to get Google account information", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
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