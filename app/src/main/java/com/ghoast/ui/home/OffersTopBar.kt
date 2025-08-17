@file:OptIn(ExperimentalMaterial3Api::class)

package com.ghoast.ui.home

import androidx.compose.foundation.layout.*
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
import com.ghoast.viewmodel.UserType
import com.ghoast.viewmodel.UserTypeViewModel

@Composable
fun OffersTopBar(
    navController: NavHostController,
    onMenuExpand: (Boolean) -> Unit,
    onShowHelp: () -> Unit,
    onShowContact: () -> Unit,
    menuExpanded: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    extraActions: @Composable RowScope.() -> Unit = {}
) {
    val userTypeViewModel: UserTypeViewModel = viewModel()
    val userType by userTypeViewModel.userType.collectAsState()
    val isLoading by userTypeViewModel.isLoading.collectAsState()

    Column {
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Αναζήτηση προσφορών...") },
                    singleLine = true
                )
            },
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

        if (!isLoading) {
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuExpand(false) }
            ) {
                when (userType) {
                    UserType.USER -> {
                        DropdownMenuItem(
                            text = { Text("Αγαπημένα Καταστήματα") },
                            onClick = {
                                navController.navigate(Screen.FavoriteShops.route + "?fromMenu=true")
                                onMenuExpand(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Αγαπημένες Προσφορές") },
                            onClick = {
                                navController.navigate(Screen.FavoriteOffers.route + "?fromMenu=true")
                                onMenuExpand(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Λίστα Καταστημάτων") },
                            onClick = {
                                navController.navigate("all_shops?fromMenu=true")
                                onMenuExpand(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Το προφίλ μου") },
                            onClick = {
                                navController.navigate(Screen.UserProfile.route + "?fromMenu=true")
                                onMenuExpand(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🔔 Ειδοποιήσεις") },
                            onClick = {
                                navController.navigate("notifications?fromMenu=true")
                                onMenuExpand(false)
                            }
                        )
                    }

                    UserType.SHOP -> {
                        DropdownMenuItem(
                            text = { Text("➕ Προσθήκη Προσφοράς") },
                            onClick = {
                                navController.navigate(Screen.AddOffer.route + "?fromMenu=true")
                                onMenuExpand(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("➕ Νέο Κατάστημα") },
                            onClick = {
                                navController.navigate(Screen.AddNewShop.route + "?fromMenu=true")
                                onMenuExpand(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Οι Προσφορές μου") },
                            onClick = {
                                navController.navigate(Screen.MyShopOffers.route + "?fromMenu=true")
                                onMenuExpand(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Προφίλ Καταστήματος") },
                            onClick = {
                                navController.navigate(Screen.ShopProfile.route + "?fromMenu=true")
                                onMenuExpand(false)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("💳 Αγόρασε Προσφορά ή Συνδρομή") },
                            onClick = {
                                navController.navigate(Screen.OfferLimitExceeded.route + "?fromMenu=true")
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

                DropdownMenuItem(text = { Text("Βοήθεια") }, onClick = {
                    onShowHelp()
                    onMenuExpand(false)
                })
                DropdownMenuItem(text = { Text("Επικοινωνία") }, onClick = {
                    onShowContact()
                    onMenuExpand(false)
                })
                if (userType != UserType.UNKNOWN) {
                    DropdownMenuItem(text = { Text("Logout") }, onClick = {
                        userTypeViewModel.logout {
                            navController.navigate(Screen.OffersHome.route) {
                                popUpTo(Screen.OffersHome.route) { inclusive = true }
                            }
                        }
                        onMenuExpand(false)
                    })
                }
            }
        }
    }
}
