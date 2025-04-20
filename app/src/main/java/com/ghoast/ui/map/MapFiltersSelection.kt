package com.ghoast.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun MapFiltersSection(
    selectedCategory: String?,
    selectedDistance: Int,
    onCategoryChange: (String?) -> Unit,
    onDistanceChange: (Int) -> Unit
) {
    val categories = listOf("Όλα", "Ανδρική ένδυση", "Ανδρική υπόδυση", "Γυναικεία ένδυση", "Γυναικεία υπόδυση", "Παιδική ένδυση", "Παιδική υπόδυση", "Αξεσουάρ")

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {

        // Κατηγορία
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCategory ?: "Όλα",
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
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onCategoryChange(if (category == "Όλα") null else category)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Απόσταση με slider
        Text(text = "Απόσταση: $selectedDistance km", style = MaterialTheme.typography.bodyMedium)

        Slider(
            value = selectedDistance.toFloat(),
            onValueChange = { onDistanceChange(it.toInt()) },
            valueRange = 1f..20f,
            steps = 18,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
