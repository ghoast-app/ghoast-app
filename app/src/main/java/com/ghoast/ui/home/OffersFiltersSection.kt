package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OffersFiltersSection(
    selectedCategory: String?,
    selectedDistance: Int?,
    onCategoryChange: (String?) -> Unit,
    onDistanceChange: (Int?) -> Unit
) {
    val categoryOptions = listOf(
        "Όλα",
        "Ανδρική ένδυση", "Ανδρική υπόδηση",
        "Γυναικεία ένδυση", "Γυναικεία υπόδηση",
        "Παιδική ένδυση", "Παιδική υπόδηση",
        "Αξεσουάρ"
    )

    val distanceOptions = listOf<Int?>(null, 1, 2, 5, 10, 20)

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var distanceDropdownExpanded by remember { mutableStateOf(false) }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Κατηγορία
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
                            onCategoryChange(if (category == "Όλα") null else category)
                            categoryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Απόσταση
        ExposedDropdownMenuBox(
            expanded = distanceDropdownExpanded,
            onExpandedChange = { distanceDropdownExpanded = !distanceDropdownExpanded }
        ) {
            TextField(
                readOnly = true,
                value = selectedDistance?.let { "${it}km" } ?: "Απόσταση",
                onValueChange = {}, // ❗Χρειάζεται ακόμα και αν είναι readOnly
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
                    val label = distance?.let { "${it}km" } ?: "Όλες οι αποστάσεις"
                    DropdownMenuItem(
                        text = { Text(label) },
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
