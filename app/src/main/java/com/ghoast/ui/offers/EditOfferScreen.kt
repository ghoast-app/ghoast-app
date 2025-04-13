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

    // Επιλογές
    val discountOptions = (10..90 step 5).map { "$it%" }
    val categoryOptions = listOf(
        "Ανδρική ένδυση", "Ανδρική υπόδηση",
        "Γυναικεία ένδυση", "Γυναικεία υπόδηση",
        "Παιδική ένδυση", "Παιδική υπόδηση",
        "Αξεσουάρ"
    )

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.size <= 3) {
            newImageUris = uris
        } else {
            Toast.makeText(context, "Μπορείτε να επιλέξετε μέχρι 3 εικόνες.", Toast.LENGTH_SHORT).show()
        }
    }

    var discountDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(offerId) {
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

            // Ποσοστό έκπτωσης dropdown
            ExposedDropdownMenuBox(
                expanded = discountDropdownExpanded,
                onExpandedChange = { discountDropdownExpanded = !discountDropdownExpanded }
            ) {
                TextField(
                    value = discount,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ποσοστό έκπτωσης") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(discountDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = discountDropdownExpanded,
                    onDismissRequest = { discountDropdownExpanded = false }
                ) {
                    discountOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                discount = option
                                discountDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Κατηγορία dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
            ) {
                TextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Κατηγορία") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Προεπισκόπηση εικόνων
            Text("Εικόνες προσφοράς", style = MaterialTheme.typography.titleMedium)

            if (newImageUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(newImageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .height(160.dp)
                                .width(250.dp)
                        )
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentOffer.imageUrls) { url ->
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = null,
                            modifier = Modifier
                                .height(160.dp)
                                .width(250.dp)
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
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Αποθήκευση αλλαγών")
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLoading) CircularProgressIndicator()
            else Text("Η προσφορά δεν βρέθηκε.")
        }
    }
}
