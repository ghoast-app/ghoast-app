package com.ghoast.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFiltersDialog(
    selectedCategory: String?,
    selectedDistance: Int,
    onlyNew: Boolean = false,
    onlyWithOffers: Boolean = false,
    onlyFavorites: Boolean = false,
    isShopMode: Boolean = false,
    onCategoryChange: (String?) -> Unit,
    onDistanceChange: (Int) -> Unit,
    onOnlyNewChange: (Boolean) -> Unit = {},
    onOnlyWithOffersChange: (Boolean) -> Unit = {},
    onOnlyFavoritesChange: (Boolean) -> Unit = {},
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        "Όλες οι κατηγορίες",
        "Γυναικεία ένδυση",
        "Γυναικεία υπόδηση",
        "Ανδρική ένδυση",
        "Ανδρική υπόδηση",
        "Παιδική ένδυση",
        "Παιδική υπόδηση",
        "Αθλητική ένδυση",
        "Αθλητική υπόδηση",
        "Εσώρουχα",
        "Καλλυντικά",
        "Αξεσουάρ",
        "Κοσμήματα",
        "Οπτικά",
        "Ρολόγια"
    )

    var category by remember { mutableStateOf(selectedCategory ?: "Όλες οι κατηγορίες") }
    var distance by remember { mutableStateOf(selectedDistance) }
    var onlyNewOffers by remember { mutableStateOf(onlyNew) }
    var onlyWithActiveOffers by remember { mutableStateOf(onlyWithOffers) }
    var onlyFavoriteShops by remember { mutableStateOf(onlyFavorites) }

    val scrollState = rememberScrollState()
    val showIndicator by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        properties = DialogProperties(usePlatformDefaultWidth = false),
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 500.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Κατηγορία", style = MaterialTheme.typography.titleMedium)

                    categories.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = category == it,
                                onClick = { category = it }
                            )
                            Text(it)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Απόσταση: έως $distance km")
                    Slider(
                        value = distance.toFloat(),
                        onValueChange = { distance = it.toInt() },
                        valueRange = 1f..100f,
                        steps = 99
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isShopMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = onlyNewOffers,
                                onCheckedChange = { onlyNewOffers = it }
                            )
                            Text("Μόνο νέες προσφορές")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = onlyFavoriteShops,
                                onCheckedChange = { onlyFavoriteShops = it }
                            )
                            Text("Προσφορές από αγαπημένα καταστήματα")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = onlyWithActiveOffers,
                                onCheckedChange = { onlyWithActiveOffers = it }
                            )
                            Text("Καταστήματα με ενεργές προσφορές")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = onlyFavoriteShops,
                                onCheckedChange = { onlyFavoriteShops = it }
                            )
                            Text("Αγαπημένα καταστήματα")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onReset) { Text("Επαναφορά") }
                        TextButton(onClick = onDismiss) { Text("Άκυρο") }
                        Button(onClick = {
                            onCategoryChange(if (category == "Όλες οι κατηγορίες") null else category)
                            onDistanceChange(distance)
                            onOnlyNewChange(onlyNewOffers)
                            onOnlyWithOffersChange(onlyWithActiveOffers)
                            onOnlyFavoritesChange(onlyFavoriteShops)
                            onApply()
                        }) {
                            Text("Εφαρμογή")
                        }
                    }
                }

                if (showIndicator) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll Indicator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .alpha(0.7f)
                    )
                }
            }
        }
    )
}
