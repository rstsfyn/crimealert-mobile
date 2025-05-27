package com.restusofyan.crimealert_mobile.data.api


import com.restusofyan.crimealert_mobile.data.response.casesreports.CasesReportResponse
import com.restusofyan.crimealert_mobile.data.response.login.LoginRequest
import com.restusofyan.crimealert_mobile.data.response.login.LoginResponse
import com.restusofyan.crimealert_mobile.data.response.profile.MyProfileResponse
import com.restusofyan.crimealert_mobile.data.response.register.RegisterRequest
import com.restusofyan.crimealert_mobile.data.response.register.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun loginUser(
        @Body loginData: LoginRequest
    ) : Response<LoginResponse>

    @POST("auth/register")
    suspend fun registerUser(
        @Body registerData: RegisterRequest
    ) : Response<RegisterResponse>

    @GET("users/me")
    suspend fun myProfile(
        @Header("Authorization") token: String,
    ) : Response<MyProfileResponse>

    @GET("reports/")
    suspend fun getAllReports(
        @Header("Authorization") token: String,
    ) : Response<CasesReportResponse>

}