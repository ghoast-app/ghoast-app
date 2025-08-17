package com.ghoast.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.home.OfferCard
import com.ghoast.viewmodel.FavoriteOfferSortMode
import com.ghoast.viewmodel.FavoritesViewModel
import com.ghoast.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteOffersScreen(
    navController: NavHostController,
    favoritesViewModel: FavoritesViewModel = viewModel(),
    fromMenu: Boolean = false
) {
    val filteredOffers by favoritesViewModel.filteredFavoriteOffers.collectAsState()
    val searchQuery by favoritesViewModel.searchQuery.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf(FavoriteOfferSortMode.NEWEST) }

    LaunchedEffect(Unit) {
        favoritesViewModel.setFavoriteOfferSortMode(FavoriteOfferSortMode.NEWEST)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Αγαπημένες Προσφορές") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.OffersHome.route + "?fromMenu=true") {
                            popUpTo(0)
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedSort.label,
                    onValueChange = {},
                    label = { Text("Ταξινόμηση κατά") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    FavoriteOfferSortMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label) },
                            onClick = {
                                selectedSort = mode
                                favoritesViewModel.setFavoriteOfferSortMode(mode)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = searchQuery,
                onValueChange = { favoritesViewModel.updateSearchQuery(it) },
                placeholder = { Text("Αναζήτηση προσφοράς...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredOffers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⭐", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Δεν έχετε αγαπημένες προσφορές ακόμα.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(filteredOffers) { offer ->
                        OfferCard(
                            offer = offer,
                            isFavorite = true,
                            onToggleFavorite = {
                                favoritesViewModel.toggleFavoriteOffer(offer.id)
                            },
                            onClick = {
                                navController.navigate(Screen.OfferDetails.createRoute(offer.id))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
