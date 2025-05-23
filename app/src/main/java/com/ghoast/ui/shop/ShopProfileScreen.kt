package com.ghoast.ui.shop

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.ghoast.ui.navigation.Screen
import com.ghoast.viewmodel.EditShopProfileViewModel

@Composable
fun ShopProfileScreen(
    navController: NavHostController,
    viewModel: EditShopProfileViewModel = viewModel()
) {
    val shop by viewModel.shop.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        Log.d("ShopProfileScreen", "🚀 Calling loadShopById from profile screen")
        viewModel.loadShopById(null)
    }

    if (shop == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Δεν βρέθηκαν στοιχεία καταστήματος")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        shop?.profilePhotoUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Προφίλ καταστήματος",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        Text("Όνομα: ${shop?.shopName}", style = MaterialTheme.typography.titleLarge)

        Text("Κατηγορίες:", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            shop?.categories?.forEach { category ->
                AssistChip(
                    onClick = {},
                    label = { Text(category) }
                )
            }
        }

        Text("Διεύθυνση: ${shop?.address}")
        Text("Τηλέφωνο: ${shop?.phone}")
        Text("Ιστοσελίδα: ${shop?.website}")
        Text("Email: ${shop?.email}")

        Text("Ώρες Λειτουργίας:", style = MaterialTheme.typography.titleMedium)
        shop?.workingHours?.forEach { wh ->
            if (wh.enabled) {
                Text("${wh.day}: ${wh.from} - ${wh.to}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate(Screen.EditShop.createRoute(shop?.id ?: ""))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("✏ Επεξεργασία Προφίλ")
        }

        Button(
            onClick = {
                navController.navigate(Screen.MyShops.route)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🏪 Διαχείριση Καταστημάτων")
        }
    }
}
