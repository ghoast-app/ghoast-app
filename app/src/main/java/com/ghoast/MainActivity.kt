package com.ghoast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ghoast.ui.navigation.GhoastNavGraph
import com.ghoast.ui.theme.GhoastTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔑 Μην ξεχάσεις να βάλεις το κανονικό API key εδώ!
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCywkXm5g4z37zkJu0grCNfx_pagNokJaE")
        }

        setContent {
            GhoastTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize()) {
                    GhoastNavGraph(navController = navController)
                }
            }
        }
    }
}
