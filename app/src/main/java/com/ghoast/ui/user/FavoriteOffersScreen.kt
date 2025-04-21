package com.ghoast.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.home.OfferCard
import com.ghoast.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteOffersScreen(
    navController: NavHostController,
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val favoriteOffers by favoritesViewModel.favoriteOffers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Αγαπημένες Προσφορές") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (favoriteOffers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Δεν έχετε αγαπημένες προσφορές.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(favoriteOffers) { offer ->
                        OfferCard(
                            offer = offer,
                            isFavorite = true,
                            onToggleFavorite = { favoritesViewModel.toggleFavoriteOffer(offer.id) },
                            onClick = {}
                        )

                    }
                }
            }
        }
    }
}
