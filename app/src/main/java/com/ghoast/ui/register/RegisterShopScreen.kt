// Αρχή RegisterShopScreen.kt

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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.ghoast.model.WorkingHour
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ghoast.ui.navigation.Screen
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterShopScreen(navController: NavHostController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

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

    val categoryOptions = listOf("Ανδρική ένδυση", "Ανδρική υπόδηση", "Γυναικεία ένδυση", "Γυναικεία υπόδηση", "Παιδική ένδυση", "Παιδική υπόδηση", "Αξεσουάρ")
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
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.all { c -> c.isDigit() }) phone = it },
            label = { Text("Τηλέφωνο") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )

        // 🔁 Ώρες λειτουργίας
        Text("Ώρες Λειτουργίας", style = MaterialTheme.typography.titleMedium)
        workingHours.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
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
                        focusManager.clearFocus() // ✅ προσθήκη
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
                        focusManager.clearFocus() // ✅ προσθήκη
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                }) {
                    Text(item.to ?: "Έως")
                }
            }
        }

        // ✅ Dropdown με κατηγορίες
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
                            if (selected) selectedCategories.remove(category) else selectedCategories.add(category)
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
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            auth.currentUser?.sendEmailVerification()
                            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                            val shopData = hashMapOf(
                                "email" to email,
                                "shopName" to shopName,
                                "address" to address,
                                "latitude" to latLng?.latitude,
                                "longitude" to latLng?.longitude,
                                "website" to website,
                                "phone" to phone,
                                "workingHours" to workingHours.filter { it.enabled },
                                "categories" to selectedCategories,
                                "profilePhotoUri" to imageUri?.toString()
                            )

                            db.collection("shops").document(userId).set(shopData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Εγγραφή επιτυχής! Ελέγξτε το email σας.", Toast.LENGTH_LONG).show()
                                    isLoading = false
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.RegisterShop.route) { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Σφάλμα καταχώρησης στο Firestore.", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                }
                        } else {
                            Toast.makeText(context, "Αποτυχία εγγραφής: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Παρακαλώ περιμένετε..." else "Εγγραφή")
        }
    }
}
