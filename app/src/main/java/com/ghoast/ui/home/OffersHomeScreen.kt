package com.ghoast.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.components.OffersFiltersDialog
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.session.UserSessionViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersHomeScreen(navController: NavHostController) {
    val viewModel: OffersViewModel = viewModel()
    val offers = viewModel.filteredOffers.collectAsState().value
    val sessionViewModel: UserSessionViewModel = viewModel()
    val favorites = viewModel.favoriteOfferIds.collectAsState().value

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var menuExpanded by remember { mutableStateOf(false) }
    var showFiltersDialog by remember { mutableStateOf(false) }
    var isCheckingLimit by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 📍 Άδειες
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            try {
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                location?.let {
                    viewModel.userLatitude = it.latitude
                    viewModel.userLongitude = it.longitude
                    viewModel.listenToOffers()
                }
            } catch (e: Exception) {
                Log.e("LOCATION", "Error getting location", e)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        isCheckingLimit = true
                        checkOfferLimitAndNavigate(
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            onFinishChecking = { isCheckingLimit = false }
                        )
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Νέα Προσφορά")
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
                onCategoryChange = { viewModel.setCategoryFilter(it) },
                onDistanceChange = { viewModel.setDistanceFilter(it) },
                onApply = { showFiltersDialog = false },
                onReset = {
                    viewModel.setCategoryFilter(null)
                    viewModel.setDistanceFilter(null)
                    showFiltersDialog = false
                },
                onDismiss = { showFiltersDialog = false }
            )
        }

        if (isCheckingLimit) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {},
                title = { Text("Παρακαλώ περιμένετε...") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }
            )
        }
    }
}

suspend fun checkOfferLimitAndNavigate(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onFinishChecking: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    if (userId == null) {
        snackbarHostState.showSnackbar("❌ Δεν έχεις συνδεθεί!")
        onFinishChecking()
        return
    }

    try {
        val documents = db.collection("offers")
            .whereEqualTo("shopId", userId)
            .get()
            .await()

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        var offersThisMonth = 0

        for (document in documents) {
            val timestampMillis = document.getLong("timestamp")
            val timestamp = timestampMillis?.let { Date(it) }
            if (timestamp != null) {
                val offerMonth = Calendar.getInstance().apply { time = timestamp }.get(Calendar.MONTH)
                if (offerMonth == currentMonth) {
                    offersThisMonth++
                }
            }
        }

        if (offersThisMonth == 0) {
            navController.navigate(Screen.AddOffer.route)
        } else {
            navController.navigate(Screen.OfferLimitExceeded.route)
        }

    } catch (e: Exception) {
        Log.e("CHECK_LIMIT", "❌ Firestore Error", e)
        snackbarHostState.showSnackbar("❌ Προέκυψε σφάλμα κατά τον έλεγχο προσφορών!")
    } finally {
        onFinishChecking()
    }
}
