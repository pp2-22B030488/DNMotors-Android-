package com.example.dnmotors.viewdealer.compose

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun DealerApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        DealerNavGraph(navController = navController, padding = padding)
    }
}