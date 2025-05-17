package com.ghoast.ui.shop

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.model.Shop
import com.ghoast.ui.navigation.Screen
import com.ghoast.viewmodel.MyShopsViewModel

@Composable
fun MyShopsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: MyShopsViewModel = viewModel()
    val shops by viewModel.shops.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedShopId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Σφάλμα κατά την φόρτωση.",
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
                    items(shops) { shop ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(Screen.EditShop.route + "/${shop.id}")
                                },
                            elevation = CardDefaults.elevatedCardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(shop.shopName.ifBlank { "(Χωρίς Όνομα)" }, style = MaterialTheme.typography.titleMedium)
                                Text(shop.address.ifBlank { "(Χωρίς Διεύθυνση)" }, style = MaterialTheme.typography.bodySmall)

                                if (shop.categories.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        shop.categories.forEach { category ->
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(category) }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(onClick = {
                                        navController.navigate(Screen.EditShop.route + "/${shop.id}")
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Επεξεργασία")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = {
                                        selectedShopId = shop.id
                                        showDeleteDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Διαγραφή", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                if (showDeleteDialog && selectedShopId != null) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Επιβεβαίωση Διαγραφής") },
                        text = { Text("Θέλετε σίγουρα να διαγράψετε αυτό το κατάστημα;") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteShop(selectedShopId!!) {
                                    Toast.makeText(context, "Το κατάστημα διαγράφηκε", Toast.LENGTH_SHORT).show()
                                    showDeleteDialog = false
                                }
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
