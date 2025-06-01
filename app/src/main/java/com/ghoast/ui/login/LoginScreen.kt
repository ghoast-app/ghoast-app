package com.ghoast.ui.login

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.utils.FCMTokenUtils
import com.ghoast.utils.StayLoggedInPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    navController: NavHostController,
    sessionViewModel: UserSessionViewModel,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var stayLoggedIn by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = stayLoggedIn,
                        onCheckedChange = { stayLoggedIn = it }
                    )
                    Text("Μείνε συνδεδεμένος")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Ξέχασες τον κωδικό;",
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Feature: Ανάκτηση κωδικού – Coming soon", Toast.LENGTH_SHORT).show()
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.loginUser(
                                email,
                                password,
                                onSuccess = {
                                    val user = FirebaseAuth.getInstance().currentUser
                                    if (user != null && user.isEmailVerified) {
                                        if (stayLoggedIn) {
                                            StayLoggedInPreferences.save(context, true)
                                        }

                                        Toast.makeText(context, "✅ Είσοδος επιτυχής", Toast.LENGTH_SHORT).show()
                                        FCMTokenUtils.updateFCMToken()
                                        sessionViewModel.refreshUserStatus(withDelay = true)

                                        coroutineScope.launch {
                                            delay(300)
                                            navController.navigate(Screen.OffersHome.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "⚠️ Επιβεβαίωσε το email σου πρώτα.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        FirebaseAuth.getInstance().signOut()
                                    }
                                },
                                onFailure = { error ->
                                    Toast.makeText(
                                        context,
                                        "❌ Αποτυχία σύνδεσης: $error",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Συμπληρώστε email και κωδικό",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Δεν έχετε λογαριασμό;", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { navController.navigate(Screen.RegisterUser.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Εγγραφή Χρήστη")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { navController.navigate(Screen.RegisterShop.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Εγγραφή Καταστήματος")
                }
            }
        }
    }
}
