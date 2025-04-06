package com.ghoast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.ghoast.model.Offer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OffersViewModel : ViewModel() {

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _filteredOffers = MutableStateFlow<List<Offer>>(emptyList())
    val filteredOffers: StateFlow<List<Offer>> = _filteredOffers

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    fun setUserLocation(lat: Double, lon: Double) {
        userLatitude = lat
        userLongitude = lon
    }

    fun fetchOffers(selectedCategory: String? = null, selectedDistance: Int? = null) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance()
                .collection("offers")
                .get()
                .addOnSuccessListener { result ->
                    val allOffers = result.toObjects(Offer::class.java)

                    val filtered = allOffers.filter { offer ->
                        val matchesCategory = selectedCategory == null || offer.category == selectedCategory
                        val matchesDistance = selectedDistance == null || (
                                userLatitude != null && userLongitude != null &&
                                        offer.latitude != null && offer.longitude != null &&
                                        distanceBetween(
                                            userLatitude!!, userLongitude!!,
                                            offer.latitude!!, offer.longitude!!
                                        ) <= selectedDistance
                                )
                        matchesCategory && matchesDistance
                    }

                    _offers.value = allOffers
                    _filteredOffers.value = filtered
                }
        }
    }

    private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusKm * c
    }
}
