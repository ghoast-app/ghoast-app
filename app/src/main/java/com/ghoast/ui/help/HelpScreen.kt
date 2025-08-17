package com.ghoast.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
import com.ghoast.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    navController: NavHostController,
    fromMenu: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Βοήθεια & Συχνές Ερωτήσεις") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.OffersHome.route + "?fromMenu=true") {
                            popUpTo(0)
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val faqs = listOf(
                "Πώς μπορώ να προσθέσω μια προσφορά;" to "Αν είστε κατάστημα, πατήστε στο κουμπί '+' και συμπληρώστε τη φόρμα με τίτλο, περιγραφή, κατηγορία και εικόνες.",
                "Πώς λειτουργούν τα αγαπημένα;" to "Πατώντας το ❤️ μπορείτε να προσθέσετε προσφορές και καταστήματα στα αγαπημένα σας για εύκολη πρόσβαση.",
                "Τι είναι η προβολή στον χάρτη;" to "Μπορείτε να δείτε τις προσφορές ή τα καταστήματα πάνω σε έναν διαδραστικό χάρτη για να βρείτε κοντινά σημεία.",
                "Πώς λειτουργεί η αναζήτηση με απόσταση;" to "Η εφαρμογή χρησιμοποιεί την τοποθεσία σας για να εμφανίζει προσφορές εντός της απόστασης που έχετε επιλέξει.",
                "Ξέχασα τον κωδικό μου. Τι μπορώ να κάνω;" to "Στην οθόνη σύνδεσης πατήστε 'Ξεχάσατε τον κωδικό;' για να λάβετε email επαναφοράς."
            )
            faqs.forEach { (question, answer) ->
                ExpandableFAQ(question, answer)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableFAQ(
    question: String,
    answer: String
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = answer, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
