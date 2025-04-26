package com.ghoast.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyShopsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadMyShops()
    }

    fun loadMyShops() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("Δεν υπάρχει συνδεδεμένος χρήστης")
                val result = db.collection("shops")
                    .whereEqualTo("ownerId", uid)
                    .get()
                    .await()
                val shopList = result.documents.mapNotNull { it.toObject(Shop::class.java)?.copy(id = it.id) }
                _shops.value = shopList
            } catch (e: Exception) {
                Log.e("MyShopsVM", "❌ Error loading shops", e)
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteShop(shopId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("shops").document(shopId).delete().await()
                Log.d("MyShopsVM", "✅ Shop deleted: $shopId")
                loadMyShops() // Refresh list
                onSuccess()
            } catch (e: Exception) {
                Log.e("MyShopsVM", "❌ Error deleting shop", e)
                _errorMessage.value = "Αποτυχία διαγραφής καταστήματος"
            }
        }
    }
}
