package com.ghoast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.ghoast.model.Offer

class OffersViewModel : ViewModel() {

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val db = FirebaseFirestore.getInstance()

    fun fetchOffers() {
        viewModelScope.launch {
            db.collection("offers")
                .get()
                .addOnSuccessListener { result ->
                    val fetchedOffers = result.map { document ->
                        val data = document.data

                        Offer(
                            id = document.id,
                            shopName = data["shopName"] as? String ?: "",
                            shopImageUrl = data["shopImageUrl"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            category = data["category"] as? String ?: "",
                            discount = data["discount"] as? String ?: "",
                            distanceKm = (data["distanceKm"] as? Number)?.toDouble() ?: 0.0,
                            isNew = data["isNew"] as? Boolean ?: false,
                            endsSoon = data["endsSoon"] as? Boolean ?: false,
                            shopId = data["shopId"] as? String ?: "",
                            imageUrls = data["imageUrls"] as? List<String> ?: emptyList(),
                            location = data["location"] as? String ?: "",
                            timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.seconds ?: 0L,
                            latitude = (data["latitude"] as? Number)?.toDouble(),
                            longitude = (data["longitude"] as? Number)?.toDouble()
                        )
                    }
                    _offers.value = fetchedOffers
                }
        }
    }
}