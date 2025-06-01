package com.ghoast.ui.session

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class UserType {
    USER, SHOP, UNKNOWN
}

class UserSessionViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userType = MutableStateFlow(UserType.UNKNOWN)
    val userType: StateFlow<UserType> = _userType

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        checkUserStatus()
    }

    fun refreshUserStatus(withDelay: Boolean = false) {
        viewModelScope.launch {
            if (withDelay) delay(1000)
            checkUserStatus()
        }
    }

    private fun checkUserStatus() {
        val user = auth.currentUser
        _isLoggedIn.value = user != null
        Log.d("SESSION_CHECK", "user = ${user?.uid}, isLoggedIn = ${_isLoggedIn.value}")

        user?.uid?.let { uid ->
            viewModelScope.launch {
                try {
                    // 🔄 Νέος ενοποιημένος έλεγχος
                    val shopSnapshot = db.collection("shops")
                        .whereEqualTo("ownerId", uid)
                        .limit(1)
                        .get()
                        .await()

                    if (!shopSnapshot.isEmpty) {
                        _userType.value = UserType.SHOP
                        Log.d("SESSION_CHECK", "✅ UserType set to SHOP")
                        return@launch
                    }

                    val userSnapshot = db.collection("users")
                        .document(uid)
                        .get()
                        .await()

                    if (userSnapshot.exists()) {
                        _userType.value = UserType.USER
                        Log.d("SESSION_CHECK", "✅ UserType set to USER")
                        saveFcmToken(uid)
                        return@launch
                    }

                    _userType.value = UserType.UNKNOWN
                    Log.d("SESSION_CHECK", "⛔ UserType set to UNKNOWN")
                } catch (e: Exception) {
                    Log.e("SESSION_CHECK", "❌ Exception: ${e.message}", e)
                    _userType.value = UserType.UNKNOWN
                }
            }
        }
    }

    private fun saveFcmToken(uid: String) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                db.collection("users").document(uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("FCM_TOKEN", "✅ Token updated for user $uid: $token")
                    }
                    .addOnFailureListener {
                        Log.e("FCM_TOKEN", "❌ Failed to update token", it)
                    }
            }
            .addOnFailureListener {
                Log.e("FCM_TOKEN", "❌ Failed to get token", it)
            }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _userType.value = UserType.UNKNOWN
    }
}
