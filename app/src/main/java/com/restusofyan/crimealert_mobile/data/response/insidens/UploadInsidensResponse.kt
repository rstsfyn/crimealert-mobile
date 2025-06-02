package com.restusofyan.crimealert_mobile.data.response.insidens

import com.google.gson.annotations.SerializedName

data class UploadInsidensResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)