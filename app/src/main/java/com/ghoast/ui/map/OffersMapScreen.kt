// OffersMapScreen.kt - Refactored to assume permission already handled

package com.ghoast.ui.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.model.Offer
import com.ghoast.ui.home.OffersViewModel
import com.ghoast.ui.navigation.Screen
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

    var selectedOffers by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var selectedOfferIndex by remember { mutableStateOf(0) }
    var recenter by remember { mutableStateOf(true) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var tabIndex by remember { mutableStateOf(0) }
    var isRecenterButtonPressed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(recenter) {
        if (recenter) {
            isLoading = true
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()

                if (location != null) {
                    val target = LatLng(location.latitude, location.longitude)
                    userLatLng = target
                    offersViewModel.userLatitude = target.latitude
                    offersViewModel.userLongitude = target.longitude
                    offersViewModel.applyFilters()

                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(target, 12f),
                        durationMs = 1000
                    )
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "\u274c Σφάλμα τοποθεσίας", e)
            } finally {
                recenter = false
                isRecenterButtonPressed = false
                isLoading = false
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
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Προσφορές") })
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Καταστήματα") })
                }
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
                                    selectedOffers = offers.filter { it.shopId == offer.shopId }
                                    selectedOfferIndex = 0
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
                            MarkerInfoWindow(
                                state = MarkerState(position = LatLng(lat, lng)),
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                                title = shop.shopName,
                                onInfoWindowClick = {
                                    navController.navigate(Screen.ShopDetails.createRoute(shop.id))
                                }
                            )
                        }
                    }
                }
            }

            if (selectedOffers.isNotEmpty()) {
                val offer = selectedOffers[selectedOfferIndex]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${selectedOfferIndex + 1} / ${selectedOffers.size}", style = MaterialTheme.typography.labelLarge)
                            IconButton(onClick = { selectedOffers = emptyList() }) {
                                Icon(Icons.Default.Close, contentDescription = "Κλείσιμο")
                            }
                        }
                        Text(offer.shopName, style = MaterialTheme.typography.titleMedium)
                        Text(offer.title, style = MaterialTheme.typography.bodyMedium)
                        Text("${offer.discount}%", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = {
                                selectedOfferIndex = if (selectedOfferIndex == 0) selectedOffers.lastIndex else selectedOfferIndex - 1
                            }) {
                                Text("◀")
                            }
                            Text(
                                text = "Περισσότερα...",
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.OfferDetails.createRoute(offer.id))
                                    selectedOffers = emptyList()
                                },
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                            IconButton(onClick = {
                                selectedOfferIndex = if (selectedOfferIndex == selectedOffers.lastIndex) 0 else selectedOfferIndex + 1
                            }) {
                                Text("▶")
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    isRecenterButtonPressed = true
                    recenter = true
                },
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 88.dp, end = 16.dp)
                    .graphicsLayer {
                        rotationZ = if (isRecenterButtonPressed) 360f else 0f
                    },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Επανακέντρωση")
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (showFilterDialog) {
                MapFiltersDialog(
                    selectedCategory = offersViewModel.selectedCategory,
                    selectedDistance = offersViewModel.selectedDistance ?: 10,
                    onCategoryChange = { offersViewModel.setCategoryFilter(it) },
                    onDistanceChange = { offersViewModel.setDistanceFilter(it) },
                    onApply = {
                        offersViewModel.applyFilters()
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
