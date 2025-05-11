package com.ghoast.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.ghoast.viewmodel.FavoritesViewModel
import com.ghoast.viewmodel.UserType
import com.ghoast.viewmodel.UserTypeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userTypeViewModel: UserTypeViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userType by userTypeViewModel.userType.collectAsState()

    if (currentUser == null || userType == UserType.UNKNOWN) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Ο χρήστης δεν είναι συνδεδεμένος.")
        }
        return
    }

    var selectedLanguage by remember { mutableStateOf("Ελληνικά") }
    val languageOptions = listOf("Ελληνικά", "Αγγλικά")

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
            Text("👤 Email: ${currentUser.email ?: "Άγνωστο"}")
            Text("🆔 ID: ${currentUser.uid ?: "Άγνωστο"}")

            Button(onClick = { /* Προσθήκη fetch/update user data αν χρειαστεί */ }) {
                Text("🔄 Ανανέωση Στοιχείων")
            }

            Divider()

            Text("⚙️ Ρυθμίσεις", style = MaterialTheme.typography.titleMedium)

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
                    userTypeViewModel.logout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Αποσύνδεση")
            }
        }
    }
}
