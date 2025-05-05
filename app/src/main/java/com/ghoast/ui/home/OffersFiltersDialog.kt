package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.alpha
import com.ghoast.ui.viewmodel.SortMode

@Composable
fun OffersFiltersDialog(
    selectedCategory: String?,
    selectedDistance: Int,
    selectedSortMode: SortMode,
    onCategoryChange: (String?) -> Unit,
    onDistanceChange: (Int) -> Unit,
    onSortModeChange: (SortMode) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val localCategory = remember { mutableStateOf(selectedCategory) }
    val localDistance = remember { mutableStateOf(selectedDistance) }
    val localSort = remember { mutableStateOf(selectedSortMode) }

    val scrollState = rememberScrollState()
    val showIndicator by remember {
        derivedStateOf {
            scrollState.value < scrollState.maxValue
        }
    }

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

                    val categories = listOf(
                        null to "Όλες οι κατηγορίες",
                        "Γυναικεία ένδυση" to "Γυναικεία ένδυση",
                        "Γυναικεία υπόδηση" to "Γυναικεία υπόδηση",
                        "Ανδρική ένδυση" to "Ανδρική ένδυση",
                        "Ανδρική υπόδηση" to "Ανδρική υπόδηση",
                        "Παιδική ένδυση" to "Παιδική ένδυση",
                        "Παιδική υπόδηση" to "Παιδική υπόδηση",
                        "Αθλητική ένδυση" to "Αθλητική ένδυση",
                        "Αθλητική υπόδηση" to "Αθλητική υπόδηση",
                        "Εσώρουχα" to "Εσώρουχα",
                        "Καλλυντικά" to "Καλλυντικά",
                        "Αξεσουάρ" to "Αξεσουάρ",
                        "Κοσμήματα" to "Κοσμήματα",
                        "Οπτικά" to "Οπτικά",
                        "Ρολόγια" to "Ρολόγια"
                    )


                    categories.forEach { (value, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = localCategory.value == value,
                                onClick = { localCategory.value = value }
                            )
                            Text(label)
                        }
                    }

                    Text("Απόσταση: έως ${localDistance.value} km")
                    Slider(
                        value = localDistance.value.toFloat(),
                        onValueChange = { localDistance.value = it.toInt() },
                        valueRange = 1f..100f,
                        steps = 99
                    )

                    Text("Ταξινόμηση κατά:", style = MaterialTheme.typography.titleMedium)
                    SortMode.values().forEach { mode ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = localSort.value == mode,
                                onClick = { localSort.value = mode }
                            )
                            Text(mode.label)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onReset) { Text("Επαναφορά") }
                        TextButton(onClick = onDismiss) { Text("Άκυρο") }
                        Button(onClick = {
                            onCategoryChange(localCategory.value)
                            onDistanceChange(localDistance.value)
                            onSortModeChange(localSort.value)
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
