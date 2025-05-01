package com.ghoast.ui.shop

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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

    val myShops = mutableStateListOf<Shop>()
    var selectedShop = mutableStateOf<Shop?>(null)

    fun loadMyShops() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("shops")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { result ->
                myShops.clear()
                for (doc in result) {
                    val shop = doc.toObject(Shop::class.java).copy(id = doc.id)
                    myShops.add(shop)
                }
                if (myShops.size == 1) selectedShop.value = myShops.first()
            }
            .addOnFailureListener {
                Log.e("AddOfferVM", "❌ Failed to load shops", it)
            }
    }

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
                val shop = selectedShop.value ?: throw Exception("Δεν επιλέχθηκε κατάστημα")

                // 🔹 Ανέβασμα εικόνων
                val imageUrls = uploadImages(imageUris)

                if (imageUrls.isEmpty()) {
                    throw Exception("Απέτυχε το ανέβασμα εικόνων")
                }

                // 🔹 Δημιουργία προσφοράς
                val offer = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "discount" to discount,
                    "category" to category,
                    "shopId" to shop.id,
                    "shopName" to shop.shopName,
                    "profilePhotoUri" to shop.profilePhotoUri.orEmpty(),
                    "imageUrls" to imageUrls,
                    "timestamp" to System.currentTimeMillis(),
                    "distanceKm" to 1,
                    "isNew" to true,
                    "endsSoon" to false,
                    "location" to shop.address.orEmpty(),
                    "latitude" to (shop.latitude ?: 0.0),
                    "longitude" to (shop.longitude ?: 0.0),
                )

                val offerRef = db.collection("offers").document()
                offer["id"] = offerRef.id
                offerRef.set(offer).await()

                // 🔔 Cloud Function για push
                callSendNotificationFunction(shop.id, shop.shopName, title)

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
        val userId = auth.currentUser?.uid ?: throw Exception("Μη έγκυρος χρήστης")
        for (uri in imageUris) {
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = storage.reference.child("offers/$userId/$fileName") // ✅ νέο path
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
