package com.ghoast.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersFiltersDialog(
    selectedCategory: String?,
    selectedDistance: Int,
    onCategoryChange: (String?) -> Unit,
    onDistanceChange: (Int) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf("Όλα", "Ανδρική ένδυση", "Ανδρική υπόδυση", "Γυναικεία ένδυση", "Γυναικεία υπόδυση", "Παιδική ένδυση", "Παιδική υπόδυση", "Αξεσουάρ")

    var localCategory by remember { mutableStateOf(selectedCategory ?: "Όλα") }
    var localDistance by remember { mutableStateOf(selectedDistance) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = {
                onCategoryChange(if (localCategory == "Όλα") null else localCategory)


                onDistanceChange(localDistance)
                onApply()
            }) {
                Text("Εφαρμογή")
            }
        },
        dismissButton = {
            TextButton(onClick = { onReset() }) {
                Text("Επαναφορά")
            }
        },
        title = { Text("Φίλτρα") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                // 🔽 DropDown για κατηγορία
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = localCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Κατηγορία") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    localCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🎚 Απόσταση
                Text("Απόσταση: $localDistance km")
                Slider(
                    value = localDistance.toFloat(),
                    onValueChange = { localDistance = it.toInt() },
                    valueRange = 1f..20f,
                    steps = 18,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
