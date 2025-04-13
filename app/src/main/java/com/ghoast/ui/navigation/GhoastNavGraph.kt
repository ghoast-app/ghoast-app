package com.ghoast.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ghoast.ui.home.OffersHomeScreen
import com.ghoast.ui.login.LoginScreen
import com.ghoast.ui.register.RegisterUserScreen
import com.ghoast.ui.register.RegisterShopScreen
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.offers.EditOfferScreen
import com.ghoast.ui.offers.OfferDetailsScreen
import com.ghoast.ui.shop.AddOfferScreen
import com.ghoast.ui.shop.MyShopOffersScreen

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
        composable(Screen.MyShopOffers.route) {
            MyShopOffersScreen(navController = navController)
        }
        composable(
            route = Screen.EditOffer.route + "/{offerId}",
            arguments = listOf(navArgument("offerId") { defaultValue = "" })
        ) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
            EditOfferScreen(navController = navController, offerId = offerId)
        }


        composable(
            route = Screen.OfferDetails.route,
            arguments = listOf(navArgument("offerId") { defaultValue = "" })
        ) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
            OfferDetailsScreen(navController = navController, offerId = offerId)
        }

        // 👉 Εδώ μπορείς να προσθέσεις κι άλλες διαδρομές όταν χρειαστεί
    }
}
