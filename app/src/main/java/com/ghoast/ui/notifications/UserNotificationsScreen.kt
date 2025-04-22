package com.ghoast.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghoast.ui.components.LoadingSpinner
import com.ghoast.ui.notifications.viewmodel.NotificationsViewModel

@Composable
fun UserNotificationsScreen() {
    val viewModel: NotificationsViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("📬 Οι Ειδοποιήσεις σου", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            LoadingSpinner()
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Δεν υπάρχουν ειδοποιήσεις.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(notif.title, style = MaterialTheme.typography.titleMedium)
                            Text(notif.body, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(notif.timestamp)),
                                style = MaterialTheme.typography.labelSmall
                            )

                        }
                    }
                }
            }
        }
    }
}
