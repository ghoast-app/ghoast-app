package com.ghoast.ui.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.model.Offer
import com.ghoast.ui.home.OffersViewModel
import com.ghoast.ui.navigation.Screen
import com.ghoast.util.LocationPermissionHandler
import com.ghoast.util.LocationSettingsHelper
import com.ghoast.viewmodel.ShopsMapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun OffersMapScreen(
    navController: NavHostController,
    offersViewModel: OffersViewModel = viewModel(),
    shopsMapViewModel: ShopsMapViewModel = viewModel()
) {
    Log.d("MapScreen", "🗺️ OffersMapScreen ξεκίνησε")

    val context = LocalContext.current
    val defaultLatLng = LatLng(35.1856, 33.3823) // Λευκωσία fallback
    var userLatLng by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 10f)
    }

    val offers = offersViewModel.filteredOffers.collectAsState().value
    val shops = shopsMapViewModel.shops.collectAsState().value

    Log.d("MapScreen", "📍 Προσφορές loaded: ${offers.size}")
    Log.d("MapScreen", "🏬 Καταστήματα loaded: ${shops.size}")

    var selectedOffer by remember { mutableStateOf<Offer?>(null) }
    var recenter by remember { mutableStateOf(true) }

    LocationPermissionHandler(
        onPermissionGranted = {
            LocationSettingsHelper.checkGpsEnabled(context) { gpsEnabled ->
                if (!gpsEnabled) {
                    LocationSettingsHelper.requestEnableGps(context)
                } else {
                    recenter = true
                }
            }
        },
        onPermissionDenied = {
            Log.e("MapScreen", "❌ Δεν δόθηκε άδεια τοποθεσίας")
        }
    )

    if (recenter) {
        LaunchedEffect(Unit) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()

                val target = if (location != null) {
                    LatLng(location.latitude, location.longitude).also {
                        userLatLng = it
                    }
                } else {
                    defaultLatLng
                }

                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(target, 12f),
                    durationMs = 1000
                )

                Log.d("MapScreen", "🎯 Κάμερα zoom σε: ${target.latitude}, ${target.longitude}")

            } catch (e: Exception) {
                Log.e("MapScreen", "❌ Σφάλμα τοποθεσίας", e)
            } finally {
                recenter = false
            }
        }
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    recenter = true
                    Log.d("MapScreen", "📌 Πατήθηκε κουμπί επαναφοράς θέσης")
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("📍")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // 🔴 Προσφορές
                offers.forEach { offer ->
                    val lat = offer.latitude ?: 0.0
                    val lng = offer.longitude ?: 0.0

                    if (lat != 0.0 && lng != 0.0) {
                        Marker(
                            state = MarkerState(position = LatLng(lat, lng)),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                            onClick = {
                                selectedOffer = offer
                                true
                            }
                        )
                    }
                }

                // 🔵 Καταστήματα
                shops.forEach { shop ->
                    val lat = shop.latitude
                    val lng = shop.longitude

                    if (lat != 0.0 && lng != 0.0) {
                        Marker(
                            state = MarkerState(position = LatLng(lat, lng)),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                            title = shop.shopName
                        )
                    }
                }
            }

            // 📦 Κάρτα προσφοράς
            selectedOffer?.let { offer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(offer.shopName, style = MaterialTheme.typography.titleMedium)
                        Text(offer.title, style = MaterialTheme.typography.bodyMedium)
                        Text("${offer.discount}%", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "περισσότερα...",
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable {
                                    offer.id.let { id ->
                                        navController.navigate(Screen.OfferDetails.createRoute(id))
                                        selectedOffer = null
                                    }
                                },
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
