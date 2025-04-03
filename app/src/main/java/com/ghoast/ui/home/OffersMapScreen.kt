package com.ghoast.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghoast.ui.home.OffersViewModel
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition



@Composable
fun OffersMapScreen(viewModel: OffersViewModel = viewModel()) {

    LaunchedEffect(Unit) {
        viewModel.fetchOffers()
    }

    val offers = viewModel.offers.collectAsState().value

    val athens = LatLng(37.9838, 23.7275)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(athens, 11f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        offers.forEach { offer ->
            if (offer.latitude != null && offer.longitude != null) {
                Marker(
                    state = MarkerState(position = LatLng(offer.latitude, offer.longitude)),
                    title = offer.shopName,
                    snippet = offer.title
                )
            }
        }
    }
}
