
package com.ghoast.ui.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.model.Offer
import com.ghoast.model.Shop
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
    val context = LocalContext.current
    val defaultLatLng = LatLng(35.1856, 33.3823)
    var userLatLng by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 10f)
    }

    val offers = offersViewModel.filteredOffers.collectAsState().value
    val shops = shopsMapViewModel.shops.collectAsState().value

    var selectedOffer by remember { mutableStateOf<Offer?>(null) }
    var recenter by remember { mutableStateOf(true) }
    var showFilterDialog by remember { mutableStateOf(false) }

    var tabIndex by remember { mutableStateOf(0) }

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
                val location =
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .await()

                val target = if (location != null) {
                    LatLng(location.latitude, location.longitude).also {
                        userLatLng = it
                        offersViewModel.userLatitude = it.latitude
                        offersViewModel.userLongitude = it.longitude
                        offersViewModel.applyFilters()

                    }
                } else {
                    defaultLatLng
                }

                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(target, 12f),
                    durationMs = 1000
                )
            } catch (e: Exception) {
                Log.e("MapScreen", "❌ Σφάλμα τοποθεσίας", e)
            } finally {
                recenter = false
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Χάρτης") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Φίλτρα")
                        }
                    }
                )

                TabRow(selectedTabIndex = tabIndex) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        text = { Text("Προσφορές") }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = { Text("Καταστήματα") }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { recenter = true },
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
                if (tabIndex == 0) {
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
                } else {
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
            }

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

            if (showFilterDialog) {
                MapFiltersDialog(
                    selectedCategory = offersViewModel.selectedCategory,
                    selectedDistance = offersViewModel.selectedDistance ?: 10,
                    onCategoryChange = {
                        offersViewModel.setCategoryFilter(it) // ✅ σωστή μέθοδος
                    },
                    onDistanceChange = {
                        offersViewModel.setDistanceFilter(it) // ✅ σωστή μέθοδος
                    },
                    onApply = {
                        offersViewModel.applyFilters() // ✅ άμεση εφαρμογή χωρίς fetch
                        showFilterDialog = false
                    },
                    onReset = {
                        offersViewModel.setCategoryFilter(null)
                        offersViewModel.setDistanceFilter(null)
                        showFilterDialog = false
                    },
                    onDismiss = { showFilterDialog = false }
                )
            }
        }
    }
}
