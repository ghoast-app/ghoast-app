package com.ghoast.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteOffersScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Αγαπημένες Προσφορές") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Εδώ θα εμφανίζονται οι αγαπημένες προσφορές του χρήστη.")
        }
    }
}
