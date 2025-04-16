package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import com.ghoast.model.Shop
import com.ghoast.model.WorkingHour
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EditShopProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop

    init {
        loadShopData()
    }

    fun loadShopData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("shops").document(uid)
            .get()
            .addOnSuccessListener { document ->
                _shop.value = document.toObject(Shop::class.java)
            }
    }

    fun updateShopProfile(
        shopName: String,
        category: String,
        phone: String,
        email: String,
        website: String,
        address: String,
        profilePhotoUri: String,
        workingHours: List<WorkingHour>,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return

        val updatedData = mapOf(
            "shopName" to shopName,
            "categories" to listOf(category),
            "phone" to phone,
            "email" to email,
            "website" to website,
            "address" to address,
            "profilePhotoUri" to profilePhotoUri,
            "workingHours" to workingHours,
            "latitude" to latitude,
            "longitude" to longitude
        )

        db.collection("shops").document(uid)
            .update(updatedData)
            .addOnSuccessListener {
                loadShopData()
                onSuccess()
            }
    }
}
