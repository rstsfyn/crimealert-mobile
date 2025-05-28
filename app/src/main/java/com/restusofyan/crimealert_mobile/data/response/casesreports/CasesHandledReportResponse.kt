package com.restusofyan.crimealert_mobile.data.response.casesreports

import com.google.gson.annotations.SerializedName

data class CasesHandledReportResponse(

	@field:SerializedName("listHandledReports")
	val listHandledReports: List<ListHandledReportsItem?>? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class ListHandledReportsItem(

	@field:SerializedName("User")
	val user: User? = null,

	@field:SerializedName("handled_by")
	val handledBy: Int? = null,

	@field:SerializedName("status_kasus")
	val statusKasus: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id_report")
	val idReport: Int? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("Map")
	val map: Map? = null,

	@field:SerializedName("picture")
	val picture: String? = null
)