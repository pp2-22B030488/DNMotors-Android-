package com.example.dnmotors.viewdealer.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dnmotors.viewdealer.compose.screen.*

@Composable
fun DealerNavGraph(navController: NavHostController, padding: PaddingValues) {
    NavHost(navController, startDestination = DealerNavItem.Cars.route) {
        composable(DealerNavItem.Cars.route) { DealerCarsScreen(navController) }
        composable("dealer_car_details/{carId}") { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")
            carId?.let {
                DealerCarDetailsScreen(carId = it)
            }
        }
        composable("add_car") { AddCarScreen() }
        composable(DealerNavItem.Chat.route) { ChatScreen() }
        composable(DealerNavItem.Profile.route) { DealerProfileScreen() }

    }
}