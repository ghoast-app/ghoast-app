package com.ghoast.ui.shop

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ghoast.model.Offer
import com.ghoast.ui.navigation.Screen
import com.ghoast.viewmodel.MyShopOffersViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghoast.ui.shop.OfferItem

@Composable
fun MyShopOffersScreen(navController: NavHostController) {
    val viewModel: MyShopOffersViewModel = viewModel()
    val offers by viewModel.offers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Κάτι πήγε στραβά.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(offers) { offer ->
                        OfferItem(
                            offer = offer,
                            onEditClick = {
                                navController.navigate(Screen.EditOffer.route + "/${offer.id}")
                            },
                            onDeleteClick = {
                                viewModel.deleteOffer(offer.id ?: "")
                            }
                        )
                    }
                    @Composable
                    fun OfferItem(
                        offer: Offer,
                        onEditClick: () -> Unit,
                        onDeleteClick: () -> Unit
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Τίτλος: ${offer.title}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Περιγραφή: ${offer.description}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Έκπτωση: ${offer.discount}%",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Κατηγορία: ${offer.category}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = onEditClick) {
                                        Text("✏ Επεξεργασία")
                                    }
                                    OutlinedButton(onClick = onDeleteClick) {
                                        Text("🗑 Διαγραφή")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}