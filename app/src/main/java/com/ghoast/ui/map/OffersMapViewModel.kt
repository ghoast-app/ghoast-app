package com.ghoast.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OffersMapViewModel : ViewModel() {

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    init {
        loadOffers()
    }

    private fun loadOffers() {
        viewModelScope.launch {
            FirebaseFirestore.getInstance()
                .collection("offers")
                .get()
                .addOnSuccessListener { result ->
                    val offerList = result.documents.mapNotNull {
                        it.toObject(Offer::class.java)?.copy(id = it.id)
                    }
                    _offers.value = offerList
                }
        }
    }
}
