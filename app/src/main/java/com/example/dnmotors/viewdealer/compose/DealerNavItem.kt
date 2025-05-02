package com.example.dnmotors.viewdealer.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class DealerNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Cars : DealerNavItem("cars", Icons.Default.Home, "Cars")
    object Chat : DealerNavItem("chat", Icons.Default.Email, "Chat")
    object Profile : DealerNavItem("profile", Icons.Default.Person, "Profile")
    object EditProfile : DealerNavItem("edit_profile_screen", Icons.Default.Person, "Edit Profile")
    object ChangePassword : DealerNavItem("change_password", Icons.Default.Lock, "Change Password")

}
