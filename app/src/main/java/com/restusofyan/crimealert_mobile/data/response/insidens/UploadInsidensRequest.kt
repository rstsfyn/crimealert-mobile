package com.restusofyan.crimealert_mobile.data.response.insidens

data class UploadInsidensRequest(
    val voice_detection: String,
    val latitude: Double?,
    val longitude: Double?
)