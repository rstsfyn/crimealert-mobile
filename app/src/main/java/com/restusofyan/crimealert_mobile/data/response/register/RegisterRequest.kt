package com.restusofyan.crimealert_mobile.data.response.register

data class RegisterRequest (
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
)