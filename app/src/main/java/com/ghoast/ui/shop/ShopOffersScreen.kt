package com.ghoast.ui.shop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghoast.model.Offer
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.home.OffersViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopOffersScreen(
    shopId: String,
    navController: NavController,
    offersViewModel: OffersViewModel = viewModel()
) {
    val offers by offersViewModel.filteredOffers.collectAsState()
    val shopOffers = offers.filter { it.shopId == shopId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Προσφορές Καταστήματος") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                    }
                }
            )
        }
    ) { padding ->
        if (shopOffers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Δεν υπάρχουν προσφορές για αυτό το κατάστημα.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(shopOffers) { offer ->
                    OfferCard(offer = offer) {
                        navController.navigate(Screen.OfferDetails.createRoute(offer.id))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun OfferCard(offer: Offer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(offer.title, style = MaterialTheme.typography.titleMedium)
            Text("Έκπτωση: ${offer.discount}", style = MaterialTheme.typography.bodyMedium)
            Text(offer.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
