package com.example.dnmotors.viewdealer.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dnmotors.viewdealer.compose.screen.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DealerNavGraph(
    navController: NavHostController,
    padding: PaddingValues,
    onToggleBottomBar: (Boolean) -> Unit
) {

    NavHost(navController, startDestination = DealerNavItem.Cars.route) {
        composable(DealerNavItem.Cars.route) {
            DealerCarsScreen(navController, padding)
        }
        composable("dealer_car_details/{vin}") { backStackEntry ->
            val vin = backStackEntry.arguments?.getString("vin")
            vin?.let {
                DealerCarDetailsScreen(carId = it)
            }
        }
        composable("add_car") { AddCarScreen() }

        composable(DealerNavItem.Chat.route) {
            ChatScreen { chatItem ->
                navController.navigate("messages/${chatItem.userId}/${chatItem.carId}")
            }
        }

        composable("messages/{userId}/{vin}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val carId = backStackEntry.arguments?.getString("vin") ?: return@composable
            val dealerId = FirebaseAuth.getInstance().currentUser?.uid
            val dealerName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Dealer"

            if (dealerId != null) {
                MessagesScreen(
                    chatId = "${dealerId}_${userId}",
                    carId = carId,
                    userId = userId,
                    dealerId = dealerId,
                    dealerName = dealerName,
                    onToggleBottomBar = onToggleBottomBar
                )
            } else {
                Text("Error: Dealer not logged in.")
            }
        }

        composable(DealerNavItem.Profile.route) {
            DealerProfileScreen(navController = navController)
        }

        composable("edit_profile_screen") {
            EditProfileScreen(navController = navController)
            }
        composable("change_password") {
            ChangePasswordScreen(navController = navController)
        }

    }

    }
