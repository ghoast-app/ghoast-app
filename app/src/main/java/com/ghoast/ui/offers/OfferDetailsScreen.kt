package com.ghoast.ui.offers

import androidx.compose.foundation.Image
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.ghoast.model.Offer
import com.ghoast.model.Shop

@Composable
fun OfferDetailsScreen(
    navController: NavController,
    offerId: String // αυτό θα περάσουμε από τη λίστα/χάρτη
) {
    val context = LocalContext.current

    // Temporary placeholders (θα αντικατασταθούν με live Firestore δεδομένα)
    val dummyOffer = remember {
        Offer(
            id = offerId,
            title = "Τίτλος προσφοράς",
            description = "Αναλυτική περιγραφή προσφοράς με πληροφορίες και όρους.",
            discount = "40%",
            category = "Ανδρική ένδυση",
            imageUrls = listOf(
                "https://via.placeholder.com/400x200",
                "https://via.placeholder.com/400x200"
            ),
            shopId = "shop123"
        )
    }

    val dummyShop = remember {
        Shop(
            id = "shop123",
            shopName = "Κατάστημα Παράδειγμα",
            address = "Ερμού 1, Αθήνα",
            email = "info@example.com",
            phone = "2101234567",
            website = "https://example.com",
            profilePhotoUri = "https://via.placeholder.com/100",
            latitude = 37.9838,
            longitude = 23.7275
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 📸 Εικόνες προσφοράς
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dummyOffer.imageUrls.size) { index ->
                Image(
                    painter = rememberAsyncImagePainter(dummyOffer.imageUrls[index]),
                    contentDescription = null,
                    modifier = Modifier
                        .height(200.dp)
                        .width(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        // 🏷️ Τίτλος και Ποσοστό έκπτωσης
        Text(dummyOffer.title, style = MaterialTheme.typography.headlineSmall)
        Text("Έκπτωση: ${dummyOffer.discount}", style = MaterialTheme.typography.titleMedium)

        // 💬 Περιγραφή
        Text(dummyOffer.description, style = MaterialTheme.typography.bodyMedium)

        Divider()

        // 🏪 Πληροφορίες καταστήματος
        Text("Κατάστημα", style = MaterialTheme.typography.titleMedium)
        Text(dummyShop.shopName)
        Text(dummyShop.address)
        Text("📞 ${dummyShop.phone}")
        Text("📧 ${dummyShop.email}")
        Text("🌐 ${dummyShop.website}")
    }
}
