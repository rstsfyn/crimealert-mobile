package com.restusofyan.crimealert_mobile.data.response.login

data class GoogleLoginRequest(
    val idToken: String,
    val email: String,
    val name: String,
    val photoUrl: String?
)