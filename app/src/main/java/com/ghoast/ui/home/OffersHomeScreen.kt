package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ghoast.ui.session.UserSessionViewModel

@Composable
fun OffersHomeScreen(navController: NavHostController) {
    val viewModel: OffersViewModel = viewModel()
    val sessionViewModel: UserSessionViewModel = viewModel()

    val offers by viewModel.filteredOffers.collectAsState()
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedDistance by remember { mutableStateOf<Int?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(setOf<String>()) }

    // 🔄 Φόρτωσε προσφορές με βάση τα φίλτρα
    LaunchedEffect(selectedCategory, selectedDistance) {
        viewModel.fetchOffers(selectedCategory, selectedDistance)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        OffersTopBar(
            navController = navController,
            sessionViewModel = sessionViewModel,
            onMenuExpand = { menuExpanded = it },
            onShowHelp = { showHelpDialog = true },
            onShowContact = { showContactDialog = true },
            menuExpanded = menuExpanded
        )

        OffersFiltersSection(
            selectedCategory = selectedCategory,
            selectedDistance = selectedDistance,
            onCategoryChange = { selectedCategory = it },
            onDistanceChange = { selectedDistance = it }
        )

        OffersListSection(
            offers = offers,
            favorites = favorites,
            onToggleFavorite = { offerId ->
                favorites = if (favorites.contains(offerId)) {
                    favorites - offerId
                } else {
                    favorites + offerId
                }
            }
        )
    }

    // 💬 Help Dialog
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

    // ☎️ Contact Dialog
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
}
