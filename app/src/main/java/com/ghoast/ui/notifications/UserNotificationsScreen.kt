package com.ghoast.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghoast.ui.components.LoadingSpinner
import com.ghoast.ui.notifications.viewmodel.NotificationsViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNotificationsScreen() {
    val viewModel: NotificationsViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📬 Οι Ειδοποιήσεις σου") },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Να διαγραφούν όλες οι ειδοποιήσεις;",
                                    actionLabel = "ΝΑΙ"
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.clearAllNotifications()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Διαγραφή Όλων")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (isLoading) {
                LoadingSpinner()
            } else if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔔", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Δεν υπάρχουν ειδοποιήσεις αυτή τη στιγμή.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notifications) { notif ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(notif.title, style = MaterialTheme.typography.titleMedium)
                                    Text(notif.body, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                            .format(java.util.Date(notif.timestamp)),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        viewModel.deleteNotification(notif.id)
                                        snackbarHostState.showSnackbar("Η ειδοποίηση διαγράφηκε.")
                                    }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Διαγραφή")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
