package com.restusofyan.crimealert_mobile.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CasesModel(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val timestamp: String,
    val date: String,
    val status:String,
    val latitude:Double,
    val longitude:Double,
) : Parcelable
