package com.ghoast

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ghoast.ui.navigation.GhoastNavGraph
import com.ghoast.ui.session.UserSessionViewModel
import com.ghoast.ui.theme.GhoastTheme
import com.ghoast.utils.FCMTokenUtils
import com.ghoast.utils.StayLoggedInPreferences
import com.ghoast.viewmodel.UserTypeViewModel
import com.google.android.gms.maps.MapsInitializer
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Ενημέρωση FCM token
        FCMTokenUtils.updateFCMToken()

        // 🔑 Αρχικοποίηση Google Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyAXSalPhyJ4QID_yqQ3Iu2Zanhy8spZPHQ")
        }

        Log.d("GhoastDebug", "My UID is: ${FirebaseAuth.getInstance().currentUser?.uid}")

        setContent {
            GhoastTheme {
                val navController = rememberNavController()
                val sessionViewModel: UserSessionViewModel = viewModel()
                val userTypeViewModel: UserTypeViewModel = viewModel()

                // ✅ Έλεγχος stayLoggedIn + redirect
                LaunchedEffect(Unit) {
                    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
                    val stayLoggedIn = StayLoggedInPreferences.loadStayLoggedIn(applicationContext)

                    if (isLoggedIn && stayLoggedIn) {
                        navController.navigate("offers_home") {
                            popUpTo(0)
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    GhoastNavGraph(
                        navController = navController,
                        sessionViewModel = sessionViewModel
                    )
                }
            }
        }
    }
}
