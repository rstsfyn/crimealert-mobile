package com.restusofyan.crimealert_mobile.data.response.profile

import com.google.gson.annotations.SerializedName

data class UpdateAvatarResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)