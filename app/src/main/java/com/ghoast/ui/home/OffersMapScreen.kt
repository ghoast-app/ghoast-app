package com.ghoast.ui.home

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.model.Offer
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.*
import com.ghoast.ui.session.UserSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersMapScreen(
    navController: NavHostController,
    selectedCategory: String? = null,
    selectedDistance: Int? = null,
    viewModel: OffersViewModel = viewModel()
) {
    val offers by viewModel.offers.collectAsState()
    val sessionViewModel: UserSessionViewModel = viewModel()

    // Φόρτωσε τα offers με βάση τα φίλτρα
    LaunchedEffect(selectedCategory, selectedDistance) {
        viewModel.fetchOffers()
    }

    val athens = LatLng(37.9838, 23.7275)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(athens, 11f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Προσφορές στον Χάρτη") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                offers.forEach { offer ->
                    val position = LatLng(offer.latitude ?: return@forEach, offer.longitude ?: return@forEach)
                    Marker(
                        state = MarkerState(position = position),
                        title = offer.shopName,
                        snippet = "${offer.title} (${offer.category})"
                    )
                }
            }
        }
    }
}
