package com.ghoast.ui.shop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyShopOffersScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📜 Οι Προσφορές μου") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Εδώ θα εμφανίζονται όλες οι προσφορές του καταστήματος.")
        }
    }
}
