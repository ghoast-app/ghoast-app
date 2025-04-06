package com.ghoast.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ghoast.ui.home.OffersHomeScreen
import com.ghoast.ui.home.OffersMapScreen
import com.ghoast.ui.login.LoginScreen
import com.ghoast.ui.register.RegisterShopScreen
import com.ghoast.ui.register.RegisterUserScreen
import com.ghoast.ui.shop.AddOfferScreen
import com.ghoast.ui.shop.MyShopOffersScreen
import com.ghoast.ui.shop.ShopProfileScreen
import com.ghoast.ui.user.FavoriteOffersScreen
import com.ghoast.ui.user.FavoriteShopsScreen
import com.ghoast.ui.user.UserProfileScreen
import com.ghoast.ui.user.AllShopsScreen

@Composable
fun GhoastNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.OffersHome.route
    ) {

        // Αρχικές Οθόνες
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.RegisterUser.route) { RegisterUserScreen(navController) }
        composable(Screen.RegisterShop.route) { RegisterShopScreen(navController) }
        composable(Screen.OffersHome.route) { OffersHomeScreen(navController) }

        // Προβολή Χάρτη με φίλτρα
        composable(
            route = "offers_map?category={category}&distance={distance}",
            arguments = listOf(
                navArgument("category") { defaultValue = "" },
                navArgument("distance") { defaultValue = "0" }
            )
        ) { backStackEntry ->
            val selectedCategory = backStackEntry.arguments?.getString("category") ?: ""
            val selectedDistance = backStackEntry.arguments?.getString("distance")?.toIntOrNull()

            OffersMapScreen(
                navController = navController,
                selectedCategory = selectedCategory,
                selectedDistance = selectedDistance
            )
        }

        // User Screens
        composable(Screen.FavoriteShops.route) { FavoriteShopsScreen() }
        composable(Screen.FavoriteOffers.route) { FavoriteOffersScreen() }
        composable(Screen.UserProfile.route) { UserProfileScreen() }

        // Shop Screens
        composable(Screen.AddOffer.route) { AddOfferScreen(navController = navController) }
        composable(Screen.MyShopOffers.route) { MyShopOffersScreen() }
        composable(Screen.ShopProfile.route) { ShopProfileScreen() }

        // Εναλλακτικές route ονομασίες (αν χρησιμοποιούνται)
        composable("favorite_shops") { FavoriteShopsScreen() }
        composable("favorite_offers") { FavoriteOffersScreen() }
        composable("user_profile") { UserProfileScreen() }
        composable("my_shop_offers") { MyShopOffersScreen() }
        composable("shop_profile") { ShopProfileScreen() }
        composable(route = "all_shops") {
            AllShopsScreen(navController = navController)
        }

    }

    }

