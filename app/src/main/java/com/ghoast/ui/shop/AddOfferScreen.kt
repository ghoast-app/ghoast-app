package com.ghoast.ui.shop

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOfferScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: AddOfferViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var selectedDiscount by remember { mutableStateOf<String?>(null) }
    var discountDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var imageUris = remember { mutableStateListOf<Uri>() }

    val discountOptions = listOf("10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%")
    val categoryOptions = listOf(
        "Ανδρική ένδυση", "Ανδρική υπόδηση",
        "Γυναικεία ένδυση", "Γυναικεία υπόδηση",
        "Παιδική ένδυση", "Παιδική υπόδηση",
        "Αξεσουάρ"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val availableSlots = 3 - imageUris.size
        val urisToAdd = uris.take(availableSlots)
        imageUris.addAll(urisToAdd)

        if (uris.size > availableSlots) {
            Toast.makeText(context, "Μπορείτε να ανεβάσετε μέχρι 3 εικόνες.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("➕ Προσθήκη Προσφοράς", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = title,
            onValueChange = {
                if (it.text.length <= 50) title = it
            },
            label = { Text("Τίτλος προσφοράς (μέχρι 50 χαρακτήρες)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = {
                if (it.text.length <= 300) description = it
            },
            label = { Text("Περιγραφή (μέχρι 300 χαρακτήρες)") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = discountDropdownExpanded,
            onExpandedChange = { discountDropdownExpanded = !discountDropdownExpanded }
        ) {
            TextField(
                value = selectedDiscount ?: "Επιλογή έκπτωσης",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(discountDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = discountDropdownExpanded,
                onDismissRequest = { discountDropdownExpanded = false }
            ) {
                discountOptions.forEach { discount ->
                    DropdownMenuItem(
                        text = { Text(discount) },
                        onClick = {
                            selectedDiscount = discount
                            discountDropdownExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = categoryDropdownExpanded,
            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
        ) {
            TextField(
                value = selectedCategory ?: "Επιλογή κατηγορίας",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryDropdownExpanded,
                onDismissRequest = { categoryDropdownExpanded = false }
            ) {
                categoryOptions.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            categoryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            imageUris.forEach { uri ->
                Box(modifier = Modifier.size(64.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                    IconButton(
                        onClick = { imageUris.remove(uri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Αφαίρεση εικόνας",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                imagePickerLauncher.launch("image/*")
            },
            enabled = imageUris.size < 3
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Επιλογή εικόνων")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.text.isBlank() || description.text.isBlank() ||
                    selectedDiscount.isNullOrBlank() || selectedCategory.isNullOrBlank() ||
                    imageUris.isEmpty()
                ) {
                    Toast.makeText(context, "Συμπληρώστε όλα τα πεδία και επιλέξτε εικόνες.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                scope.launch {
                    viewModel.saveOffer(
                        title = title.text,
                        description = description.text,
                        discount = selectedDiscount.orEmpty(),
                        category = selectedCategory.orEmpty(),
                        imageUris = imageUris,
                        onSuccess = {
                            Toast.makeText(context, "Η προσφορά αποθηκεύτηκε!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onError = {
                            Toast.makeText(context, "Σφάλμα: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Αποθήκευση Προσφοράς")
        }
    }
}
