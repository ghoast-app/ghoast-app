package com.ghoast.ui.navigation

sealed class Screen(val route: String) {
    object OffersHome : Screen("offers_home")
    object OffersMap : Screen("offers_map")
    object Login : Screen("login")
    object OfferLimitExceeded : Screen("offer_limit_exceeded")
    object Start : Screen("start")
    object ShopDetails : Screen("shop_details/{shopId}") {
        fun createRoute(shopId: String) = "shop_details/$shopId"
    }
    object ShopOffers : Screen("shop_offers/{shopId}") {
        fun createRoute(shopId: String) = "shop_offers/$shopId"
    }

    // User Screens
    object FavoriteShops : Screen("favorite_shops")
    object FavoriteOffers : Screen("favorite_offers")
    object UserProfile : Screen("user_profile")

    // Shop Screens
    object AddOffer : Screen("add_offer")
    object MyShopOffers : Screen("my_shop_offers")
    object ShopProfile : Screen("shop_profile")

    object EditOffer : Screen("edit_offer/{offerId}") {
        fun createRoute(offerId: String): String = "edit_offer/$offerId"
    }

    object EditShopProfile : Screen("edit_shop_profile")
    object AddNewShop : Screen("add_new_shop")
    object MyShops : Screen("my_shops")

    object EditShop : Screen("edit_shop/{shopId}") {
        fun createRoute(shopId: String): String = "edit_shop/$shopId"
    }

    // Register
    object RegisterUser : Screen("register_user")
    object RegisterShop : Screen("register_shop")

    // Offer Details
    object OfferDetails : Screen("offer_details/{offerId}") {
        fun createRoute(offerId: String): String = "offer_details/$offerId"



    }
}
