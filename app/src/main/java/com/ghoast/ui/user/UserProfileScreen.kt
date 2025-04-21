package com.ghoast.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    sessionViewModel: UserSessionViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Ο χρήστης δεν είναι συνδεδεμένος.")
        }
        return
    }


    // Dummy state για γλώσσα
    var selectedLanguage by remember { mutableStateOf("Ελληνικά") }
    val languageOptions = listOf("Ελληνικά", "Αγγλικά")

    // Dummy state για Theme
    var darkMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Το Προφίλ μου") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("👤 Email: ${currentUser?.email ?: "Άγνωστο"}")
            Text("🆔 ID: ${currentUser?.uid ?: "Άγνωστο"}")

            Button(onClick = { /* Μπορούμε να προσθέσουμε fetch/update user data */ }) {
                Text("🔄 Ανανέωση Στοιχείων")
            }

            Divider()

            Text("⚙️ Ρυθμίσεις", style = MaterialTheme.typography.titleMedium)

            // Γλώσσα Dropdown
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                TextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    label = { Text("Γλώσσα") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Light/Dark Mode Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🌙 Dark Mode")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            Divider()

            Button(
                onClick = {
                    sessionViewModel.logout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Αποσύνδεση")
            }
        }
    }
}
