package com.example.dnmotors.viewdealer.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dnmotors.viewdealer.activity.DealerScreen
import com.example.dnmotors.viewdealer.compose.screen.MessagesScreen
import com.example.dnmotors.viewmodel.AuthViewModel

@Composable
fun DealerApp() {
    val navController = rememberNavController()
    val showBottomBar = rememberSaveable { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            if (showBottomBar.value) {
                BottomNavigationBar(navController)
            }
        }
    ) { padding ->
        DealerNavGraph(navController = navController, padding = padding, onToggleBottomBar = { showBottomBar.value = it }
        )
    }
}
