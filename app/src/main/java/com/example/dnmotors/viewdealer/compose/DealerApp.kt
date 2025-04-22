package com.example.dnmotors.viewdealer.compose

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.dnmotors.viewmodel.AuthViewModel

@Composable
fun DealerApp(){
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        DealerNavGraph(navController = navController, padding = padding)
    }
}