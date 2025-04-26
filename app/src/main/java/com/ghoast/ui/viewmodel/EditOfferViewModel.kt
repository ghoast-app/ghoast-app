package com.ghoast.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class EditOfferViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _offer = MutableStateFlow<Offer?>(null)
    val offer: StateFlow<Offer?> = _offer

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

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

    fun updateOffer(offerId: String, updatedOffer: Offer, newImageUris: List<Uri>) {
        isLoading.value = true

        if (newImageUris.isEmpty()) {
            // Ενημέρωση χωρίς νέες εικόνες
            db.collection("offers").document(offerId)
                .set(updatedOffer)
                .addOnSuccessListener { isLoading.value = false }
                .addOnFailureListener {
                    errorMessage.value = "Αποτυχία ενημέρωσης"
                    isLoading.value = false
                }
        } else {
            // Με νέες εικόνες
            uploadImages(newImageUris) { uploadedUrls ->
                val offerWithImages = updatedOffer.copy(imageUrls = uploadedUrls)
                db.collection("offers").document(offerId)
                    .set(offerWithImages)
                    .addOnSuccessListener { isLoading.value = false }
                    .addOnFailureListener {
                        errorMessage.value = "Αποτυχία ενημέρωσης"
                        isLoading.value = false
                    }
            }
        }
    }

    private fun uploadImages(uris: List<Uri>, onComplete: (List<String>) -> Unit) {
        viewModelScope.launch {
            val urls = mutableListOf<String>()
            val total = uris.size

            uris.forEach { uri ->
                val fileName = UUID.randomUUID().toString()
                val ref = storage.reference.child("offer_images/$fileName")

                ref.putFile(uri).continueWithTask { ref.downloadUrl }.addOnSuccessListener { downloadUri ->
                    urls.add(downloadUri.toString())
                    if (urls.size == total) onComplete(urls)
                }.addOnFailureListener {
                    errorMessage.value = "Αποτυχία αποστολής εικόνας"
                    isLoading.value = false
                }
            }
        }
    }

    fun deleteOffer(offerId: String, onSuccess: () -> Unit) {
        isLoading.value = true
        db.collection("offers").document(offerId)
            .delete()
            .addOnSuccessListener {
                isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                errorMessage.value = "Αποτυχία διαγραφής προσφοράς"
                isLoading.value = false
            }
    }
}
