package com.ghoast.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⭐", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Δεν έχετε αγαπημένες προσφορές ακόμα.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
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
