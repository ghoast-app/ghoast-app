package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadShopProfile()
    }

    private fun loadShopProfile() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("shops").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val shopData = document.toObject(Shop::class.java)
                _shop.value = shopData
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }
    fun loadShopData() {
        loadShopProfile()
    }
}

