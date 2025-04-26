package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ghoast.model.Offer
import com.ghoast.ui.navigation.Screen
@Composable
fun OffersListSection(
    offers: List<Offer>,
    favorites: List<String>,
    onToggleFavorite: (String) -> Unit,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(offers) { offer ->
            OfferCard(
                offer = offer,
                isFavorite = favorites.contains(offer.id),
                onToggleFavorite = { onToggleFavorite(offer.id) },
                onClick = {
                    navController.navigate(Screen.OfferDetails.createRoute(offer.id))
                }
            )
        }
    }
}
