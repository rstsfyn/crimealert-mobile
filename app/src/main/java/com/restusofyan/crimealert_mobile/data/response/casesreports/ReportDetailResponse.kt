package com.restusofyan.crimealert_mobile.data.response.casesreports

import com.google.gson.annotations.SerializedName

data class ReportDetailResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: ReportDetailData? = null
)

data class ReportDetailData(
    @field:SerializedName("id_report")
    val idReport: Int? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("picture")
    val picture: String? = null,

    @field:SerializedName("status_kasus")
    val statusKasus: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("User")
    val user: ReportUser? = null,

    @field:SerializedName("Map")
    val map: ReportMap? = null,

    @field:SerializedName("statusHistory")
    val statusHistory: List<StatusHistoryItem>? = null
)

data class ReportUser(
    @field:SerializedName("id_user")
    val idUser: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("avatar")
    val avatar: String? = null
)

data class ReportMap(
    @field:SerializedName("id_maps")
    val idMaps: Int? = null,

    @field:SerializedName("latitude")
    val latitude: Double? = null,

    @field:SerializedName("longitude")
    val longitude: Double? = null
)

data class StatusHistoryItem(
    @field:SerializedName("id_history")
    val idHistory: Int? = null,

    @field:SerializedName("status_kasus")
    val statusKasus: String? = null,

    @field:SerializedName("evidence_photo")
    val evidencePhoto: String? = null,

    @field:SerializedName("notes")
    val notes: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("updater")
    val updater: StatusUpdater? = null
)

data class StatusUpdater(
    @field:SerializedName("id_user")
    val idUser: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("role")
    val role: String? = null
)
