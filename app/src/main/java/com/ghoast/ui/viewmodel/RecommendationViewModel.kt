package com.ghoast.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _recommendedOffers = MutableStateFlow<List<Offer>>(emptyList())
    val recommendedOffers: StateFlow<List<Offer>> = _recommendedOffers

    var userLatitude: Double? = null
    var userLongitude: Double? = null

    fun loadRecommendedOffers() {
        viewModelScope.launch {
            db.collection("offers")
                .get()
                .addOnSuccessListener { result ->
                    val offers = result.mapNotNull {
                        val offer = it.toObject(Offer::class.java)?.copy(id = it.id)
                        if (offer != null && offer.latitude != null && offer.longitude != null && userLatitude != null && userLongitude != null) {
                            val results = FloatArray(1)
                            Location.distanceBetween(
                                userLatitude!!, userLongitude!!,
                                offer.latitude!!, offer.longitude!!,
                                results
                            )
                            offer.copy(distanceKm = (results[0] / 1000f).toDouble())

                        } else {
                            offer
                        }
                    }
                    Log.d("RecommendationVM", "✅ Recommended offers loaded: ${offers.size}")
                    _recommendedOffers.value = offers
                }
                .addOnFailureListener {
                    Log.e("RecommendationVM", "❌ Failed to load offers", it)
                }
        }
    }
}
