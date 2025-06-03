package com.restusofyan.crimealert_mobile.data.api


import com.restusofyan.crimealert_mobile.data.response.casesreports.CasesHandledReportResponse
import com.restusofyan.crimealert_mobile.data.response.casesreports.CasesReportResponse
import com.restusofyan.crimealert_mobile.data.response.casesreports.UpdateStatusReportResponse
import com.restusofyan.crimealert_mobile.data.response.casesreports.UpdateStatusRequest
import com.restusofyan.crimealert_mobile.data.response.createreport.AddNewReportResponse
import com.restusofyan.crimealert_mobile.data.response.insidens.UploadInsidensRequest
import com.restusofyan.crimealert_mobile.data.response.insidens.UploadInsidensResponse
import com.restusofyan.crimealert_mobile.data.response.login.LoginRequest
import com.restusofyan.crimealert_mobile.data.response.login.LoginResponse
import com.restusofyan.crimealert_mobile.data.response.profile.MyProfileResponse
import com.restusofyan.crimealert_mobile.data.response.register.RegisterRequest
import com.restusofyan.crimealert_mobile.data.response.register.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

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

    @Multipart
    @POST("reports/")
    fun createReport(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part picture: MultipartBody.Part,
    ): Call<AddNewReportResponse>

    @GET("reports/me")
    suspend fun getReportsMe(
        @Header("Authorization") token: String,
    ) : Response<CasesReportResponse>

    @GET("reports/handled")
    suspend fun getHandledReportHistory(
        @Header("Authorization") token: String,
    ) : Response<CasesHandledReportResponse>

    @PUT("reports/{id}/status")
    suspend fun updateStatus(
        @Header("Authorization") token: String,
        @Path("id") reportId: Int,
        @Body statusBody: UpdateStatusRequest
    ): Response<UpdateStatusReportResponse>

    @POST("insidens/")
    suspend fun uploadInsidens(
        @Header("Authorization") token: String,
        @Body request: UploadInsidensRequest
    ): Response<UploadInsidensResponse>

    @Multipart
    @POST("reports/")
    fun validateInsiden(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("id_insiden") incidentId: RequestBody,
        @Part picture: MultipartBody.Part,
    ): Call<AddNewReportResponse>

}