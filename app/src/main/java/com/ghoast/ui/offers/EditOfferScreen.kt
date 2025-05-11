@file:OptIn(ExperimentalMaterial3Api::class)

package com.ghoast.ui.offers

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.ghoast.viewmodel.EditOfferViewModel
import kotlinx.coroutines.launch

@Composable
fun EditOfferScreen(
    navController: NavController,
    offerId: String
) {
    val context = LocalContext.current
    val viewModel: EditOfferViewModel = viewModel()

    val offer by viewModel.offer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val updateCompleted by viewModel.updateCompleted.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val discountOptions = (10..90 step 5).map { "$it%" }
    val categoryOptions = listOf(
        "Γυναικεία ένδυση", "Γυναικεία υπόδηση", "Ανδρική ένδυση", "Ανδρική υπόδηση",
        "Παιδική ένδυση", "Παιδική υπόδηση", "Αθλητική ένδυση", "Αθλητική υπόδηση",
        "Εσώρουχα", "Καλλυντικά", "Αξεσουάρ", "Κοσμήματα", "Οπτικά", "Ρολόγια"
    )

    val discountDropdownExpanded = remember { mutableStateOf(false) }
    val categoryDropdownExpanded = remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.size <= 3) {
            newImageUris = uris
        } else {
            Toast.makeText(context, "Μπορείτε να επιλέξετε μέχρι 3 εικόνες.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(offerId) {
        viewModel.loadOffer(offerId)
    }

    LaunchedEffect(updateCompleted) {
        if (updateCompleted) {
            Toast.makeText(context, "Η προσφορά ενημερώθηκε", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    LaunchedEffect(offer) {
        offer?.let {
            title = it.title
            description = it.description
            discount = it.discount
            category = it.category
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else offer?.let { currentOffer ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Επεξεργασία Προσφοράς", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Τίτλος") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Περιγραφή") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = discountDropdownExpanded.value,
                onExpandedChange = { discountDropdownExpanded.value = !discountDropdownExpanded.value }
            ) {
                TextField(
                    value = discount,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ποσοστό έκπτωσης") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(discountDropdownExpanded.value) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = discountDropdownExpanded.value,
                    onDismissRequest = { discountDropdownExpanded.value = false }
                ) {
                    discountOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                discount = option
                                discountDropdownExpanded.value = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded.value,
                onExpandedChange = { categoryDropdownExpanded.value = !categoryDropdownExpanded.value }
            ) {
                TextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Κατηγορία") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryDropdownExpanded.value) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded.value,
                    onDismissRequest = { categoryDropdownExpanded.value = false }
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                categoryDropdownExpanded.value = false
                            }
                        )
                    }
                }
            }

            Text("Εικόνες προσφοράς", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val imagesToShow = if (newImageUris.isNotEmpty()) newImageUris.map { it.toString() } else currentOffer.imageUrls
                items(imagesToShow) { urlOrUri ->
                    Image(
                        painter = rememberAsyncImagePainter(urlOrUri),
                        contentDescription = null,
                        modifier = Modifier.size(width = 250.dp, height = 160.dp)
                    )
                }
            }

            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Επιλογή νέων εικόνων")
            }

            Button(
                onClick = {
                    val updated = currentOffer.copy(
                        title = title,
                        description = description,
                        discount = discount,
                        category = category
                    )
                    viewModel.updateOffer(context, offerId, updated, newImageUris)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Αποθήκευση αλλαγών")
            }

            currentOffer.id?.let { id ->
                Button(
                    onClick = {
                        viewModel.deleteOffer(id) {
                            Toast.makeText(context, "Η προσφορά διαγράφηκε", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Διαγραφή προσφοράς")
                }
            }

            if (errorMessage != null) {
                Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (offer == null && !isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Η προσφορά δεν βρέθηκε.")
        }
    }
}
