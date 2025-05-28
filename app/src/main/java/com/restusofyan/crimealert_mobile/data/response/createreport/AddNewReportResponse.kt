package com.restusofyan.crimealert_mobile.data.response.createreport

import com.google.gson.annotations.SerializedName

data class AddNewReportResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)
