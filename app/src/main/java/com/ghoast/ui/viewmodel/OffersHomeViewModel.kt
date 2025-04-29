// OffersHomeViewModel.kt

package com.ghoast.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OffersHomeViewModel : ViewModel() {

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchOffers()
    }

    fun fetchOffers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("offers")
                    .get()
                    .addOnSuccessListener { result ->
                        val offersList = result.documents.mapNotNull { it.toObject(Offer::class.java) }
                        _offers.value = offersList
                        _isLoading.value = false
                    }
                    .addOnFailureListener {
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun refreshOffers() {
        fetchOffers()
    }
}
