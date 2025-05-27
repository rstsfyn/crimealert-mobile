package com.restusofyan.crimealert_mobile.data.repository

import com.restusofyan.crimealert_mobile.data.api.ApiService
import com.restusofyan.crimealert_mobile.data.response.casesreports.CasesReportResponse
import com.restusofyan.crimealert_mobile.data.response.login.LoginRequest
import com.restusofyan.crimealert_mobile.data.response.login.LoginResponse
import com.restusofyan.crimealert_mobile.data.response.profile.MyProfileResponse
import com.restusofyan.crimealert_mobile.data.response.register.RegisterRequest
import com.restusofyan.crimealert_mobile.data.response.register.RegisterResponse
import retrofit2.Response
import javax.inject.Inject

class CrimeAlertRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun loginUser(loginRequest: LoginRequest): Response<LoginResponse> {
        return apiService.loginUser(loginRequest)
    }

    suspend fun registerUser(registerRequest: RegisterRequest): Response<RegisterResponse> {
        return apiService.registerUser(registerRequest)
    }

    suspend fun getMyProfile(token: String): Response<MyProfileResponse> {
        return apiService.myProfile("Bearer $token")
    }

    suspend fun getAllReports(token: String): Response<CasesReportResponse> {
        return apiService.getAllReports("Bearer $token")
    }

}
