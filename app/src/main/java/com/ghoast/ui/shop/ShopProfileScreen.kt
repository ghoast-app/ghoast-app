package com.ghoast.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.ghoast.ui.navigation.Screen
import com.ghoast.viewmodel.ShopProfileViewModel

@Composable
fun ShopProfileScreen(
    navController: NavHostController,
    viewModel: ShopProfileViewModel = viewModel()
) {
    val shop by viewModel.shop.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadShopData()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
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
        Text("Κατηγορία: ${shop?.categories}")
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
                navController.navigate(Screen.EditShopProfile.route)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("✏ Επεξεργασία Προφίλ")
        }
    }
}
