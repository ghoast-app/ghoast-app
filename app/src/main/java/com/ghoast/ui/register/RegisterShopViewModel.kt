package com.ghoast.ui.register

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class RegisterShopViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun registerShop(
        shopName: String,
        address: String,
        phone: String,
        website: String,
        email: String,
        category: String,
        workingHours: List<Map<String, String>>,
        profileImageUri: Uri?,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("Ο χρήστης δεν είναι συνδεδεμένος")

                val imageUrl = profileImageUri?.let { uploadImage(it) }

                val shopData = hashMapOf(
                    "ownerId" to uid,
                    "shopName" to shopName,
                    "address" to address,
                    "phone" to phone,
                    "website" to website,
                    "email" to email,
                    "category" to category,
                    "workingHours" to workingHours,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "timestamp" to System.currentTimeMillis()
                )

                if (imageUrl != null) {
                    shopData["profilePhotoUri"] = imageUrl
                }

                // 🔸 Δημιουργία νέου document με random ID (όχι uid)
                db.collection("shops").add(shopData).await()

                onSuccess()
            } catch (e: Exception) {
                Log.e("RegisterShopVM", "❌ Error saving shop", e)
                onError(e)
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val imageRef = storage.reference.child("shop_profile_images/$fileName")
        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }
}
