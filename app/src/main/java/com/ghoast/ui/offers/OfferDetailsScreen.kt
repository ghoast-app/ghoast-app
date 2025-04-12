package com.ghoast.ui.offers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.ghoast.viewmodel.OfferDetailsViewModel

@Composable
fun OfferDetailsScreen(
    navController: NavController,
    offerId: String
) {
    val context = LocalContext.current
    val viewModel: OfferDetailsViewModel = viewModel()

    val offer by viewModel.offerState
    val shop by viewModel.shopState
    val isLoading by viewModel.isLoading

    LaunchedEffect(offerId) {
        viewModel.loadOfferAndShop(offerId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (offer != null && shop != null) {

        val currentShop = shop!! // ✅ ασφαλής χρήση

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 📸 Εικόνες προσφοράς
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(offer!!.imageUrls.size) { index ->
                    Image(
                        painter = rememberAsyncImagePainter(offer!!.imageUrls[index]),
                        contentDescription = null,
                        modifier = Modifier
                            .height(200.dp)
                            .width(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            // 🏷️ Τίτλος και Ποσοστό έκπτωσης
            Text(offer!!.title, style = MaterialTheme.typography.headlineSmall)
            Text("Έκπτωση: ${offer!!.discount}", style = MaterialTheme.typography.titleMedium)

            // 💬 Περιγραφή
            Text(offer!!.description, style = MaterialTheme.typography.bodyMedium)

            Divider()

            // 🏪 Πληροφορίες καταστήματος
            Text("Κατάστημα", style = MaterialTheme.typography.titleMedium)

            Text(currentShop.shopName)

            // 📍 Διεύθυνση → Google Maps
            Text(
                text = currentShop.address,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${currentShop.latitude},${currentShop.longitude}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    context.startActivity(intent)
                }
            )

            // ☎ Τηλέφωνο → Κλήση
            Text(
                text = "📞 ${currentShop.phone}",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${currentShop.phone}")
                    }
                    context.startActivity(intent)
                }
            )

            // 📧 Email → Αποστολή
            Text(
                text = "📧 ${currentShop.email}",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${currentShop.email}")
                    }
                    context.startActivity(intent)
                }
            )

            // 🌐 Website → Άνοιγμα browser
            Text(
                text = "🌐 ${currentShop.website}",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentShop.website))
                    context.startActivity(intent)
                }
            )

            // 🕒 Ώρες λειτουργίας
            Text("🕒 Ώρες: ${currentShop.workingHours}")
        }

    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Η προσφορά δεν βρέθηκε.")
        }
    }
}
