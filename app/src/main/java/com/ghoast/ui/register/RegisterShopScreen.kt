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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterShopScreen(navController: NavHostController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val viewModel: RegisterShopViewModel = viewModel()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
        Spacer(modifier = Modifier.height(32.dp))
        Text("Εγγραφή Καταστήματος", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("Όνομα Καταστήματος") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
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
            value = website,
            onValueChange = { website = it },
            label = { Text("Ιστοσελίδα") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.all { c -> c.isDigit() }) phone = it },
            label = { Text("Τηλέφωνο") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Ώρες Λειτουργίας", style = MaterialTheme.typography.titleMedium)
        workingHours.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = item.enabled,
                    onCheckedChange = {
                        val index = workingHours.indexOf(item)
                        if (index != -1) {
                            workingHours[index] = item.copy(enabled = it)
                        }
                    }
                )
                Text(item.day, modifier = Modifier.weight(1f))
                Button(onClick = {
                    val cal = Calendar.getInstance()
                    TimePickerDialog(context, { _, hour, minute ->
                        val index = workingHours.indexOf(item)
                        if (index != -1) {
                            workingHours[index] = item.copy(from = "%02d:%02d".format(hour, minute))
                        }
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                }) {
                    Text(item.from ?: "Από")
                }
                Button(onClick = {
                    val cal = Calendar.getInstance()
                    TimePickerDialog(context, { _, hour, minute ->
                        val index = workingHours.indexOf(item)
                        if (index != -1) {
                            workingHours[index] = item.copy(to = "%02d:%02d".format(hour, minute))
                        }
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                }) {
                    Text(item.to ?: "Έως")
                }
            }
        }

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
                onDismissRequest = { categoryDropdownExpanded = false }
            ) {
                categoryOptions.forEach { category ->
                    val selected = category in selectedCategories
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            if (selected) selectedCategories.remove(category)
                            else selectedCategories.add(category)
                        },
                        leadingIcon = { if (selected) Icon(Icons.Rounded.Check, null) }
                    )
                }
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
                isLoading = true
                val auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        auth.currentUser?.sendEmailVerification() // ✅
                        viewModel.registerShop(
                            shopName = shopName,
                            address = address,
                            phone = phone,
                            website = website,
                            email = email,
                            category = selectedCategories.joinToString(", "),
                            workingHours = workingHours.map {
                                mapOf(
                                    "day" to it.day,
                                    "from" to (it.from ?: ""),
                                    "to" to (it.to ?: "")
                                )
                            },
                            profileImageUri = imageUri,
                            latitude = latLng?.latitude ?: 0.0,
                            longitude = latLng?.longitude ?: 0.0,
                            onSuccess = {
                                isLoading = false
                                Toast.makeText(context, "📩 Επιτυχής εγγραφή! Ελέγξτε το email σας για επιβεβαίωση.", Toast.LENGTH_LONG).show()
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
