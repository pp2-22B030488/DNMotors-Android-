package com.example.dnmotors.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val company: String = "",
    val location: String = "",
    val phoneNumber: String = "",
    val avatarUrl: String = "",
    val profileFon: String = ""

) : Parcelable