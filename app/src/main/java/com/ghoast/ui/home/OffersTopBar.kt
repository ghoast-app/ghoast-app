package com.ghoast.ui.home

import android.util.Log
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.ui.session.UserType
import com.ghoast.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersTopBar(
    navController: NavHostController,
    sessionViewModel: UserSessionViewModel,
    onMenuExpand: (Boolean) -> Unit,
    onShowHelp: () -> Unit,
    onShowContact: () -> Unit,
    menuExpanded: Boolean,
    extraActions: @Composable RowScope.() -> Unit = {}
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
            extraActions()
            IconButton(onClick = {
                navController.navigate(Screen.OffersMap.route)
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
                            navController.navigate(Screen.FavoriteShops.route)
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Αγαπημένες Προσφορές") },
                        onClick = {
                            navController.navigate(Screen.FavoriteOffers.route)
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Λίστα Καταστημάτων") },
                        onClick = {
                            navController.navigate("all_shops")
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Το προφίλ μου") },
                        onClick = {
                            navController.navigate(Screen.UserProfile.route)
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🔔 Ειδοποιήσεις") },
                        onClick = {
                            navController.navigate("notifications")
                            onMenuExpand(false)
                        }
                    )
                }

                UserType.SHOP -> {
                    DropdownMenuItem(
                        text = { Text("➕ Προσθήκη Προσφοράς") },
                        onClick = {
                            navController.navigate(Screen.AddOffer.route)
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("➕ Νέο Κατάστημα") },
                        onClick = {
                            navController.navigate(Screen.AddNewShop.route)
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Οι Προσφορές μου") },
                        onClick = {
                            navController.navigate(Screen.MyShopOffers.route)
                            onMenuExpand(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Προφίλ Καταστήματος") },
                        onClick = {
                            navController.navigate(Screen.ShopProfile.route)
                            onMenuExpand(false)
                        }
                    )
                }

                UserType.UNKNOWN -> {
                    DropdownMenuItem(
                        text = { Text("Login") },
                        onClick = {
                            navController.navigate(Screen.Login.route)
                            onMenuExpand(false)
                        }
                    )
                }
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
                    navController.navigate(Screen.OffersHome.route) {
                        popUpTo(Screen.OffersHome.route) { inclusive = true }
                    }
                    onMenuExpand(false)
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Login") },
                onClick = {
                    navController.navigate(Screen.Login.route)
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
