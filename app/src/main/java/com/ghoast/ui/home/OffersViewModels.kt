package com.ghoast.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ghoast.model.Offer
import com.ghoast.util.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OffersViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _filteredOffers = MutableStateFlow<List<Offer>>(emptyList())
    val filteredOffers: StateFlow<List<Offer>> = _filteredOffers

    private val _favoriteOfferIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteOfferIds: StateFlow<Set<String>> = _favoriteOfferIds

    private val _favoriteShopIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteShopIds: StateFlow<Set<String>> = _favoriteShopIds

    var selectedCategory: String? = null
    var selectedDistance: Int? = null
    var onlyNewOffers: Boolean = false
    var onlyFromFavoriteShops: Boolean = false

    var userLatitude: Double? = null
    var userLongitude: Double? = null

    init {
        listenToOffers()
        fetchFavoriteOffers()
        fetchFavoriteShops()
    }

    fun listenToOffers() {
        db.collection("offers")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("OffersViewModel", "❌ Firestore error", error)
                    return@addSnapshotListener
                }

                val allOffers = snapshot.documents.mapNotNull { doc ->
                    try {
                        val rawOffer = doc.toObject(Offer::class.java)?.copy(id = doc.id)

                        val updatedOffer = if (
                            rawOffer != null &&
                            userLatitude != null && userLongitude != null &&
                            rawOffer.latitude != null && rawOffer.longitude != null
                        ) {
                            val distance = LocationUtils.calculateHaversineDistance(
                                userLatitude!!, userLongitude!!,
                                rawOffer.latitude!!, rawOffer.longitude!!
                            )
                            rawOffer.copy(distanceKm = String.format("%.1f", distance).toDouble())
                        } else {
                            rawOffer
                        }

                        updatedOffer
                    } catch (e: Exception) {
                        Log.e("OffersViewModel", "❌ Error parsing offer", e)
                        null
                    }
                }

                _offers.value = allOffers
                applyFilters()
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
            }
    }

    fun fetchFavoriteShops() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("favorite_shops")
            .get()
            .addOnSuccessListener { result ->
                val ids = result.map { it.id }.toSet()
                _favoriteShopIds.value = ids
            }
    }

    fun setCategoryFilter(category: String?) {
        selectedCategory = category
        applyFilters()
    }

    fun setDistanceFilter(distance: Int?) {
        selectedDistance = distance
        applyFilters()
    }

    fun setOnlyNewOffersFilter(onlyNew: Boolean) {
        onlyNewOffers = onlyNew
        applyFilters()
    }

    fun setOnlyFromFavoriteShopsFilter(onlyFav: Boolean) {
        onlyFromFavoriteShops = onlyFav
        applyFilters()
    }

    fun applyFilters() {
        val category = selectedCategory
        val distance = selectedDistance
        val hasLocation = userLatitude != null && userLongitude != null
        val favoriteShops = favoriteShopIds.value

        _filteredOffers.value = _offers.value.filter { offer ->
            val matchCategory = category == null || offer.category == category
            val matchDistance = if (!hasLocation || distance == null) {
                true
            } else {
                offer.distanceKm != null && offer.distanceKm!! <= distance
            }
            val matchNew = !onlyNewOffers || offer.isNew
            val matchFavoriteShop = !onlyFromFavoriteShops || favoriteShops.contains(offer.shopId)

            matchCategory && matchDistance && matchNew && matchFavoriteShop
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
