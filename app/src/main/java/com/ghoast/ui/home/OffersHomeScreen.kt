package com.ghoast.ui.home

import android.Manifest
import android.content.pm.PackageManager
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
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.ui.session.UserType
import com.ghoast.ui.viewmodel.OffersHomeViewModel
import com.ghoast.ui.viewmodel.SortMode
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersHomeScreen(navController: NavHostController) {
    val viewModel: OffersHomeViewModel = viewModel()
    val offers by viewModel.filteredOffers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val sessionViewModel: UserSessionViewModel = viewModel()
    val userType by sessionViewModel.userType.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var menuExpanded by remember { mutableStateOf(false) }
    var showFiltersDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted) {
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
                sessionViewModel = sessionViewModel,
                menuExpanded = menuExpanded,
                onMenuExpand = { menuExpanded = it },
                onShowHelp = { navController.navigate("help") },
                onShowContact = { navController.navigate("contact") },
                extraActions = {
                    IconButton(onClick = { showFiltersDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Φίλτρα")
                    }
                }
            )

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = isLoading),
                onRefresh = { viewModel.refreshOffers() }
            ) {
                if (offers.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "\uD83D\uDCED",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Δεν υπάρχουν διαθέσιμες προσφορές",
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
    }
}
