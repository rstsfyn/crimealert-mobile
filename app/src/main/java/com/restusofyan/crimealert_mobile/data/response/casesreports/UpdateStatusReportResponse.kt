package com.restusofyan.crimealert_mobile.data.response.casesreports

import com.google.gson.annotations.SerializedName

data class UpdateStatusReportResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)
