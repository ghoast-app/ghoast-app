package com.ghoast.ui.billing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubscriptionOptionsDialog(
    onMonthlySelected: () -> Unit,
    onYearlySelected: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "🔥 Επίλεξε Συνδρομή για Απεριόριστες Προσφορές",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SubscriptionOption(
                    title = "🚀 Μηνιαία Συνδρομή",
                    description = "4.99€/μήνα",
                    onClick = onMonthlySelected
                )
                SubscriptionOption(
                    title = "🏆 Ετήσια Συνδρομή (Best Value)",
                    description = "49.99€/έτος (~4.16€/μήνα)",
                    onClick = onYearlySelected
                )
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onCancel) {
                Text("❌ Άκυρο")
            }
        }
    )
}

@Composable
private fun SubscriptionOption(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 14.sp)
        }
    }
}
