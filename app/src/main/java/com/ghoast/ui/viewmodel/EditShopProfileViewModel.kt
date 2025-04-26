package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ghoast.model.WorkingHour
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditShopProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop

    fun loadShopById(shopId: String?) {
        viewModelScope.launch {
            if (shopId != null) {
                db.collection("shops").document(shopId).get()
                    .addOnSuccessListener { document ->
                        document?.toObject(Shop::class.java)?.let {
                            _shop.value = it.copy(id = document.id)
                        }
                    }
            } else {
                // Load first shop of current user
                db.collection("shops")
                    .whereEqualTo("ownerId", currentUserId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { documents ->
                        val firstDoc = documents.firstOrNull()
                        firstDoc?.toObject(Shop::class.java)?.let {
                            _shop.value = it.copy(id = firstDoc.id)
                        }
                    }
            }
        }
    }

    fun updateShopProfile(
        shopId: String?,
        shopName: String,
        shopCategory: String,
        phone: String,
        email: String,
        website: String,
        address: String,
        imageUrl: String,
        workingHours: List<WorkingHour>,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit
    ) {
        val id = shopId ?: _shop.value?.id
        if (id != null) {
            val updatedShop = hashMapOf(
                "shopName" to shopName,
                "categories" to listOf(shopCategory),
                "phone" to phone,
                "email" to email,
                "website" to website,
                "address" to address,
                "profilePhotoUri" to imageUrl,
                "workingHours" to workingHours,
                "latitude" to latitude,
                "longitude" to longitude
            )
            db.collection("shops").document(id)
                .update(updatedShop as Map<String, Any>)
                .addOnSuccessListener { onSuccess() }
        }
    }
}
