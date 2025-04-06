package com.ghoast.ui.session

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ✅ Σωστό Enum
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

    private fun checkUserStatus() {
        val user = auth.currentUser
        _isLoggedIn.value = user != null
        Log.d("SESSION_CHECK", "user = ${user?.uid}, isLoggedIn = ${_isLoggedIn.value}")

        user?.uid?.let { uid ->
            viewModelScope.launch {
                try {
                    val userDoc = db.collection("users").document(uid).get().await()
                    Log.d("SESSION_CHECK", "userDoc.exists = ${userDoc.exists()}")
                    if (userDoc.exists()) {
                        _userType.value = UserType.USER
                        Log.d("SESSION_CHECK", "✅ UserType set to USER")
                        return@launch
                    }

                    val shopDoc = db.collection("shops").document(uid).get().await()
                    Log.d("SESSION_CHECK", "shopDoc.exists = ${shopDoc.exists()}")
                    if (shopDoc.exists()) {
                        _userType.value = UserType.SHOP
                        Log.d("SESSION_CHECK", "✅ UserType set to SHOP")
                        return@launch
                    }

                    _userType.value = UserType.UNKNOWN
                    Log.d("SESSION_CHECK", "⛔ UserType set to UNKNOWN (not found)")
                } catch (e: Exception) {
                    Log.e("SESSION_CHECK", "❌ Exception: ${e.message}", e)
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _userType.value = UserType.UNKNOWN
    }
}
