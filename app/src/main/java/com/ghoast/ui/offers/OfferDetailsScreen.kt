package com.ghoast.ui.offers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.ui.text.style.TextDecoration

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Slide hint if multiple images
            if (offer!!.imageUrls.size > 1) {
                Text(
                    text = "➡️ Σύρε δεξιά για να δεις όλες τις εικόνες",
                    style = MaterialTheme.typography.bodySmall
                )
            }

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

            Text("Προσφορά", style = MaterialTheme.typography.titleMedium)
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(offer!!.title, style = MaterialTheme.typography.headlineSmall)
                    Text("Έκπτωση: ${offer!!.discount}", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.error))
                    Text(offer!!.description, style = MaterialTheme.typography.bodyMedium)
                    Text("Κατηγορία: ${offer!!.category}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Text("Πληροφορίες Καταστήματος", style = MaterialTheme.typography.titleMedium)
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(currentShop.profilePhotoUri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currentShop.shopName, style = MaterialTheme.typography.titleMedium)
                    }

                    if (currentShop.categories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentShop.categories.forEach { cat ->
                                AssistChip(onClick = {}, label = { Text(cat) })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${currentShop.latitude},${currentShop.longitude}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currentShop.address, textDecoration = TextDecoration.Underline)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${currentShop.phone}")
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currentShop.phone, textDecoration = TextDecoration.Underline)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${currentShop.email}")
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currentShop.email, textDecoration = TextDecoration.Underline)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentShop.website))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Public, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currentShop.website, textDecoration = TextDecoration.Underline)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🕒 Ώρες λειτουργίας", style = MaterialTheme.typography.titleMedium)
                    currentShop.workingHours.forEach {
                        Text("• ${it.day}: ${it.from ?: "-"} - ${it.to ?: "-"}")
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Η προσφορά δεν βρέθηκε.")
        }
    }
}
