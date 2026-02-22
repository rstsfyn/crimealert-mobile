package com.restusofyan.crimealert_mobile.data.repository

import com.restusofyan.crimealert_mobile.data.api.ApiService
import com.restusofyan.crimealert_mobile.data.response.casesreports.CasesHandledReportResponse
import com.restusofyan.crimealert_mobile.data.response.casesreports.CasesReportResponse
import com.restusofyan.crimealert_mobile.data.response.casesreports.ReportDetailResponse
import com.restusofyan.crimealert_mobile.data.response.casesreports.UpdateStatusReportResponse
import com.restusofyan.crimealert_mobile.data.response.createreport.AddNewReportResponse
import com.restusofyan.crimealert_mobile.data.response.insidens.UploadInsidensRequest
import com.restusofyan.crimealert_mobile.data.response.insidens.UploadInsidensResponse
import com.restusofyan.crimealert_mobile.data.response.login.GoogleLoginRequest
import com.restusofyan.crimealert_mobile.data.response.login.LoginRequest
import com.restusofyan.crimealert_mobile.data.response.login.LoginResponse
import com.restusofyan.crimealert_mobile.data.response.profile.MyProfileResponse
import com.restusofyan.crimealert_mobile.data.response.profile.UpdateAvatarRequest
import com.restusofyan.crimealert_mobile.data.response.profile.UpdateAvatarResponse
import com.restusofyan.crimealert_mobile.data.response.register.GoogleRegisterRequest
import com.restusofyan.crimealert_mobile.data.response.register.RegisterRequest
import com.restusofyan.crimealert_mobile.data.response.register.RegisterResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrimeAlertRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun loginUser(loginRequest: LoginRequest): Response<LoginResponse> {
        return try {
            apiService.loginUser(loginRequest)
        } catch (e: Exception) {
            throw Exception("Login failed: ${e.message}")
        }
    }

    suspend fun registerUser(registerRequest: RegisterRequest): Response<RegisterResponse> {
        return try {
            apiService.registerUser(registerRequest)
        } catch (e: Exception) {
            throw Exception("Registration failed: ${e.message}")
        }
    }

    suspend fun googleLogin(googleLoginRequest: GoogleLoginRequest): Response<LoginResponse> {
        return try {
            apiService.googleLogin(googleLoginRequest)
        } catch (e: Exception) {
            throw Exception("Google login failed: ${e.message}")
        }
    }

    suspend fun googleRegister(googleRegisterRequest: GoogleRegisterRequest): Response<RegisterResponse> {
        return try {
            apiService.googleRegister(googleRegisterRequest)
        } catch (e: Exception) {
            throw Exception("Google registration failed: ${e.message}")
        }
    }

    suspend fun getMyProfile(token: String): Response<MyProfileResponse> {
        return try {
            apiService.myProfile("Bearer $token")
        } catch (e: Exception) {
            throw Exception("Failed to fetch profile: ${e.message}")
        }
    }

    suspend fun getAllReports(token: String): Response<CasesReportResponse> {
        return try {
            apiService.getAllReports("Bearer $token")
        } catch (e: Exception) {
            throw Exception("Failed to fetch reports: ${e.message}")
        }
    }

    suspend fun getMyReports(token: String): Response<CasesReportResponse> {
        return try {
            apiService.getReportsMe("Bearer $token")
        } catch (e: Exception) {
            throw Exception("Failed to fetch my reports: ${e.message}")
        }
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
        return try {
            apiService.getHandledReportHistory("Bearer $token")
        } catch (e: Exception) {
            throw Exception("Failed to fetch handled history: ${e.message}")
        }
    }

    suspend fun getReportDetail(token: String, reportId: Int): Response<ReportDetailResponse> {
        return try {
            apiService.getReportDetail("Bearer $token", reportId)
        } catch (e: Exception) {
            throw Exception("Failed to fetch report detail: ${e.message}")
        }
    }

    suspend fun updateReportStatus(
        token: String, 
        reportId: Int, 
        status: String,
        evidencePhoto: MultipartBody.Part? = null,
        notes: String? = null
    ): Response<UpdateStatusReportResponse> {
        return try {
            val statusBody = status.toRequestBody("text/plain".toMediaType())
            val notesBody = notes?.toRequestBody("text/plain".toMediaType())
            
            apiService.updateStatus(
                "Bearer $token", 
                reportId, 
                statusBody,
                evidencePhoto,
                notesBody
            )
        } catch (e: Exception) {
            throw Exception("Failed to update status: ${e.message}")
        }
    }

    suspend fun uploadScreamDetection(
        token: String,
        voiceDetection: String,
        latitude: Double,
        longitude: Double
    ): Response<UploadInsidensResponse> {
        return try {
            val request = UploadInsidensRequest(
                voice_detection = voiceDetection,
                latitude = latitude,
                longitude = longitude
            )
            apiService.uploadInsidens("Bearer $token", request)
        } catch (e: Exception) {
            throw Exception("Failed to upload scream detection: ${e.message}")
        }
    }

    fun validateNewInsiden(
        token: String,
        title: RequestBody,
        description: RequestBody,
        latitude: RequestBody?,
        longitude: RequestBody?,
        picture: MultipartBody.Part,
        incidentId: RequestBody,
    ): Call<AddNewReportResponse> {
        return apiService.validateInsiden("Bearer $token", title, description, latitude, longitude, incidentId, picture)
    }

    suspend fun updateAvatar(token: String, userId: Int, avatarRequest: UpdateAvatarRequest): Response<UpdateAvatarResponse> {
        return try {
            apiService.updateAvatar("Bearer $token", userId, avatarRequest)
        } catch (e: Exception) {
            throw Exception("Failed to update avatar: ${e.message}")
        }
    }
}