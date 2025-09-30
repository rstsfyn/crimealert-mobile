package com.restusofyan.crimealert_mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.data.response.login.LoginRequest
import com.restusofyan.crimealert_mobile.data.response.login.LoginResponse
import com.restusofyan.crimealert_mobile.data.response.login.GoogleLoginRequest
import com.restusofyan.crimealert_mobile.data.response.register.RegisterResponse
import com.restusofyan.crimealert_mobile.data.response.register.RegisterRequest
import com.restusofyan.crimealert_mobile.data.response.register.GoogleRegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: CrimeAlertRepository
) : ViewModel() {

    private val _loginResponse = MutableStateFlow<Response<LoginResponse>?>(null)
    val loginResponse: StateFlow<Response<LoginResponse>?> = _loginResponse

    private val _registerResponse = MutableStateFlow<Response<RegisterResponse>?>(null)
    val registerResponse: StateFlow<Response<RegisterResponse>?> = _registerResponse

    private val _googleLoginResponse = MutableStateFlow<Response<LoginResponse>?>(null)
    val googleLoginResponse: StateFlow<Response<LoginResponse>?> = _googleLoginResponse

    private val _googleRegisterResponse = MutableStateFlow<Response<RegisterResponse>?>(null)
    val googleRegisterResponse: StateFlow<Response<RegisterResponse>?> = _googleRegisterResponse

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun register(name: String, phone: String, email: String, password: String) {
        val request = RegisterRequest(name, phone, email, password)
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.registerUser(request)
                _registerResponse.value = response
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        val request = LoginRequest(email, password)
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.loginUser(request)
                _loginResponse.value = response
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun googleLogin(idToken: String, email: String, name: String, photoUrl: String?) {
        val request = GoogleLoginRequest(idToken, email, name, photoUrl)
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.googleLogin(request)
                _googleLoginResponse.value = response
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun googleRegister(idToken: String, email: String, name: String, photoUrl: String?, role: String = "masyarakat") {
        val request = GoogleRegisterRequest(idToken, email, name, photoUrl, role)
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.googleRegister(request)
                _googleRegisterResponse.value = response
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResponses() {
        _loginResponse.value = null
        _registerResponse.value = null
        _googleLoginResponse.value = null
        _googleRegisterResponse.value = null
    }
}