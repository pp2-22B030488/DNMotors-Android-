package com.example.dnmotors.viewdealer.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dnmotors.viewdealer.compose.screen.*
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.model.AuthUser
import com.google.firebase.auth.FirebaseAuth
//import com.example.changepassword.ChangePasswordScreen

@Composable
fun DealerNavGraph(
    navController: NavHostController,
    padding: PaddingValues,
    onToggleBottomBar: (Boolean) -> Unit,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
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
        composable("add_car") { AddCarScreen(navController = navController) }

        composable(DealerNavItem.Chat.route) {
            ChatScreen(chatViewModel, authViewModel) { chatItem ->
//                navController.navigate("messages/${chatItem.userId}/${chatItem.carId}")
                navController.navigate("messages/${chatItem.userId}/${chatItem.carId}/${chatItem.dealerId}/${chatItem.dealerName}")

            }
        }

        composable("messages/{userId}/{vin}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val carId = backStackEntry.arguments?.getString("vin") ?: return@composable
            val dealerId = FirebaseAuth.getInstance().currentUser?.uid
            val dealerName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Dealer"

            if (dealerId != null) {
                MessagesScreen(
                    chatId = "${carId}_${dealerId}_${userId}",
                    carId = carId,
                    userId = userId,
                    dealerId = dealerId,
                    dealerName = dealerName,
                    onToggleBottomBar = onToggleBottomBar,
                    viewModel = chatViewModel
                )
            } else {
                Text("Error: Dealer not logged in.")
            }
        }
        composable(
            route = "messages/{userId}/{carId}/{dealerId}/{dealerName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("carId") { type = NavType.StringType },
                navArgument("dealerId") { type = NavType.StringType },
                navArgument("dealerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")!!
            val carId = backStackEntry.arguments?.getString("carId")!!
            val dealerId = backStackEntry.arguments?.getString("dealerId")!!
            val dealerName = backStackEntry.arguments?.getString("dealerName")!!

            val chatId = "${carId}_${dealerId}_${userId}"

            MessagesScreen(
                chatId = chatId,
                carId = carId,
                userId = userId,
                dealerId = dealerId,
                dealerName = dealerName,
                viewModel = chatViewModel,
                onToggleBottomBar = onToggleBottomBar
            )
        }

        composable(DealerNavItem.Profile.route) {
            DealerProfileScreen(navController = navController)
        }

        composable("edit_profile_screen") {
            EditProfileScreen(navController = navController)
            }
        composable("change_password") {
//            ChangePasswordScreen(navController = navController)
//            ChangePasswordScreen() // Библиотечная функция не требует NavController

        }

    }

}
