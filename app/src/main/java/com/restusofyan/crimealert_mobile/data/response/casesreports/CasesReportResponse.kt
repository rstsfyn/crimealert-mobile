package com.restusofyan.crimealert_mobile.data.response.casesreports

import com.google.gson.annotations.SerializedName

data class CasesReportResponse(

	@field:SerializedName("listReports")
	val listReports: List<ListReportsItem?>? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class ListReportsItem(

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

data class User(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("avatar")
	val avatar: String? = null,

	@field:SerializedName("id_user")
	val idUser: Int? = null
)

data class Map(

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("id_maps")
	val idMaps: Int? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null
)
