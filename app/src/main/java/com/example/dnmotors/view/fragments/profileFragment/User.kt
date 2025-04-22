package com.example.dnmotors.view.fragments.profileFragment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val company: String = "",

) : Parcelable