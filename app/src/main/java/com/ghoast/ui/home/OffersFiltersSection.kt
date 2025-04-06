package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OffersFiltersSection(
    selectedCategory: String?,
    selectedDistance: Int?,
    onCategoryChange: (String?) -> Unit,
    onDistanceChange: (Int?) -> Unit
) {
    val categoryOptions = listOf(
        "Ανδρική ένδυση", "Ανδρική υπόδηση",
        "Γυναικεία ένδυση", "Γυναικεία υπόδηση",
        "Παιδική ένδυση", "Παιδική υπόδηση", "Αξεσουάρ"
    )
    val distanceOptions = listOf(1, 2, 5, 10, 20)

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var distanceDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            // Φίλτρο Κατηγορίας
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedCategory ?: "Κατηγορία",
                    onValueChange = {},
                    label = { Text("Κατηγορία") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .weight(1f)
                )

                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categoryOptions.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onCategoryChange(category)
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Φίλτρο Απόστασης
            ExposedDropdownMenuBox(
                expanded = distanceDropdownExpanded,
                onExpandedChange = { distanceDropdownExpanded = !distanceDropdownExpanded }
            ) {
                TextField(
                    readOnly = true,
                    value = if (selectedDistance != null) "${selectedDistance}km" else "Απόσταση",
                    onValueChange = {},
                    label = { Text("Απόσταση") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = distanceDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .weight(1f)
                )

                ExposedDropdownMenu(
                    expanded = distanceDropdownExpanded,
                    onDismissRequest = { distanceDropdownExpanded = false }
                ) {
                    distanceOptions.forEach { distance ->
                        DropdownMenuItem(
                            text = { Text("$distance km") },
                            onClick = {
                                onDistanceChange(distance)
                                distanceDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
