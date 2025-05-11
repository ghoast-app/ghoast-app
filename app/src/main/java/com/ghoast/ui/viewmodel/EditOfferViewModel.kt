package com.ghoast.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class EditOfferViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _offer = MutableStateFlow<Offer?>(null)
    val offer: StateFlow<Offer?> = _offer

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    val updateCompleted = MutableStateFlow(false)

    fun loadOffer(offerId: String) {
        isLoading.value = true
        db.collection("offers").document(offerId).get()
            .addOnSuccessListener { snapshot ->
                _offer.value = snapshot.toObject(Offer::class.java)?.copy(id = snapshot.id)
                isLoading.value = false
            }
            .addOnFailureListener {
                errorMessage.value = "Αποτυχία φόρτωσης προσφοράς"
                isLoading.value = false
            }
    }

    fun updateOffer(
        context: Context,
        offerId: String,
        updatedOffer: Offer,
        newImageUris: List<Uri>
    ) {
        isLoading.value = true
        updateCompleted.value = false

        val ownerId = auth.currentUser?.uid ?: run {
            errorMessage.value = "Ο χρήστης δεν είναι συνδεδεμένος"
            isLoading.value = false
            return
        }

        if (newImageUris.isEmpty()) {
            val offerWithOwner = updatedOffer.copy(shopOwnerId = ownerId)
            db.collection("offers").document(offerId)
                .set(offerWithOwner)
                .addOnSuccessListener {
                    isLoading.value = false
                    updateCompleted.value = true
                }
                .addOnFailureListener {
                    errorMessage.value = "❌ Αποτυχία ενημέρωσης"
                    isLoading.value = false
                }
        } else {
            uploadImages(context, newImageUris) { uploadedUrls ->
                val offerWithImages = updatedOffer.copy(
                    imageUrls = uploadedUrls,
                    shopOwnerId = ownerId
                )
                db.collection("offers").document(offerId)
                    .set(offerWithImages)
                    .addOnSuccessListener {
                        isLoading.value = false
                        updateCompleted.value = true
                    }
                    .addOnFailureListener {
                        errorMessage.value = "❌ Αποτυχία ενημέρωσης"
                        isLoading.value = false
                    }
            }
        }
    }

    fun deleteOffer(offerId: String, onSuccess: () -> Unit) {
        isLoading.value = true
        Log.d("EditOfferVM", "🗑️ Trying to delete offer with ID: $offerId")
        db.collection("offers").document(offerId)
            .delete()
            .addOnSuccessListener {
                isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("EditOfferVM", "❌ Σφάλμα κατά τη διαγραφή", e)
                errorMessage.value = "Αποτυχία διαγραφής προσφοράς"
                isLoading.value = false
            }
    }

    private fun uploadImages(context: Context, uris: List<Uri>, onComplete: (List<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val urls = mutableListOf<String>()
            val userId = auth.currentUser?.uid ?: run {
                withContext(Dispatchers.Main) {
                    errorMessage.value = "Ο χρήστης δεν είναι συνδεδεμένος"
                    isLoading.value = false
                }
                return@launch
            }

            for (uri in uris) {
                try {
                    val fileName = UUID.randomUUID().toString() + ".jpg"
                    val imageRef = storage.reference.child("offers/$userId/$fileName")

                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream == null) {
                        Log.e("EditOfferVM", "❌ InputStream is null for $uri")
                        withContext(Dispatchers.Main) {
                            errorMessage.value = "Αποτυχία αποστολής εικόνας"
                            isLoading.value = false
                        }
                        return@launch
                    }

                    inputStream.use {
                        imageRef.putStream(it).await()
                    }

                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    urls.add(downloadUrl)

                    Log.d("EditOfferVM", "✅ Uploaded $fileName")
                } catch (e: Exception) {
                    Log.e("EditOfferVM", "❌ Αποτυχία upload για εικόνα: $uri", e)
                    withContext(Dispatchers.Main) {
                        errorMessage.value = "Αποτυχία αποστολής εικόνας"
                        isLoading.value = false
                    }
                    return@launch
                }
            }

            withContext(Dispatchers.Main) {
                onComplete(urls)
            }
        }
    }
}
