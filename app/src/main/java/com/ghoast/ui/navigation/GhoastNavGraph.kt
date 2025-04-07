package com.ghoast.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ghoast.ui.home.OffersHomeScreen
import com.ghoast.ui.login.LoginScreen
import com.ghoast.ui.register.RegisterUserScreen
import com.ghoast.ui.register.RegisterShopScreen
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.shop.AddOfferScreen

@Composable
fun GhoastNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.OffersHome.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.RegisterUser.route) {
            RegisterUserScreen(navController)
        }
        composable(Screen.RegisterShop.route) {
            RegisterShopScreen(navController)
        }
        composable(Screen.OffersHome.route) {
            OffersHomeScreen(navController)
        }
        composable(Screen.AddOffer.route) {
            AddOfferScreen(navController)
        }

        // 👉 Εδώ μπορείς να προσθέσεις κι άλλες διαδρομές όταν χρειαστεί
    }
}
