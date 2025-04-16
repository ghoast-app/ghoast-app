package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyShopOffersViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    init {
        loadMyOffers()
    }

    fun loadMyOffers() {
        val shopId = auth.currentUser?.uid ?: return

        isLoading.value = true
        db.collection("offers")
            .whereEqualTo("shopId", shopId)
            .get()
            .addOnSuccessListener { result ->
                val offerList =
                    result.documents.mapNotNull { it.toObject(Offer::class.java)?.copy(id = it.id) }
                _offers.value = offerList
                isLoading.value = false
            }
            .addOnFailureListener {
                errorMessage.value = "Αποτυχία φόρτωσης προσφορών"
                isLoading.value = false
            }
    }

    fun deleteOffer(offerId: String, onSuccess: () -> Unit) {
        isLoading.value = true
        db.collection("offers").document(offerId)
            .delete()
            .addOnSuccessListener {
                loadMyOffers()
                isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                errorMessage.value = "Αποτυχία διαγραφής προσφοράς"
                isLoading.value = false
            }
    }
}
