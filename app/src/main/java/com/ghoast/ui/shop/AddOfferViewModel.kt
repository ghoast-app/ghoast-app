package com.ghoast.ui.shop

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

                // 🔹 2. Λήψη στοιχείων καταστήματος
                val shopSnapshot = db.collection("shops").document(shopId).get().await()
                val shopName = shopSnapshot.getString("shopName") ?: ""
                val shopImageUrl = shopSnapshot.getString("shopImageUrl") ?: ""

                // 🔹 3. Δημιουργία προσφοράς
                val offer = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "discount" to discount,
                    "category" to category,
                    "shopId" to shopId,
                    "shopName" to shopName,
                    "shopImageUrl" to shopImageUrl,
                    "imageUrls" to imageUrls,
                    "timestamp" to System.currentTimeMillis(),
                    "distanceKm" to 1,
                    "isNew" to true,
                    "endsSoon" to false,
                    "location" to "",
                    "latitude" to null,
                    "longitude" to null
                )

                db.collection("offers").add(offer).await()

                onSuccess()
            } catch (e: Exception) {
                Log.e("AddOfferViewModel", "❌ Error saving offer", e)
                onError(e)
            }
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
