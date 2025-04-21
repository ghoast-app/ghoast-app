package com.ghoast.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.components.OffersFiltersDialog
import com.ghoast.ui.session.UserSessionViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersHomeScreen(navController: NavHostController) {
    val viewModel: OffersViewModel = viewModel()
    val offers = viewModel.filteredOffers.collectAsState().value
    val sessionViewModel: UserSessionViewModel = viewModel()
    val favorites = viewModel.favoriteOfferIds.collectAsState().value

    var menuExpanded by remember { mutableStateOf(false) }
    var showFiltersDialog by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // ✅ Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            Log.d("PERMISSION", "✅ Άδεια τοποθεσίας δόθηκε")
        } else {
            Log.e("PERMISSION", "❌ Άρνηση άδειας τοποθεσίας")
        }
    }

    // ✅ Ελέγχουμε και ζητάμε permission με την εκκίνηση
    LaunchedEffect(Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // ✅ Μόλις πάρουμε permission, παίρνουμε τοποθεσία και ακούμε προσφορές
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            try {
                val location = fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .await()

                if (location != null) {
                    viewModel.userLatitude = location.latitude
                    viewModel.userLongitude = location.longitude
                    Log.d("DISTANCE_DEBUG", "📍 Τοποθεσία: ${location.latitude}, ${location.longitude}")
                    viewModel.listenToOffers()
                } else {
                    Log.e("DISTANCE_DEBUG", "❌ Τοποθεσία null — δεν κάνουμε listenToOffers ακόμα")
                }
            } catch (e: SecurityException) {
                Log.e("DISTANCE_DEBUG", "❌ Σφάλμα άδειας τοποθεσίας", e)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OffersTopBar(
            navController = navController,
            sessionViewModel = sessionViewModel,
            menuExpanded = menuExpanded,
            onMenuExpand = { menuExpanded = it },
            onShowHelp = {
                navController.navigate("help")
            },
            onShowContact = {
                navController.navigate("contact")
            },
            extraActions = {
                IconButton(onClick = { showFiltersDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Φίλτρα")
                }
            }
        )

        OffersListSection(
            offers = offers,
            favorites = favorites.toList(),
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            navController = navController
        )
    }

    if (showFiltersDialog) {
        OffersFiltersDialog(
            selectedCategory = viewModel.selectedCategory,
            selectedDistance = viewModel.selectedDistance ?: 10,
            onCategoryChange = {
                viewModel.setCategoryFilter(it)
            },
            onDistanceChange = {
                viewModel.setDistanceFilter(it)
            },
            onApply = {
                showFiltersDialog = false
            },
            onReset = {
                viewModel.setCategoryFilter(null)
                viewModel.setDistanceFilter(null)
                showFiltersDialog = false
            },
            onDismiss = {
                showFiltersDialog = false
            }
        )
    }
}
