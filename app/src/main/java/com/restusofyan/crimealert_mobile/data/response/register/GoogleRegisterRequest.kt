package com.restusofyan.crimealert_mobile.data.response.register

data class GoogleRegisterRequest(
    val idToken: String,
    val email: String,
    val name: String,
    val photoUrl: String?,
    val role: String = "masyarakat"
)