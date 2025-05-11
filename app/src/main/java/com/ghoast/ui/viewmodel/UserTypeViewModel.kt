package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class UserType {
    USER,
    SHOP,
    UNKNOWN
}

class UserTypeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userType = MutableStateFlow(UserType.UNKNOWN)
    val userType: StateFlow<UserType> = _userType

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        determineUserType()
    }

    fun determineUserType() {
        viewModelScope.launch {
            _isLoading.value = true
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _userType.value = UserType.UNKNOWN
                _isLoading.value = false
                return@launch
            }

            try {
                val shopSnapshot = db.collection("shops")
                    .whereEqualTo("ownerId", uid)
                    .limit(1)
                    .get()
                    .await()

                _userType.value = if (!shopSnapshot.isEmpty) {
                    UserType.SHOP
                } else {
                    UserType.USER
                }
            } catch (e: Exception) {
                _userType.value = UserType.UNKNOWN
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetUserType() {
        _userType.value = UserType.UNKNOWN
        _isLoading.value = true
    }

    fun logout(onComplete: () -> Unit = {}) {
        auth.signOut()
        _userType.value = UserType.UNKNOWN
        _isLoading.value = false
        onComplete()
    }
}
