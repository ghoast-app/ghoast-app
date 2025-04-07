package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.home.OffersViewModel
import com.ghoast.ui.session.UserSessionViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect

@Composable
fun OffersHomeScreen(navController: NavHostController) {
    val viewModel: OffersViewModel = viewModel()
    val offers = viewModel.filteredOffers.collectAsState().value
    val sessionViewModel: UserSessionViewModel = viewModel()

    // ✅ State για την εμφάνιση/απόκρυψη του dropdown menu
    var menuExpanded by remember { mutableStateOf(false) }

    // ✅ Snapshot listener για live ενημέρωση προσφορών
    LaunchedEffect(Unit) {
        viewModel.listenToOffers()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ✅ TopAppBar με το dynamic menu
        OffersTopBar(
            navController = navController,
            sessionViewModel = sessionViewModel,
            onMenuExpand = { expanded -> menuExpanded = expanded },
            onShowHelp = {
                // TODO: Προσθήκη διαλόγου ή οθόνης βοήθειας
            },
            onShowContact = {
                // TODO: Προσθήκη διαλόγου ή οθόνης επικοινωνίας
            },
            menuExpanded = menuExpanded
        )

        // ✅ Φίλτρα (Κατηγορία / Απόσταση)
        OffersFiltersSection(
            selectedCategory = viewModel.selectedCategory,
            selectedDistance = viewModel.selectedDistance,
            onCategoryChange = { category ->
                viewModel.selectedCategory = category
                viewModel.fetchOffers()
            },
            onDistanceChange = { distance ->
                viewModel.selectedDistance = distance
                viewModel.fetchOffers()
            }
        )

        // ✅ Εδώ θα μπει η λίστα ή ο χάρτης με τις προσφορές
        val favorites = viewModel.favoriteOfferIds.collectAsState().value

        OffersListSection(
            offers = offers,
            favorites = favorites.toList(),
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            navController = navController // ✅ Εδώ περνάμε τον controller
        )
    }
}
