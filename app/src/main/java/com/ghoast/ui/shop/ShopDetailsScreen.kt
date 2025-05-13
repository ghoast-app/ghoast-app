package com.ghoast.ui.shop

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.ghoast.model.Shop
import com.ghoast.ui.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailsScreen(
    shopId: String,
    navController: NavController
) {
    val context = LocalContext.current
    var shop by remember { mutableStateOf<Shop?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(shopId) {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("shops")
            .document(shopId)
            .get()
            .await()

        val shopData = snapshot.toObject(Shop::class.java)?.copy(id = snapshot.id)

        val fallbackCategories: List<String> = if (shopData?.categories.isNullOrEmpty()) {
            val legacy = snapshot.getString("category")
            legacy?.split(",")?.map { it.trim() } ?: emptyList()
        } else {
            shopData?.categories ?: emptyList()
        }

        val finalShop = shopData?.copy(categories = fallbackCategories)
        shop = finalShop
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(shop?.shopName ?: "Κατάστημα") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                shop?.let { shopData ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        shopData.profilePhotoUri.takeIf { it.isNotBlank() }?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(shopData.shopName, style = MaterialTheme.typography.headlineSmall)

                        if (shopData.categories.isNotEmpty()) {
                            Text("\ud83d\udccc Κατηγορίες", style = MaterialTheme.typography.titleMedium)

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                shopData.categories.forEach { category ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(category) }
                                    )
                                }
                            }
                        }

                        Text(
                            text = shopData.address,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${shopData.latitude},${shopData.longitude}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                context.startActivity(intent)
                            }
                        )

                        Text(
                            text = "\ud83d\udcde ${shopData.phone}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${shopData.phone}")
                                }
                                context.startActivity(intent)
                            }
                        )

                        Text(
                            text = "\ud83d\udce7 ${shopData.email}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${shopData.email}")
                                }
                                context.startActivity(intent)
                            }
                        )

                        Text(
                            text = "\ud83c\udf10 ${shopData.website}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(shopData.website))
                                context.startActivity(intent)
                            }
                        )

                        if (shopData.workingHours.isNotEmpty()) {
                            Text("\ud83d\udd52 \u038f\u03c1\u03b5\u03c2 \u03bb\u03b5\u03b9\u03c4\u03bf\u03c5\u03c1\u03b3\u03af\u03b1\u03c2", style = MaterialTheme.typography.titleMedium)
                            shopData.workingHours.forEach {
                                Text("\u2022 ${it.day}: ${it.from ?: "-"} - ${it.to ?: "-"}")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                navController.navigate(Screen.ShopOffers.createRoute(shopId))
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("\u0394\u03b5\u03c2 \u03cc\u03bb\u03b5\u03c2 \u03c4\u03b9\u03c2 \u03c0\u03c1\u03bf\u03c3\u03c6\u03bf\u03c1\u03ad\u03c2")
                        }
                    }
                } ?: Text("\u0394\u03b5\u03bd \u03b2\u03c1\u03ad\u03b8\u03b7\u03ba\u03b5 \u03c4\u03bf \u03ba\u03b1\u03c4\u03ac\u03c3\u03c4\u03b7\u03bc\u03b1")
            }
        }
    }
}
