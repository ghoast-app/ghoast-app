package com.ghoast.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ghoast.model.Offer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.ghoast.util.LocationUtils

class OffersViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _filteredOffers = MutableStateFlow<List<Offer>>(emptyList())
    val filteredOffers: StateFlow<List<Offer>> = _filteredOffers

    private val _favoriteOfferIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteOfferIds: StateFlow<Set<String>> = _favoriteOfferIds

    var selectedCategory: String? = null
    var selectedDistance: Int? = null

    var userLatitude: Double? = null
    var userLongitude: Double? = null

    init {
        fetchOffers()
        fetchFavoriteOffers()
    }

    fun fetchOffers(selectedCategory: String? = null, selectedDistance: Int? = null) {
        db.collection("offers")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("OffersViewModel", "❌ Firestore error", error)
                    return@addSnapshotListener
                }

                val allOffers = snapshot.documents.mapNotNull { doc ->
                    try {
                        val offer = doc.toObject(Offer::class.java)?.copy(id = doc.id)

                        // Log info for debugging each offer
                        if (offer != null) {
                            Log.d("OffersViewModel", "✅ Προσφορά: ${offer.title} (${offer.id}) - Lat: ${offer.latitude}, Lng: ${offer.longitude}")
                        }

                        offer
                    } catch (e: Exception) {
                        Log.e("OffersViewModel", "❌ Error parsing offer", e)
                        null
                    }
                }

                val filtered = allOffers.filter { offer ->
                    val matchesCategory = selectedCategory == null || offer.category == selectedCategory
                    val matchesDistance = selectedDistance == null || (
                            userLatitude != null && userLongitude != null &&
                                    offer.latitude != null && offer.longitude != null &&
                                    LocationUtils.calculateHaversineDistance(
                                        userLatitude!!, userLongitude!!,
                                        offer.latitude!!, offer.longitude!!
                                    ) <= selectedDistance
                            )

                    matchesCategory && matchesDistance
                }

                Log.i("OffersViewModel", "🎯 Προσφορές συνολικά: ${allOffers.size}, Φιλτραρισμένες: ${filtered.size}")

                _offers.value = allOffers
                _filteredOffers.value = filtered
            }
    }

    private fun applyFilters() {
        val currentDistance = selectedDistance
        _filteredOffers.value = _offers.value.filter { offer ->
            val matchesCategory = selectedCategory == null || offer.category == selectedCategory
            val matchesDistance = currentDistance == null || (offer.distanceKm != null && offer.distanceKm <= currentDistance)
            matchesCategory && matchesDistance
        }
    }

    fun fetchFavoriteOffers() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("favorite_offers")
            .get()
            .addOnSuccessListener { result ->
                val ids = result.map { it.id }.toSet()
                _favoriteOfferIds.value = ids
                Log.d("OffersViewModel", "⭐ Favorite Offers Loaded: $ids")
            }
    }

    fun listenToOffers() {
        db.collection("offers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OffersViewModel", "❌ Error listening to offers", error)
                    return@addSnapshotListener
                }

                val allOffers = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val offer = doc.toObject(Offer::class.java)?.copy(id = doc.id)
                        if (offer != null) {
                            Log.d("OffersViewModel", "🔁 Offer updated: ${offer.title} (${offer.id})")
                        }
                        offer
                    } catch (e: Exception) {
                        Log.e("OffersViewModel", "❌ Error parsing offer", e)
                        null
                    }
                } ?: emptyList()

                _offers.value = allOffers
                applyFilters()
            }
    }

    fun toggleFavorite(offerId: String) {
        val uid = auth.currentUser?.uid ?: return
        val favRef = db.collection("users")
            .document(uid)
            .collection("favorite_offers")
            .document(offerId)

        val currentFavorites = _favoriteOfferIds.value.toMutableSet()

        if (currentFavorites.contains(offerId)) {
            favRef.delete()
            currentFavorites.remove(offerId)
        } else {
            favRef.set(mapOf("offerId" to offerId))
            currentFavorites.add(offerId)
        }

        _favoriteOfferIds.value = currentFavorites
    }
}
