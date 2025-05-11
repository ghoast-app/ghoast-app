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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.navigation.Screen
import com.ghoast.viewmodel.MyShopOffersViewModel
import com.ghoast.model.Shop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyShopOffersScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: MyShopOffersViewModel = viewModel()
    val offers by viewModel.offers.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedShop by remember { mutableStateOf<Shop?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedOfferId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(shops) {
        if (selectedShop == null && shops.isNotEmpty()) {
            selectedShop = shops.first()
            viewModel.loadOffersForShop(shops.first().id ?: "")
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedShop?.shopName ?: "Επιλογή καταστήματος",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text("Κατάστημα") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    shops.forEach { shop ->
                        DropdownMenuItem(
                            text = { Text(shop.shopName ?: "(Χωρίς Όνομα)") },
                            onClick = {
                                selectedShop = shop
                                expanded = false
                                shop.id?.let { viewModel.loadOffersForShop(it) }
                            }
                        )
                    }
                }
            }

            selectedShop?.let { shop ->
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        navController.navigate(Screen.EditShop.createRoute(shop.id ?: ""))
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("✏ Επεξεργασία Καταστήματος")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Σφάλμα.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                offers.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📭", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Δεν έχετε προσφορές για αυτό το κατάστημα.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(offers) { offer ->
                            OfferItem(
                                offer = offer,
                                onEditClick = {
                                    navController.navigate(Screen.EditOffer.createRoute(offer.id ?: ""))
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
                            text = { Text("Διαγραφή της προσφοράς;") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteOffer(selectedOfferId!!) {
                                        Toast.makeText(context, "Διαγράφηκε!", Toast.LENGTH_SHORT).show()
                                        selectedShop?.id?.let { viewModel.loadOffersForShop(it) }
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
