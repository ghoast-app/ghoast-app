package com.ghoast.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ghoast.billing.BillingViewModel
import com.ghoast.ui.billing.OfferLimitExceededScreen
import com.ghoast.ui.contact.ContactScreen
import com.ghoast.ui.help.HelpScreen
import com.ghoast.ui.home.OffersHomeScreen
import com.ghoast.ui.login.LoginScreen
import com.ghoast.ui.map.OffersMapScreen
import com.ghoast.ui.notifications.UserNotificationsScreen
import com.ghoast.ui.offers.EditOfferScreen
import com.ghoast.ui.offers.OfferDetailsScreen
import com.ghoast.ui.register.RegisterShopScreen
import com.ghoast.ui.register.RegisterUserScreen
import com.ghoast.ui.shop.*
import com.ghoast.ui.start.StartScreen
import com.ghoast.ui.user.*
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.ui.session.UserType

@Composable
fun GhoastNavGraph(
    navController: NavHostController,
    sessionViewModel: UserSessionViewModel
) {
    val userType = sessionViewModel.userType.collectAsState().value

    NavHost(
        navController = navController,
        startDestination = Screen.Start.route
    ) {
        composable(Screen.Start.route) {
            StartScreen(
                onPermissionGranted = {
                    navController.navigate(Screen.OffersHome.route) {
                        popUpTo(Screen.Start.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) { LoginScreen(navController, sessionViewModel) }
        composable(Screen.RegisterUser.route) { RegisterUserScreen(navController) }
        composable(Screen.RegisterShop.route) { RegisterShopScreen(navController) }
        composable(Screen.OffersHome.route) { OffersHomeScreen(navController, sessionViewModel) }

        composable(Screen.AddOffer.route) {
            if (userType == UserType.SHOP) {
                AddOfferScreen(navController)
            } else {
                Text("Δεν έχετε πρόσβαση σε αυτήν τη σελίδα.")
            }
        }
        composable(Screen.MyShopOffers.route) {
            if (userType == UserType.SHOP) {
                MyShopOffersScreen(navController)
            } else {
                Text("Δεν έχετε πρόσβαση σε αυτήν τη σελίδα.")
            }
        }
        composable(Screen.ShopProfile.route) {
            if (userType == UserType.SHOP) {
                ShopProfileScreen(navController)
            } else {
                Text("Δεν έχετε πρόσβαση σε αυτήν τη σελίδα.")
            }
        }
        composable(Screen.EditShopProfile.route) {
            if (userType == UserType.SHOP) {
                EditShopProfileScreen(navController = navController)
            } else {
                Text("Δεν έχετε πρόσβαση σε αυτήν τη σελίδα.")
            }
        }
        composable(
            route = Screen.EditShop.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            if (userType == UserType.SHOP) {
                EditShopProfileScreen(navController = navController, shopId = shopId)
            } else {
                Text("Δεν έχετε πρόσβαση σε αυτήν τη σελίδα.")
            }
        }
        composable(
            route = Screen.OfferDetails.route,
            arguments = listOf(navArgument("offerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
            OfferDetailsScreen(navController = navController, offerId = offerId)
        }
        composable(
            route = Screen.EditOffer.route,
            arguments = listOf(navArgument("offerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
            if (userType == UserType.SHOP) {
                EditOfferScreen(navController = navController, offerId = offerId)
            } else {
                Text("Δεν έχετε πρόσβαση σε αυτήν τη σελίδα.")
            }
        }
        composable(Screen.OffersMap.route) { OffersMapScreen(navController = navController) }
        composable(Screen.FavoriteShops.route) { FavoriteShopsScreen(navController = navController) }
        composable(Screen.FavoriteOffers.route) { FavoriteOffersScreen(navController = navController) }
        composable("all_shops") { AllShopsScreen(navController = navController) }
        composable(Screen.UserProfile.route) { UserProfileScreen() }
        composable("help") { HelpScreen() }
        composable("contact") { ContactScreen() }
        composable("notifications") { UserNotificationsScreen() }
        composable(Screen.AddNewShop.route) {
            if (userType == UserType.SHOP) {
                AddNewShopScreen(navController = navController)
            } else {
                Text("Δεν έχετε πρόσβαση σε αυτήν τη σελίδα.")
            }
        }
        composable(Screen.MyShops.route) {
            if (userType == UserType.SHOP) {
                MyShopsScreen(navController = navController)
            } else {
                Text("Δεν έχετε πρόσβαση σε αυτήν τη σελίδα.")
            }
        }
        composable(Screen.OfferLimitExceeded.route) {
            OfferLimitExceededScreen(
                billingViewModel = viewModel<BillingViewModel>(),
                onCancel = {
                    navController.popBackStack()
                },
                onPaymentSuccess = {
                    navController.navigate(Screen.AddOffer.route) {
                        popUpTo(Screen.OfferLimitExceeded.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.ShopDetails.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            ShopDetailsScreen(shopId = shopId, navController = navController)
        }
        composable(
            route = Screen.ShopOffers.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            ShopOffersScreen(shopId = shopId, navController = navController)
        }
    }
}
