package com.ghoast

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ghoast.ui.navigation.GhoastNavGraph
import com.ghoast.ui.theme.GhoastTheme
import com.ghoast.utils.FCMTokenUtils
import com.ghoast.viewmodel.UserTypeViewModel
import com.google.android.gms.maps.MapsInitializer
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Αυτόματη ενημέρωση FCM token με το άνοιγμα της εφαρμογής
        FCMTokenUtils.updateFCMToken()

        // 🔑 Αρχικοποίηση Google Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyAXSalPhyJ4QID_yqQ3Iu2Zanhy8spZPHQ")
        }
        Log.d("GhoastDebug", "My UID is: ${FirebaseAuth.getInstance().currentUser?.uid}")

        setContent {
            GhoastTheme {
                val navController = rememberNavController()
                val userTypeViewModel: UserTypeViewModel = viewModel()
                Surface(modifier = Modifier.fillMaxSize()) {
                    GhoastNavGraph(
                        navController = navController,
                        userTypeViewModel = userTypeViewModel
                    )
                }
            }
        }
    }
}
