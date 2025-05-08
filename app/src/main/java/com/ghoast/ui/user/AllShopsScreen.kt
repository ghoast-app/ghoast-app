package com.ghoast.ui.user

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.model.Shop
import com.ghoast.util.LocationUtils
import com.ghoast.viewmodel.AllShopsViewModel
import com.ghoast.viewmodel.FavoritesViewModel
import com.google.android.gms.location.LocationServices
import com.ghoast.ui.navigation.Screen
import com.ghoast.viewmodel.ShopSortMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllShopsScreen(navController: NavHostController) {
    val viewModel: AllShopsViewModel = viewModel()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val sortedShops by viewModel.sortedShops.collectAsState()
    val favorites by viewModel.favoriteShopIds.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                viewModel.userLatitude = it.latitude
                viewModel.userLongitude = it.longitude
                viewModel.applySorting()
            }
        }
    }

    var expandedSort by remember { mutableStateOf(false) }
    val selectedSort = viewModel.selectedSortMode
    val sortOptions = ShopSortMode.values().toList()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedSort,
                onExpandedChange = { expandedSort = !expandedSort }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedSort.label,
                    onValueChange = {},
                    label = { Text("Ταξινόμηση") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSort) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor().weight(1f)
                )

                ExposedDropdownMenu(
                    expanded = expandedSort,
                    onDismissRequest = { expandedSort = false }
                ) {
                    sortOptions.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label) },
                            onClick = {
                                viewModel.setSortMode(mode)
                                expandedSort = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sortedShops.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏬", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Δεν υπάρχουν καταστήματα διαθέσιμα.", textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn {
                items(sortedShops) { shop ->
                    val distance = if (viewModel.userLatitude != null && viewModel.userLongitude != null) {
                        LocationUtils.calculateHaversineDistance(
                            viewModel.userLatitude!!,
                            viewModel.userLongitude!!,
                            shop.latitude,
                            shop.longitude
                        )
                    } else null

                    ShopCard(
                        shop = shop,
                        isFavorite = favorites.contains(shop.id),
                        onToggleFavorite = { viewModel.toggleFavorite(shop.id) },
                        distanceInKm = distance?.toFloat(),
                        onClick = { navController.navigate(Screen.ShopDetails.createRoute(shop.id)) }
                    )
                    Divider()
                }
            }
        }
    }
}
