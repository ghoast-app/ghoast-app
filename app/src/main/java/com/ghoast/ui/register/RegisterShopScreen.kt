// RegisterShopScreen.kt
package com.ghoast.ui.register

import android.app.Activity
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.ghoast.model.WorkingHour
import com.ghoast.ui.navigation.Screen
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterShopScreen(navController: NavHostController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val viewModel: RegisterShopViewModel = viewModel()

    var email by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatch by remember { mutableStateOf(false) }

    var shopName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latLng by remember { mutableStateOf<LatLng?>(null) }
    var website by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val workingDays = listOf("Δευτέρα", "Τρίτη", "Τετάρτη", "Πέμπτη", "Παρασκευή", "Σάββατο", "Κυριακή")
    val workingHours = remember {
        mutableStateListOf<WorkingHour>().apply {
            workingDays.forEach { add(WorkingHour(it)) }
        }
    }

    val categoryOptions = listOf(
        "Γυναικεία ένδυση", "Γυναικεία υπόδηση", "Ανδρική ένδυση", "Ανδρική υπόδηση",
        "Παιδική ένδυση", "Παιδική υπόδηση", "Αθλητική ένδυση", "Αθλητική υπόδηση",
        "Εσώρουχα", "Καλλυντικά", "Αξεσουάρ", "Κοσμήματα", "Οπτικά", "Ρολόγια"
    )
    val selectedCategories = remember { mutableStateListOf<String>() }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            address = place.address ?: ""
            latLng = place.latLng
        }
    }

    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Εγγραφή Λογαριασμού", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Σύνδεσης") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordMismatch = false
            },
            label = { Text("Κωδικός") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordMismatch = false
            },
            label = { Text("Επιβεβαίωση Κωδικού") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (passwordMismatch) {
            Text("❗ Οι κωδικοί δεν ταιριάζουν", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Δημιουργία Καταστήματος", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("Όνομα Καταστήματος") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            ).build(context)
            launcher.launch(intent)
        }) {
            Text(if (address.isBlank()) "Επιλέξτε διεύθυνση" else address)
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.all(Char::isDigit)) phone = it },
            label = { Text("Τηλέφωνο Επικοινωνίας") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contactEmail,
            onValueChange = { contactEmail = it },
            label = { Text("Email Επικοινωνίας Καταστήματος") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = website,
            onValueChange = { website = it },
            label = { Text("Ιστοσελίδα ή Social Link") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = categoryDropdownExpanded,
            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
        ) {
            TextField(
                value = selectedCategories.joinToString(", "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Κατηγορίες") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryDropdownExpanded,
                onDismissRequest = { categoryDropdownExpanded = false },
                modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())
            ) {
                categoryOptions.forEach { category ->
                    val selected = category in selectedCategories
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            if (selected) selectedCategories.remove(category) else selectedCategories.add(category)
                        },
                        leadingIcon = { if (selected) Icon(Icons.Rounded.Check, null) }
                    )
                }
            }
        }

        Text("Ώρες Λειτουργίας", style = MaterialTheme.typography.titleMedium)
        workingHours.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = item.enabled, onCheckedChange = {
                    workingHours[workingHours.indexOf(item)] = item.copy(enabled = it)
                })
                Text(item.day, modifier = Modifier.weight(1f))
                Button(onClick = {
                    val cal = Calendar.getInstance()
                    TimePickerDialog(context, { _, h, m ->
                        workingHours[workingHours.indexOf(item)] = item.copy(from = "%02d:%02d".format(h, m))
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                }) { Text(item.from ?: "Από") }
                Button(onClick = {
                    val cal = Calendar.getInstance()
                    TimePickerDialog(context, { _, h, m ->
                        workingHours[workingHours.indexOf(item)] = item.copy(to = "%02d:%02d".format(h, m))
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                }) { Text(item.to ?: "Έως") }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(CircleShape)
                )
            } else {
                Surface(modifier = Modifier.size(64.dp).clip(CircleShape), color = MaterialTheme.colorScheme.surfaceVariant) {}
            }
            Text("Επιλογή φωτογραφίας προφίλ")
        }

        Button(
            onClick = {
                if (password != confirmPassword) {
                    passwordMismatch = true
                    Toast.makeText(context, "❗ Οι κωδικοί δεν ταιριάζουν", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val auth = FirebaseAuth.getInstance()
                isLoading = true
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        auth.currentUser?.sendEmailVerification()
                        viewModel.registerShop(
                            shopName = shopName,
                            address = address,
                            phone = phone,
                            website = website,
                            email = contactEmail,
                            contactEmail = contactEmail,
                            category = selectedCategories.joinToString(", "),
                            workingHours = workingHours.map {
                                mapOf("day" to it.day, "from" to (it.from ?: ""), "to" to (it.to ?: ""))
                            },
                            profileImageUri = imageUri,
                            latitude = latLng?.latitude ?: 0.0,
                            longitude = latLng?.longitude ?: 0.0,
                            onSuccess = {
                                isLoading = false
                                Toast.makeText(context, "📩 Επιτυχής εγγραφή! Ελέγξτε το email σας.", Toast.LENGTH_LONG).show()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.RegisterShop.route) { inclusive = true }
                                }
                            },
                            onError = {
                                isLoading = false
                                Toast.makeText(context, "Σφάλμα: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, "Σφάλμα: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Παρακαλώ περιμένετε..." else "Εγγραφή")
        }
    }
}
