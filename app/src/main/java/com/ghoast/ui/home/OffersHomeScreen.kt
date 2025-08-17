@file:OptIn(ExperimentalMaterial3Api::class)

package com.ghoast.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ghoast.ui.components.RecommendedOffersSection
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.ui.session.UserType
import com.ghoast.ui.viewmodel.OffersHomeViewModel
import com.ghoast.viewmodel.RecommendationViewModel
import com.ghoast.ui.viewmodel.SortMode
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

@Composable
fun OffersHomeScreen(
    navController: NavHostController,
    sessionViewModel: UserSessionViewModel,
    fromMenu: Boolean = false
) {
    val viewModel: OffersHomeViewModel = viewModel()
    val recommendationViewModel: RecommendationViewModel = viewModel()

    val offers by viewModel.filteredOffers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userType by sessionViewModel.userType.collectAsState()
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showFiltersDialog by remember { mutableStateOf(false) }
    var showGPSDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(fromMenu) {
        if (fromMenu) {
            menuExpanded = true
        }
    }

    LaunchedEffect(viewModel.userLatitude, viewModel.userLongitude, userType, isLoggedIn) {
        Log.d("DEBUG_SESSION", "🔍 userType = $userType, isLoggedIn = $isLoggedIn")
        recommendationViewModel.userLatitude = viewModel.userLatitude
        recommendationViewModel.userLongitude = viewModel.userLongitude

        if (userType == UserType.USER && isLoggedIn) {
            Log.d("DEBUG_SESSION", "✅ Loading recommended offers...")
            recommendationViewModel.loadRecommendedOffers()
        } else {
            Log.d("DEBUG_SESSION", "⛔ Not loading recommended offers (wrong type or not logged in)")
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (fineGranted && isGPSEnabled) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        location?.let {
                            viewModel.userLatitude = it.latitude
                            viewModel.userLongitude = it.longitude
                            viewModel.refreshOffers()
                        }
                    }
            } catch (e: Exception) {
                Log.e("OffersHome", "Location error", e)
            }
        } else if (!isGPSEnabled) {
            showGPSDialog = true
            viewModel.refreshOffers()
        } else {
            viewModel.refreshOffers()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (userType == UserType.SHOP) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            navController.navigate(Screen.AddOffer.route)
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Νέα Προσφορά")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OffersTopBar(
                navController = navController,
                menuExpanded = menuExpanded,
                onMenuExpand = { menuExpanded = it },
                onShowHelp = {
                    navController.navigate("help?fromMenu=true")
                },
                onShowContact = {
                    navController.navigate("contact?fromMenu=true")
                },
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    viewModel.searchQuery = it
                    viewModel.refreshOffers()
                },
                extraActions = {
                    IconButton(onClick = { showFiltersDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Φίλτρα")
                    }
                }
            )

            if (userType == UserType.USER && isLoggedIn) {
                RecommendedOffersSection(
                    viewModel = recommendationViewModel,
                    onOfferClick = { offer ->
                        navController.navigate(Screen.OfferDetails.createRoute(offer.id ?: ""))
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = isLoading),
                onRefresh = { viewModel.refreshOffers() }
            ) {
                if (offers.isEmpty() && !isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📭", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Δεν υπάρχουν διαθέσιμες προσφορές",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    OffersListSection(
                        offers = offers,
                        favorites = viewModel.favoriteOfferIds.collectAsState().value.toList(),
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        navController = navController
                    )
                }
            }
        }

        if (showFiltersDialog) {
            OffersFiltersDialog(
                selectedCategory = viewModel.selectedCategory,
                selectedDistance = viewModel.selectedDistance ?: 20,
                selectedSortMode = viewModel.selectedSortMode,
                onCategoryChange = { viewModel.setCategoryFilter(it) },
                onDistanceChange = { viewModel.setDistanceFilter(it) },
                onSortModeChange = { viewModel.setSortMode(it) },
                onApply = { showFiltersDialog = false },
                onReset = {
                    viewModel.setCategoryFilter(null)
                    viewModel.setDistanceFilter(20)
                    viewModel.setSortMode(SortMode.NEWEST)
                    showFiltersDialog = false
                },
                onDismiss = { showFiltersDialog = false }
            )
        }

        if (showGPSDialog) {
            AlertDialog(
                onDismissRequest = { showGPSDialog = false },
                title = { Text("Απαιτείται GPS") },
                text = {
                    Text("Για να εμφανιστούν προσφορές κοντά σας, ενεργοποιήστε την τοποθεσία στη συσκευή σας.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        showGPSDialog = false
                    }) {
                        Text("Άνοιγμα ρυθμίσεων")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGPSDialog = false }) {
                        Text("Άκυρο")
                    }
                }
            )
        }
    }
}