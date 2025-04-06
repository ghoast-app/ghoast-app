package com.ghoast.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.ui.session.UserType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersTopBar(
    navController: NavHostController,
    sessionViewModel: UserSessionViewModel,
    onMenuExpand: (Boolean) -> Unit,
    onShowHelp: () -> Unit,
    onShowContact: () -> Unit,
    menuExpanded: Boolean
) {
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()
    val userType by sessionViewModel.userType.collectAsState()

    TopAppBar(
        title = { Text("Προσφορές") },
        navigationIcon = {
            IconButton(onClick = { onMenuExpand(true) }) {
                Icon(Icons.Default.Menu, contentDescription = "Μενού")
            }
        },
        actions = {
            IconButton(onClick = {
                navController.navigate("offers_map")
            }) {
                Icon(Icons.Default.Place, contentDescription = "Χάρτης")
            }
        }
    )

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { onMenuExpand(false) }
    ) {
        if (isLoggedIn) {
            when (userType) {
                UserType.USER -> {
                    DropdownMenuItem(
                        text = { Text("Αγαπημένα Καταστήματα") },
                        onClick = {
                            navController.navigate("favorite_shops")
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Αγαπημένες Προσφορές") },
                        onClick = {
                            navController.navigate("favorite_offers")
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Το προφίλ μου") },
                        onClick = {
                            navController.navigate("user_profile")
                            onMenuExpand(false)
                        }
                    )
                }

                UserType.SHOP -> {
                    DropdownMenuItem(
                        text = { Text("➕ Προσθήκη Προσφοράς") },
                        onClick = {
                            navController.navigate("add_offer")
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Οι Προσφορές μου") },
                        onClick = {
                            navController.navigate("my_shop_offers")
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Προφίλ Καταστήματος") },
                        onClick = {
                            navController.navigate("shop_profile")
                            onMenuExpand(false)
                        }
                    )
                }

                else -> {}
            }

            DropdownMenuItem(
                text = { Text("Βοήθεια") },
                onClick = {
                    onShowHelp()
                    onMenuExpand(false)
                }
            )
            DropdownMenuItem(
                text = { Text("Επικοινωνία") },
                onClick = {
                    onShowContact()
                    onMenuExpand(false)
                }
            )
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    sessionViewModel.logout()
                    navController.navigate("offers_home") {
                        popUpTo("offers_home") { inclusive = true }
                    }
                    onMenuExpand(false)
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Login") },
                onClick = {
                    navController.navigate("login")
                    onMenuExpand(false)
                }
            )
            DropdownMenuItem(
                text = { Text("Βοήθεια") },
                onClick = {
                    onShowHelp()
                    onMenuExpand(false)
                }
            )
            DropdownMenuItem(
                text = { Text("Επικοινωνία") },
                onClick = {
                    onShowContact()
                    onMenuExpand(false)
                }
            )
        }
    }
}
