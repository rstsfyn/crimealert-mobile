package com.restusofyan.crimealert_mobile.data.response.profile

import com.google.gson.annotations.SerializedName

data class MyProfileResponse(

	@field:SerializedName("myProfileResult")
	val myProfileResult: MyProfileResult? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class MyProfileResult(

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id_user")
	val idUser: Int? = null,

	@field:SerializedName("avatar")
	val avatar: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)
