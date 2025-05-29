package com.restusofyan.crimealert_mobile.data.repository

import com.restusofyan.crimealert_mobile.data.api.ApiService
import com.restusofyan.crimealert_mobile.data.response.casesreports.CasesHandledReportResponse
import com.restusofyan.crimealert_mobile.data.response.casesreports.CasesReportResponse
import com.restusofyan.crimealert_mobile.data.response.casesreports.UpdateStatusReportResponse
import com.restusofyan.crimealert_mobile.data.response.casesreports.UpdateStatusRequest
import com.restusofyan.crimealert_mobile.data.response.createreport.AddNewReportResponse
import com.restusofyan.crimealert_mobile.data.response.login.LoginRequest
import com.restusofyan.crimealert_mobile.data.response.login.LoginResponse
import com.restusofyan.crimealert_mobile.data.response.profile.MyProfileResponse
import com.restusofyan.crimealert_mobile.data.response.register.RegisterRequest
import com.restusofyan.crimealert_mobile.data.response.register.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
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

    suspend fun getMyReports(token: String): Response<CasesReportResponse> {
        return apiService.getReportsMe("Bearer $token")
    }

    fun createNewReport(
        token: String,
        title: RequestBody,
        description: RequestBody,
        latitude: RequestBody?,
        longitude: RequestBody?,
        picture: MultipartBody.Part,
    ): Call<AddNewReportResponse> {
        return apiService.createReport("Bearer $token", title, description, latitude, longitude, picture)
    }

    suspend fun getHandledHistory(token: String): Response<CasesHandledReportResponse> {
        return apiService.getHandledReportHistory("Bearer $token")
    }

    suspend fun updateReportStatus(token: String, reportId: Int, status: String): Response<UpdateStatusReportResponse> {
        return apiService.updateStatus("Bearer $token", reportId, UpdateStatusRequest(status))
    }

}
