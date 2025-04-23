package com.ghoast.ui.shop

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.model.Offer
import com.ghoast.ui.navigation.Screen
import com.ghoast.viewmodel.MyShopOffersViewModel
import androidx.compose.material.icons.filled.Add

@Composable
fun MyShopOffersScreen(navController: NavHostController) {
    val viewModel: MyShopOffersViewModel = viewModel()
    val offers by viewModel.offers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedOfferId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddNewShop.route)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Νέο Κατάστημα")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

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
                                    selectedOfferId = offer.id
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    if (showDeleteDialog && selectedOfferId != null) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Επιβεβαίωση διαγραφής") },
                            text = { Text("Είστε σίγουροι ότι θέλετε να διαγράψετε αυτή την προσφορά;") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteOffer(selectedOfferId!!) {
                                        Toast.makeText(context, "Η προσφορά διαγράφηκε", Toast.LENGTH_SHORT).show()
                                    }
                                    showDeleteDialog = false
                                }) {
                                    Text("Ναι")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Άκυρο")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
