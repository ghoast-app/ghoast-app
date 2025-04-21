package com.ghoast.ui.contact

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghoast.viewmodel.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(viewModel: ContactViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val messageSent by viewModel.messageSent.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Επικοινωνία") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Όνομά σας") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email επικοινωνίας") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Μήνυμα") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && message.isNotBlank()) {
                        viewModel.sendMessage(name, email, message)
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Αποστολή")
            }
        }

        // ✅ Επιβεβαίωση με AlertDialog
        if (messageSent) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.resetMessageState()
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetMessageState()
                    }) {
                        Text("OK")
                    }
                },
                title = { Text("Μήνυμα στάλθηκε!") },
                text = { Text("Το μήνυμά σας αποθηκεύτηκε με επιτυχία.") }
            )
        }
    }
}
