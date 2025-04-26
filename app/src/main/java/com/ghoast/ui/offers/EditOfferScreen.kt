package com.ghoast.ui.offers

import android.net.Uri
import android.util.Log
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


@OptIn(ExperimentalMaterial3Api::class)
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
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val discountOptions = (10..90 step 5).map { "$it%" }
    val categoryOptions = listOf(
        "Ανδρική ένδυση", "Ανδρική υπόδηση",
        "Γυναικεία ένδυση", "Γυναικεία υπόδηση",
        "Παιδική ένδυση", "Παιδική υπόδηση",
        "Αξεσουάρ"
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
        Log.d("EditOfferScreen", "Loading offer with ID: $offerId")
        viewModel.loadOffer(offerId)
    }

    offer?.let { currentOffer ->
        if (title.isBlank()) title = currentOffer.title
        if (description.isBlank()) description = currentOffer.description
        if (discount.isBlank()) discount = currentOffer.discount
        if (category.isBlank()) category = currentOffer.category

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
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Περιγραφή") },
                modifier = Modifier.fillMaxWidth()
            )

            // Discount Dropdown
            ExposedDropdownMenuBox(
                expanded = discountDropdownExpanded.value,
                onExpandedChange = { discountDropdownExpanded.value = !discountDropdownExpanded.value }
            ) {
                TextField(
                    value = discount,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ποσοστό έκπτωσης") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(discountDropdownExpanded.value)
                    },
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

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded.value,
                onExpandedChange = { categoryDropdownExpanded.value = !categoryDropdownExpanded.value }
            ) {
                TextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Κατηγορία") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(categoryDropdownExpanded.value)
                    },
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
                if (newImageUris.isNotEmpty()) {
                    items(newImageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.size(width = 250.dp, height = 160.dp)
                        )
                    }
                } else {
                    items(currentOffer.imageUrls) { url ->
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = null,
                            modifier = Modifier.size(width = 250.dp, height = 160.dp)
                        )
                    }
                }
            }

            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Επιλογή νέων εικόνων")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val updatedOffer = currentOffer.copy(
                            title = title,
                            description = description,
                            discount = discount,
                            category = category
                        )

                        viewModel.updateOffer(
                            offerId = offerId,
                            updatedOffer = updatedOffer,
                            newImageUris = newImageUris
                        )
                        Toast.makeText(context, "Η προσφορά ενημερώθηκε", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Αποθήκευση αλλαγών")
            }

            Button(
                onClick = {
                    Log.d("DEBUG_DELETE", "Κουμπί διαγραφής πατήθηκε")
                    showDeleteDialog = true

                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Διαγραφή προσφοράς")
            }

            if (errorMessage != null) {
                Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteDialog) {
        Log.d("DEBUG_DELETE", "Το showDeleteDialog είναι true – προσπαθεί να εμφανιστεί το AlertDialog")
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Επιβεβαίωση διαγραφής") },
            text = { Text("Είστε σίγουροι ότι θέλετε να διαγράψετε αυτή την προσφορά;") },
            confirmButton = {
                TextButton(onClick = {
                    Log.d("EditOfferScreen", "Confirmed deletion")
                    showDeleteDialog = false
                    viewModel.deleteOffer(offerId) {
                        Toast.makeText(context, "Η προσφορά διαγράφηκε", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }) {
                    Text("Ναι")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    Log.d("EditOfferScreen", "Cancelled deletion")
                    showDeleteDialog = false
                }) {
                    Text("Άκυρο")
                }
            }
        )
    }

    if (offer == null && !isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Η προσφορά δεν βρέθηκε.")
        }
    }
}