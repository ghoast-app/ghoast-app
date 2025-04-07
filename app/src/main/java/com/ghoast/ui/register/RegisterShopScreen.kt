package com.ghoast.ui.register

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ghoast.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterShopScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latLng by remember { mutableStateOf<LatLng?>(null) }
    var website by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val categoryOptions = listOf(
        "Ανδρική ένδυση", "Ανδρική υπόδηση",
        "Γυναικεία ένδυση", "Γυναικεία υπόδηση",
        "Παιδική ένδυση", "Παιδική υπόδηση",
        "Αξεσουάρ"
    )
    val selectedCategories = remember { mutableStateListOf<String>() }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val place = Autocomplete.getPlaceFromIntent(data!!)
            address = place.address ?: ""
            latLng = place.latLng
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Εγγραφή Καταστήματος", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("Όνομα Καταστήματος") }, modifier = Modifier.fillMaxWidth())

        // 👉 Κουμπί για επιλογή διεύθυνσης
        Button(onClick = {
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                fields
            ).build(context)
            launcher.launch(intent)
        }) {
            Text(if (address.isBlank()) "Επιλέξτε διεύθυνση" else address)
        }

        OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("Ιστοσελίδα") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Τηλέφωνο") }, modifier = Modifier.fillMaxWidth())

        // Multi-select Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryDropdownExpanded,
            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
        ) {
            TextField(
                value = selectedCategories.joinToString(", "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Κατηγορίες") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = categoryDropdownExpanded,
                onDismissRequest = { categoryDropdownExpanded = false }
            ) {
                categoryOptions.forEach { category ->
                    val isSelected = category in selectedCategories
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            if (isSelected) selectedCategories.remove(category)
                            else selectedCategories.add(category)
                        },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(Icons.Rounded.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }

        // Profile Image Picker
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.clickable {
                imagePickerLauncher.launch("image/*")
            }
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {}
            }
            Text("Επιλογή φωτογραφίας προφίλ")
        }

        // Register Button
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
                                "categories" to selectedCategories,
                                "profilePhotoUri" to imageUri?.toString()
                            )

                            db.collection("shops").document(userId)
                                .set(shopData)
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
