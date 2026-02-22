package com.restusofyan.crimealert_mobile.data.response.casesreports

import com.google.gson.annotations.SerializedName

data class UpdateStatusRequest(
    @SerializedName("status_kasus")
    val statusKasus: String,
    
    @SerializedName("evidence_photo")
    val evidencePhoto: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)