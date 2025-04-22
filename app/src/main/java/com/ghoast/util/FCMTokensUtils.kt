package com.ghoast.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FCMTokenUtils {

    fun updateFCMToken() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            db.collection("users").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCMToken", "✅ Token updated successfully: $token")
                }
                .addOnFailureListener { e ->
                    Log.e("FCMToken", "❌ Failed to update token", e)
                }
        }
    }
}
