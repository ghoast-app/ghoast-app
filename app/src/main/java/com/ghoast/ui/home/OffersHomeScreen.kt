package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.components.OffersFiltersDialog
import com.ghoast.ui.session.UserSessionViewModel

@Composable
fun OffersHomeScreen(navController: NavHostController) {
    val viewModel: OffersViewModel = viewModel()
    val offers = viewModel.filteredOffers.collectAsState().value
    val sessionViewModel: UserSessionViewModel = viewModel()
    val favorites = viewModel.favoriteOfferIds.collectAsState().value

    var menuExpanded by remember { mutableStateOf(false) }
    var showFiltersDialog by remember { mutableStateOf(false) }

    // 🔁 Live ενημέρωση
    LaunchedEffect(Unit) {
        viewModel.listenToOffers()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ✅ TopAppBar με menu + φίλτρα
        OffersTopBar(
            navController = navController,
            sessionViewModel = sessionViewModel,
            menuExpanded = menuExpanded,
            onMenuExpand = { menuExpanded = it },
            onShowHelp = { /* TODO */ },
            onShowContact = { /* TODO */ },
            extraActions = {
                IconButton(onClick = { showFiltersDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Φίλτρα")
                }
            }
        )

        // ✅ Λίστα προσφορών
        OffersListSection(
            offers = offers,
            favorites = favorites.toList(),
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            navController = navController
        )
    }

    // ✅ Φίλτρα (Dialog)
    if (showFiltersDialog) {
        OffersFiltersDialog(
            selectedCategory = viewModel.selectedCategory,
            selectedDistance = viewModel.selectedDistance ?: 10,
            onCategoryChange = {
                viewModel.setCategoryFilter(it) // ✅ ΝΕΟ
            },
            onDistanceChange = {
                viewModel.setDistanceFilter(it) // ✅ ΝΕΟ
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
