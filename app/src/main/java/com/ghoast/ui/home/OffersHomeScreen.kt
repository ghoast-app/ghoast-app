// OffersHomeScreen.kt - Updated για ViewModel + SwipeRefresh

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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ghoast.ui.components.OffersFiltersDialog
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.ui.viewmodel.OffersHomeViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersHomeScreen(navController: NavHostController) {
    val viewModel: OffersHomeViewModel = viewModel()
    val offers by viewModel.offers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val sessionViewModel: UserSessionViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var menuExpanded by remember { mutableStateOf(false) }
    var showFiltersDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
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
                OffersListSection(
                    offers = offers,
                    favorites = emptyList(), // Θα περαστεί αργότερα με σωστά favorites
                    onToggleFavorite = {},
                    navController = navController
                )
            }
        }

        if (showFiltersDialog) {
            OffersFiltersDialog(
                selectedCategory = null,
                selectedDistance = 10,
                onCategoryChange = {},
                onDistanceChange = {},
                onApply = { showFiltersDialog = false },
                onReset = { showFiltersDialog = false },
                onDismiss = { showFiltersDialog = false }
            )
        }
    }
}
