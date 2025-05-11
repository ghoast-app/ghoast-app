package com.ghoast.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.viewmodel.FavoritesViewModel
import com.ghoast.viewmodel.FavoriteShopSortMode
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.user.ShopCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteShopsScreen(
    navController: NavHostController,
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val filteredShops by favoritesViewModel.filteredFavoriteShops.collectAsState()
    val searchQuery by favoritesViewModel.shopSearchQuery.collectAsState()
    var selectedSort by remember { mutableStateOf(FavoriteShopSortMode.ALPHABETICAL) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Αγαπημένα Καταστήματα") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            // 🔽 Dropdown για ταξινόμηση
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedSort.label,
                    onValueChange = {},
                    label = { Text("Ταξινόμηση") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    FavoriteShopSortMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label) },
                            onClick = {
                                selectedSort = mode
                                favoritesViewModel.setFavoriteSortMode(mode)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🔍 SearchBar για καταστήματα
            TextField(
                value = searchQuery,
                onValueChange = { favoritesViewModel.updateShopSearchQuery(it) },
                placeholder = { Text("Αναζήτηση καταστήματος...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredShops.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏪", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Δεν έχετε αγαπημένα καταστήματα ακόμα.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(filteredShops) { shop ->
                        ShopCard(
                            shop = shop,
                            isFavorite = true,
                            onToggleFavorite = {
                                favoritesViewModel.toggleFavoriteShop(shop.id)
                            },
                            onClick = {
                                navController.navigate(Screen.ShopDetails.createRoute(shop.id))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
