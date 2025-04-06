package com.ghoast.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ghoast.model.Offer

@Composable
fun OfferCard(offer: Offer, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(offer.shopImageUrl),
                    contentDescription = "Shop Image",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(offer.shopName, fontWeight = FontWeight.Bold)
                    Text("Απόσταση: ${offer.distanceKm} km", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle Favorite"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(offer.title, style = MaterialTheme.typography.titleMedium)
            Text(offer.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                AssistChip(
                    onClick = {},
                    label = { Text(offer.category) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                if (offer.isNew) Text("ΝΕΟ", color = Color.Green, fontWeight = FontWeight.Bold)
                if (offer.endsSoon) Text("Λήγει σύντομα!", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}
