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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllShopsScreen(navController: NavHostController) {
    val viewModel: AllShopsViewModel = viewModel()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val shops by viewModel.shops.collectAsState()
    val favorites by viewModel.favoriteShopIds.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val userLocation = remember { mutableStateOf<Location?>(null) }

    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            userLocation.value = location
        }
    }

    var selectedSort by remember { mutableStateOf("Αλφαβητικά") }
    val sortOptions = listOf("Αλφαβητικά", "Νεότερα", "Απόσταση")
    var expandedSort by remember { mutableStateOf(false) }

    val sortedShops = shops.sortedWith(
        when (selectedSort) {
            "Αλφαβητικά" -> compareBy { it.shopName }
            "Νεότερα" -> compareByDescending { it.id }
            "Απόσταση" -> compareBy {
                val shopLocation = Location("shop").apply {
                    latitude = it.latitude
                    longitude = it.longitude
                }
                LocationUtils.calculateDistance(userLocation.value, shopLocation)
            }
            else -> compareBy { it.shopName }
        }
    )

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
                    value = selectedSort,
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
                    sortOptions.forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(sort) },
                            onClick = {
                                selectedSort = sort
                                expandedSort = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sortedShops.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏬", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Δεν υπάρχουν καταστήματα διαθέσιμα.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn {
                items(sortedShops) { shop ->
                    val distanceInKm = userLocation.value?.let {
                        val shopLocation = Location("shop").apply {
                            latitude = shop.latitude
                            longitude = shop.longitude
                        }
                        LocationUtils.calculateDistance(it, shopLocation) / 1000f
                    }

                    ShopCard(
                        shop = shop,
                        isFavorite = favoritesViewModel.favoriteShops.collectAsState().value.any { it.id == shop.id },
                        onToggleFavorite = { favoritesViewModel.toggleFavoriteShop(shop.id) },
                        distanceInKm = distanceInKm
                    )

                    Divider()
                }
            }
        }
    }
}
