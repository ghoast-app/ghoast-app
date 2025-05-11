package com.ghoast.ui.shop

import android.app.TimePickerDialog
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.ghoast.model.WorkingHour
import com.ghoast.viewmodel.EditShopProfileViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShopProfileScreen(
    navController: NavHostController,
    shopId: String? = null,
    viewModel: EditShopProfileViewModel = viewModel(),
    onSaveSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val shop by viewModel.shop.collectAsState()
    val scrollState = rememberScrollState()

    Log.d("EditShopScreen", "🚀 Composable launched with shopId=$shopId")

    var shopName by remember { mutableStateOf("") }
    var shopCategory by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    LaunchedEffect(shopId) {
        Log.d("EditShopScreen", "📡 Calling loadShopById with $shopId")
        viewModel.loadShopById(shopId)
    }

    LaunchedEffect(shop) {
        Log.d("EditShopScreen", "🧾 shop loaded: ${shop?.shopName}")
        shopName = shop?.shopName ?: ""
        shopCategory = shop?.categories?.firstOrNull() ?: ""
        phone = shop?.phone ?: ""
        email = shop?.email ?: ""
        website = shop?.website ?: ""
        address = shop?.address ?: ""
        latitude = shop?.latitude ?: 0.0
        longitude = shop?.longitude ?: 0.0
    }

    val categories = listOf(
        "Γυναικεία ένδυση",
        "Γυναικεία υπόδηση",
        "Ανδρική ένδυση",
        "Ανδρική υπόδηση",
        "Παιδική ένδυση",
        "Παιδική υπόδηση",
        "Αθλητική ένδυση",
        "Αθλητική υπόδηση",
        "Εσώρουχα",
        "Καλλυντικά",
        "Αξεσουάρ",
        "Κοσμήματα",
        "Οπτικά",
        "Ρολόγια"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showSavedDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { selectedImageUri = it } }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            address = place.address ?: ""
            place.latLng?.let {
                latitude = it.latitude
                longitude = it.longitude
            }
        }
    }

    val defaultHours = remember {
        mutableStateListOf<WorkingHour>().apply {
            val days = listOf("Δευτέρα", "Τρίτη", "Τετάρτη", "Πέμπτη", "Παρασκευή", "Σάββατο", "Κυριακή")
            days.forEach { day ->
                val existing = shop?.workingHours?.find { it.day == day }
                add(existing ?: WorkingHour(day, "", "", false))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Επεξεργασία Προφίλ", style = MaterialTheme.typography.titleLarge)

        selectedImageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
        } ?: shop?.profilePhotoUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
        }

        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Επιλογή Εικόνας")
        }

        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("Όνομα") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                readOnly = true,
                value = shopCategory,
                onValueChange = {},
                label = { Text("Κατηγορία") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        shopCategory = it
                        expanded = false
                    })
                }
            }
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Τηλέφωνο") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = website,
            onValueChange = { website = it },
            label = { Text("Ιστοσελίδα") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val fields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(context)
                launcher.launch(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Επιλογή Διεύθυνσης")
        }

        if (address.isNotBlank()) {
            Text("Διεύθυνση: $address", style = MaterialTheme.typography.bodyLarge)
        }

        Text("Ώρες Λειτουργίας", style = MaterialTheme.typography.titleMedium)

        defaultHours.forEachIndexed { index, hour ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = hour.enabled,
                    onCheckedChange = {
                        defaultHours[index] = defaultHours[index].copy(enabled = it)
                    }
                )
                Text(hour.day, modifier = Modifier.weight(1f))
                Button(onClick = {
                    val cal = Calendar.getInstance()
                    TimePickerDialog(context, { _, h, m ->
                        val selectedTime = String.format("%02d:%02d", h, m)
                        defaultHours[index] = defaultHours[index].copy(from = selectedTime)
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                }) {
                    Text(text = "Από: ${hour.from.orEmpty().ifEmpty { "--:--" }}")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = {
                    val cal = Calendar.getInstance()
                    TimePickerDialog(context, { _, h, m ->
                        val selectedTime = String.format("%02d:%02d", h, m)
                        defaultHours[index] = defaultHours[index].copy(to = selectedTime)
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                }) {
                    Text("Έως: ${hour.to.orEmpty().ifEmpty { "--:--" }}")
                }
            }
        }

        Button(
            onClick = {
                isUploading = true
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                val storageRef = FirebaseStorage.getInstance().reference.child("shops/$uid/profile.jpg")

                val saveToFirestore: (String) -> Unit = { imageUrl ->
                    viewModel.updateShopProfile(
                        shopId,
                        shopName, shopCategory, phone, email, website,
                        address, imageUrl, defaultHours, latitude, longitude
                    ) {
                        isUploading = false
                        showSavedDialog = true
                    }
                }

                if (selectedImageUri != null) {
                    storageRef.putFile(selectedImageUri!!)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                saveToFirestore(uri.toString())
                            }
                        }
                } else {
                    saveToFirestore(shop?.profilePhotoUri ?: "")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isUploading) "Αποθήκευση..." else "Αποθήκευση αλλαγών")
        }
    }

    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showSavedDialog = false
                    navController.popBackStack()
                }) {
                    Text("ΟΚ")
                }
            },
            title = { Text("Επιτυχία") },
            text = { Text("Οι αλλαγές αποθηκεύτηκαν με επιτυχία.") }
        )
    }
}
