package com.ghoast.ui.offers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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
        val currentShop = shop!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🔄 Hint πάνω από τις εικόνες
            if (offer!!.imageUrls.size > 1) {
                Text(
                    text = "➡️ Σύρε δεξιά για να δεις όλες τις εικόνες",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 📸 Εικόνες προσφοράς
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(offer!!.imageUrls) { _, imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .height(200.dp)
                            .width(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Text(offer!!.title, style = MaterialTheme.typography.headlineSmall)
            Text("Έκπτωση: ${offer!!.discount}", style = MaterialTheme.typography.titleMedium)
            Text(offer!!.description, style = MaterialTheme.typography.bodyMedium)

            Text("Κατηγορία: ${offer!!.category}", style = MaterialTheme.typography.bodySmall)

            Text("Κατάστημα", style = MaterialTheme.typography.titleMedium)
            Text(currentShop.shopName)

            // ✅ Κατηγορίες Καταστήματος ως Chips
            if (!currentShop.categories.isNullOrEmpty()) {
                Text("Κατηγορίες:", style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentShop.categories.forEach { cat ->
                        AssistChip(
                            onClick = {},
                            label = { Text(cat) }
                        )
                    }
                }
            }

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

            Text(
                text = "🌐 ${currentShop.website}",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentShop.website))
                    context.startActivity(intent)
                }
            )

            if (currentShop.workingHours.isNotEmpty()) {
                Text("🕒 Ώρες λειτουργίας", style = MaterialTheme.typography.titleMedium)
                currentShop.workingHours.forEach {
                    Text("• ${it.day}: ${it.from ?: "-"} - ${it.to ?: "-"}")
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Η προσφορά δεν βρέθηκε.")
        }
    }
}
