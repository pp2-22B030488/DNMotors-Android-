package com.example.dnmotors.viewdealer.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.ai.client.generativeai.Chat

sealed class DealerNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Cars : DealerNavItem("cars", Icons.Default.Home, "Cars")
    object Chat : DealerNavItem("chat", Icons.Default.Email, "Chat")
    object Profile : DealerNavItem("profile", Icons.Default.Person, "Profile")
}
