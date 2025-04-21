package com.ghoast.ui.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ghoast.model.Offer
import java.text.DecimalFormat

@Composable
fun OfferCard(
    offer: Offer,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val distanceText = offer.distanceKm?.let {
        val formatter = DecimalFormat("#.#")
        "${formatter.format(it)} km"
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // 🔹 Εικόνα προσφοράς
            offer.imageUrls.firstOrNull()?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Offer Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 🔹 Πληροφορίες καταστήματος
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (offer.profilePhotoUri.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(offer.profilePhotoUri),
                        contentDescription = "Shop Profile Image",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(offer.shopName, fontWeight = FontWeight.Bold)
                    distanceText?.let {
                        Text("Απόσταση: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }


                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle Favorite"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = offer.title, style = MaterialTheme.typography.titleMedium)
            Text(text = offer.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(offer.category) }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "-${offer.discount}", color = Color.Red, fontWeight = FontWeight.Bold)
                    if (offer.isNew) Text("ΝΕΟ", color = Color.Green, fontWeight = FontWeight.Bold)
                    if (offer.endsSoon) Text("Λήγει σύντομα!", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
