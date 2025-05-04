package com.ghoast.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFiltersDialog(
    selectedCategory: String?,
    selectedDistance: Int,
    onlyNew: Boolean = false,
    onlyWithOffers: Boolean = false,
    isShopMode: Boolean = false,
    onCategoryChange: (String?) -> Unit,
    onDistanceChange: (Int) -> Unit,
    onOnlyNewChange: (Boolean) -> Unit = {},
    onOnlyWithOffersChange: (Boolean) -> Unit = {},
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf("Όλα", "Ανδρική ένδυση", "Ανδρική υπόδυση", "Γυναικεία ένδυση", "Γυναικεία υπόδυση", "Παιδική ένδυση", "Παιδική υπόδυση", "Αξεσουάρ")

    var category by remember { mutableStateOf(selectedCategory ?: "Όλα") }
    var distance by remember { mutableStateOf(selectedDistance) }
    var onlyNewOffers by remember { mutableStateOf(onlyNew) }
    var onlyActiveShops by remember { mutableStateOf(onlyWithOffers) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onCategoryChange(if (category == "Όλα") null else category)
                onDistanceChange(distance)
                if (isShopMode) {
                    onOnlyWithOffersChange(onlyActiveShops)
                } else {
                    onOnlyNewChange(onlyNewOffers)
                }
                onApply()
            }) {
                Text("Εφαρμογή")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) {
                    Text("Επαναφορά")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Άκυρο")
                }
            }
        },
        title = { Text("Φίλτρα") },
        text = {
            Column {
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Κατηγορία") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    category = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Απόσταση: $distance km")
                Slider(
                    value = distance.toFloat(),
                    onValueChange = { distance = it.toInt() },
                    valueRange = 1f..20f,
                    steps = 18
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isShopMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = onlyActiveShops,
                            onCheckedChange = { onlyActiveShops = it }
                        )
                        Text("Μόνο με προσφορές")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = onlyNewOffers,
                            onCheckedChange = { onlyNewOffers = it }
                        )
                        Text("Μόνο νέες προσφορές")
                    }
                }
            }
        }
    )
}
