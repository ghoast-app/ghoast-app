package com.ghoast.ui.navigation

import com.ghoast.ui.home.HomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ghoast.ui.login.LoginScreen
import com.ghoast.ui.register.RegisterUserScreen
import com.ghoast.ui.register.RegisterShopScreen
import com.ghoast.ui.home.OffersHomeScreen
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.home.OffersMapScreen
// User Screens
import com.ghoast.ui.user.FavoriteShopsScreen
import com.ghoast.ui.user.FavoriteOffersScreen
import com.ghoast.ui.user.UserProfileScreen

// Shop Screens
import com.ghoast.ui.shop.AddOfferScreen
import com.ghoast.ui.shop.MyShopOffersScreen
import com.ghoast.ui.shop.ShopProfileScreen

@Composable
fun GhoastNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.OffersHome.route){

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
        composable(Screen.OffersMap.route) {
            OffersMapScreen()
        }
        composable(Screen.FavoriteShops.route) {
            FavoriteShopsScreen()
        }
        composable(Screen.FavoriteOffers.route) {
            FavoriteOffersScreen()
        }
        composable(Screen.UserProfile.route) {
            UserProfileScreen()
        }
        composable(Screen.AddOffer.route) {
            AddOfferScreen (navController = navController)
        }
        composable(Screen.MyShopOffers.route) {
            MyShopOffersScreen()
        }
        composable(Screen.ShopProfile.route) {
            ShopProfileScreen()
        }
        composable("favorite_shops") { FavoriteShopsScreen() }
        composable("favorite_offers") { FavoriteOffersScreen() }
        composable("user_profile") { UserProfileScreen() }
        composable(Screen.AddOffer.route) {AddOfferScreen(navController = navController) }
        composable("my_shop_offers") { MyShopOffersScreen() }
        composable("shop_profile") { ShopProfileScreen() }

    }
}

