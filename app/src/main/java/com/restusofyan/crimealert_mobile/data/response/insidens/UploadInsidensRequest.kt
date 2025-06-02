package com.restusofyan.crimealert_mobile.data.response.insidens

data class UploadInsidensRequest(
    val voiceDetection: String,
    val latitude: Double,
    val longitude: Double
)