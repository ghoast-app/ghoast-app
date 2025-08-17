package com.ghoast.ui.billing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghoast.billing.BillingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferLimitExceededScreen(
    billingViewModel: BillingViewModel = viewModel(),
    fromMenu: Boolean = false,
    onCancel: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    if (fromMenu) {
        BackHandler { onCancel() }
    }

    Scaffold(
        topBar = {
            if (fromMenu) {
                TopAppBar(
                    title = { Text("Ξεπέρασες το όριο προσφορών!") },
                    navigationIcon = {
                        IconButton(onClick = { onCancel() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Ξεπέρασες το όριο προσφορών!") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Μπορείς να αγοράσεις μία επιπλέον προσφορά ή να αποκτήσεις συνδρομή για απεριόριστες!",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = billingViewModel.launchPayPerOfferFlow()
                        if (success) {
                            onPaymentSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔥 Προσθήκη προσφοράς με 0.99€")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = billingViewModel.launchSubscriptionFlow()
                        if (success) {
                            onPaymentSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚀 Απεριόριστες προσφορές με 4.99€/μήνα")
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = onCancel) {
                Text("❌ Άκυρο")
            }
        }
    }
}
