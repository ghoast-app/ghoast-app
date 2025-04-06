package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghoast.model.Offer

@Composable
fun OffersListSection(
    offers: List<Offer>,
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(offers) { offer ->
            OfferCard(
                offer = offer,
                isFavorite = favorites.contains(offer.id),
                onToggleFavorite = { onToggleFavorite(offer.id) }
            )
        }
    }
}
