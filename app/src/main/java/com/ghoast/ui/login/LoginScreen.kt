package com.ghoast.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ghoast.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

import com.ghoast.utils.FCMTokenUtils // ✅ import for FCM update

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "✅ Είσοδος επιτυχής", Toast.LENGTH_SHORT).show()

                                // ✅ Ενημέρωση FCM Token στο Firestore
                                FCMTokenUtils.updateFCMToken()

                                navController.navigate(Screen.OffersHome.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }

                            } else {
                                Toast.makeText(context, "❌ Αποτυχία σύνδεσης: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Συμπληρώστε email και κωδικό", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate(Screen.RegisterUser.route)
        }) {
            Text("Δεν έχετε λογαριασμό; Εγγραφή Χρήστη")
        }

        TextButton(onClick = {
            navController.navigate(Screen.RegisterShop.route)
        }) {
            Text("Εγγραφή Καταστήματος")
        }
    }
}
