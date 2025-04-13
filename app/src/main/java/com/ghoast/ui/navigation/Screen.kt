package com.ghoast.ui.navigation

sealed class Screen(val route: String) {
    object OffersHome : Screen("offers_home")
    object OffersMap : Screen("offers_map")
    object Login : Screen("login")

    // User Screens
    object FavoriteShops : Screen("favorite_shops")
    object FavoriteOffers : Screen("favorite_offers")
    object UserProfile : Screen("user_profile")

    // Shop Screens
    object AddOffer : Screen("add_offer")
    object MyShopOffers : Screen("my_shop_offers")
    object ShopProfile : Screen("shop_profile")
    object EditOffer : Screen("edit_offer")


    object RegisterUser : Screen("register_user")
    object RegisterShop : Screen("register_shop")

    object OfferDetails : Screen("offer_details/{offerId}") {
        fun createRoute(offerId: String) = "offer_details/$offerId"

        object Screen {
            const val MyShopOffers = "my_shop_offers"
            // ...και τα υπόλοιπα
        }

    }

}

