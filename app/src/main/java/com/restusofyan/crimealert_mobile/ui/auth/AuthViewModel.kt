package com.restusofyan.crimealert_mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.data.response.login.LoginRequest
import com.restusofyan.crimealert_mobile.data.response.login.LoginResponse
import com.restusofyan.crimealert_mobile.data.response.register.RegisterResponse
import com.restusofyan.crimealert_mobile.data.response.register.RegisterRequest
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

    fun register(name: String, phone: String, email: String, password: String) {
        val request = RegisterRequest(name, phone, email, password)
        viewModelScope.launch {
            val response = repository.registerUser(request)
            _registerResponse.value = response
        }
    }

    fun login(email: String, password: String) {
        val request = LoginRequest(email, password)
        viewModelScope.launch {
            val response = repository.loginUser(request)
            _loginResponse.value = response
        }
    }
}
