package com.ghoast.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ✅ Enum για ασφαλέστερη αναπαράσταση τύπου χρήστη
enum class UserType {
    USER, SHOP, UNKNOWN, User, Shop, Unknown
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

        user?.uid?.let { uid ->
            viewModelScope.launch {
                // Ελεγχος users
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            _userType.value = UserType.USER
                        } else {
                            // Ελεγχος shops
                            db.collection("shops").document(uid).get()
                                .addOnSuccessListener { shopDoc ->
                                    if (shopDoc.exists()) {
                                        _userType.value = UserType.SHOP
                                    } else {
                                        _userType.value = UserType.UNKNOWN
                                    }
                                }
                        }
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
