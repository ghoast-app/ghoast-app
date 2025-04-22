package com.ghoast.ui.shop

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class AddOfferViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    fun saveOffer(
        title: String,
        description: String,
        discount: String,
        category: String,
        imageUris: List<Uri>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val shopId = auth.currentUser?.uid ?: throw Exception("Δεν υπάρχει ενεργό κατάστημα")

                // 🔹 1. Ανέβασμα εικόνων
                val imageUrls = uploadImages(imageUris)

                if (imageUrls.isEmpty()) {
                    Log.e("AddOfferViewModel", "❌ Δεν ανέβηκαν επιτυχώς εικόνες")
                    throw Exception("Απέτυχε το ανέβασμα εικόνων")
                }

                // 🔹 2. Ανάκτηση καταστήματος
                val shopSnapshot = db.collection("shops").document(shopId).get().await()
                val shop = shopSnapshot.toObject(Shop::class.java)
                    ?: throw Exception("Δεν βρέθηκαν στοιχεία καταστήματος")

                val shopName = shop.shopName
                val profilePhotoUri = shop.profilePhotoUri ?: ""
                val latitude = shop.latitude ?: 0.0
                val longitude = shop.longitude ?: 0.0

                // 🔹 3. Δημιουργία προσφοράς
                val offer = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "discount" to discount,
                    "category" to category,
                    "shopId" to shopId,
                    "shopName" to shopName,
                    "profilePhotoUri" to profilePhotoUri,
                    "imageUrls" to imageUrls,
                    "timestamp" to System.currentTimeMillis(),
                    "distanceKm" to 1,
                    "isNew" to true,
                    "endsSoon" to false,
                    "location" to (shop.address ?: ""),
                    "latitude" to latitude,
                    "longitude" to longitude
                )

                val offerRef = db.collection("offers").document()
                offer["id"] = offerRef.id
                offerRef.set(offer).await()

                // 🔔 4. Κλήση Cloud Function για αποστολή ειδοποίησης
                callSendNotificationFunction(shopId, shopName, title)

                onSuccess()

            } catch (e: Exception) {
                Log.e("AddOfferViewModel", "❌ Error saving offer", e)
                onError(e)
            }
        }
    }

    private fun callSendNotificationFunction(shopId: String, shopName: String, offerTitle: String) {
        val data = hashMapOf(
            "shopId" to shopId,
            "shopName" to shopName,
            "offerTitle" to offerTitle
        )

        functions
            .getHttpsCallable("sendNotificationOnNewOffer")
            .call(data)
            .addOnSuccessListener {
                Log.d("CloudFunction", "✅ Notification function called successfully")
            }
            .addOnFailureListener { e ->
                Log.e("CloudFunction", "❌ Failed to call notification function", e)
            }
    }

    private suspend fun uploadImages(imageUris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
        val urls = mutableListOf<String>()
        for (uri in imageUris) {
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = storage.reference.child("offer_images/$fileName")
            try {
                val uploadTask = imageRef.putFile(uri).await()
                if (uploadTask.task.isSuccessful) {
                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    if (downloadUrl.isNotEmpty()) {
                        urls.add(downloadUrl)
                    }
                } else {
                    Log.e("AddOfferViewModel", "❌ Upload failed for $uri")
                }
            } catch (e: Exception) {
                Log.e("AddOfferViewModel", "❌ Exception uploading image", e)
            }
        }
        return@withContext urls
    }
}
