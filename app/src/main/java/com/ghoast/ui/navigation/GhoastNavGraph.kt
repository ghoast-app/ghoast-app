package com.ghoast.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ghoast.ui.help.HelpScreen
import com.ghoast.ui.home.OffersHomeScreen
import com.ghoast.ui.map.OffersMapScreen
import com.ghoast.ui.home.OffersViewModel
import com.ghoast.ui.login.LoginScreen
import com.ghoast.ui.map.OffersMapScreen
import com.ghoast.ui.register.RegisterUserScreen
import com.ghoast.ui.register.RegisterShopScreen
import com.ghoast.ui.offers.EditOfferScreen
import com.ghoast.ui.offers.OfferDetailsScreen
import com.ghoast.ui.shop.AddOfferScreen
import com.ghoast.ui.shop.EditShopProfileScreen
import com.ghoast.ui.shop.MyShopOffersScreen
import com.ghoast.ui.shop.ShopProfileScreen
import com.ghoast.ui.user.AllShopsScreen
import com.ghoast.ui.user.FavoriteOffersScreen
import com.ghoast.ui.user.FavoriteShopsScreen
import com.ghoast.ui.user.UserProfileScreen
import com.ghoast.viewmodel.ShopsMapViewModel

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
            MyShopOffersScreen(navController)
        }
        composable(Screen.ShopProfile.route) {
            ShopProfileScreen(navController)
        }
        composable(Screen.EditShopProfile.route) {
            EditShopProfileScreen()
        }
        composable(
            route = Screen.OfferDetails.route,
            arguments = listOf(navArgument("offerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
            OfferDetailsScreen(navController = navController, offerId = offerId)
        }

        // ✅ ΕΔΩ ΕΙΝΑΙ Η ΔΙΟΡΘΩΣΗ για OffersMapScreen
        composable(route = Screen.OffersMap.route) {
            OffersMapScreen(navController = navController)
        }
        composable("favorite_shops") {
            FavoriteShopsScreen(navController = navController)
        }

        composable("favorite_offers") {
            FavoriteOffersScreen(navController = navController)
        }


        composable("all_shops") {
            AllShopsScreen(navController = navController)
        }
        composable("user_profile") {
            UserProfileScreen()
        }

        composable("help") {
                    HelpScreen()
                }


    }
}



