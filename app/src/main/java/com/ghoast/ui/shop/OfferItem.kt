package com.ghoast.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ghoast.model.Offer

@Composable
fun OfferItem(
    offer: Offer,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = offer.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Περιγραφή: ${offer.description}")
            Text(text = "Έκπτωση: ${offer.discount}")
            Text(text = "Κατηγορία: ${offer.category}")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onEditClick) {
                    Text("Επεξεργασία")
                }
                OutlinedButton(onClick = onDeleteClick, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                    Text("Διαγραφή")
                }
            }
        }
    }
}
