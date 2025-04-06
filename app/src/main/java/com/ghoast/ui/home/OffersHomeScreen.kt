package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.navigation.Screen
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.ui.session.UserType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersHomeScreen(navController: NavHostController) {
    val viewModel: OffersViewModel = viewModel()
    val sessionViewModel: UserSessionViewModel = viewModel()
    val offers = viewModel.offers.collectAsState().value
    val userType by sessionViewModel.userType.collectAsState()

    var favorites by remember { mutableStateOf(setOf<String>()) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    val isLoggedIn = sessionViewModel.isLoggedIn.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.fetchOffers()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Προσφορές") },
            navigationIcon = {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Μενού")
                }
            },
            actions = {
                IconButton(onClick = {
                    navController.navigate(Screen.OffersMap.route)
                }) {
                    Icon(Icons.Default.Place, contentDescription = "Προβολή Χάρτη")
                }
            }
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            if (isLoggedIn) {
                when (userType) {
                    UserType.USER -> {
                        DropdownMenuItem(
                            text = { Text("Αγαπημένα Καταστήματα") },
                            onClick = {
                                navController.navigate("favorite_shops")
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Αγαπημένες Προσφορές") },
                            onClick = {
                                navController.navigate("favorite_offers")
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Το προφίλ μου") },
                            onClick = {
                                navController.navigate("user_profile")
                                menuExpanded = false
                            }
                        )
                    }

                    UserType.SHOP -> {
                        DropdownMenuItem(
                            text = { Text("➕ Προσθήκη Προσφοράς") },
                            onClick = {
                                navController.navigate("add_offer")
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Οι Προσφορές μου") },
                            onClick = {
                                navController.navigate("my_shop_offers")
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Προφίλ Καταστήματος") },
                            onClick = {
                                navController.navigate("shop_profile")
                                menuExpanded = false
                            }
                        )
                    }

                    else -> {}
                }

                DropdownMenuItem(
                    text = { Text("Βοήθεια") },
                    onClick = {
                        showHelpDialog = true
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Επικοινωνία") },
                    onClick = {
                        showContactDialog = true
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = {
                        sessionViewModel.logout()
                        navController.navigate("offers_home") {
                            popUpTo("offers_home") { inclusive = true }
                        }
                        menuExpanded = false
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Login") },
                    onClick = {
                        navController.navigate("login")
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Βοήθεια") },
                    onClick = {
                        showHelpDialog = true
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Επικοινωνία") },
                    onClick = {
                        showContactDialog = true
                        menuExpanded = false
                    }
                )
            }
        }

        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("🛍️ Οδηγίες Χρήσης") },
                text = {
                    Text(
                        "• Αναζήτησε προσφορές από τοπικά καταστήματα.\n" +
                                "• Κάνε login για να αποθηκεύεις αγαπημένα.\n" +
                                "• Δες προσφορές σε λίστα ή χάρτη.\n" +
                                "• Τα καταστήματα μπορούν να κάνουν εγγραφή και να προσθέτουν προσφορές."
                    )
                }
            )
        }

        if (showContactDialog) {
            AlertDialog(
                onDismissRequest = { showContactDialog = false },
                confirmButton = {
                    TextButton(onClick = { showContactDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("📞 Επικοινωνία") },
                text = {
                    Text(
                        "• Email: support@ghoastapp.com\n" +
                                "• Τηλέφωνο: +30 210 1234567\n" +
                                "• Ώρες: Δευ–Παρ, 10:00–17:00"
                    )
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(offers) { offer ->
                OfferCard(
                    offer = offer,
                    isFavorite = favorites.contains(offer.id),
                    onToggleFavorite = {
                        // Προσθήκη / Αφαίρεση αγαπημένου
                    }
                )
            }
        }
    }
}
