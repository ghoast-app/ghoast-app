package com.ghoast.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.ghoast.util.LocationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OffersHomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _filteredOffers = MutableStateFlow<List<Offer>>(emptyList())
    val filteredOffers: StateFlow<List<Offer>> = _filteredOffers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _favoriteOfferIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteOfferIds: StateFlow<Set<String>> = _favoriteOfferIds

    var selectedCategory: String? = null
    var selectedDistance: Int? = 10
    var selectedSortMode: SortMode = SortMode.DISTANCE
    var searchQuery: String = ""

    var userLatitude: Double? = null
    var userLongitude: Double? = null

    init {
        fetchOffers()
        fetchFavoriteOffers()
    }

    fun fetchOffers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("offers")
                    .get()
                    .addOnSuccessListener { result ->
                        val offersList = result.documents.mapNotNull { doc ->
                            val offer = doc.toObject(Offer::class.java)?.copy(id = doc.id)
                            offer?.let {
                                if (userLatitude != null && userLongitude != null && it.latitude != null && it.longitude != null) {
                                    val distance = LocationUtils.calculateHaversineDistance(
                                        userLatitude!!, userLongitude!!,
                                        it.latitude!!, it.longitude!!
                                    )
                                    it.copy(distanceKm = distance)
                                } else it
                            }
                        }
                        _offers.value = offersList
                        applyFilters()
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

    fun setCategoryFilter(category: String?) {
        selectedCategory = category
        applyFilters()
    }

    fun setDistanceFilter(distance: Int?) {
        selectedDistance = distance
        applyFilters()
    }

    fun setSortMode(mode: SortMode) {
        selectedSortMode = mode
        applyFilters()
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

    private fun applyFilters() {
        val category = selectedCategory
        val distance = selectedDistance
        val lat = userLatitude
        val lng = userLongitude
        val query = searchQuery.trim().lowercase()

        var filtered = _offers.value.filter { offer ->
            val matchCategory = category == null || category == "Όλες οι κατηγορίες" || offer.category == category
            val matchDistance = if (lat != null && lng != null && distance != null) {
                offer.distanceKm?.let { it <= distance } ?: true
            } else true
            val matchSearch = query.isBlank() || listOf(
                offer.shopName.lowercase(),
                offer.title.lowercase(),
                offer.description.lowercase()
            ).any { it.contains(query) }

            matchCategory && matchDistance && matchSearch
        }

        filtered = when (selectedSortMode) {
            SortMode.DISCOUNT -> filtered.sortedByDescending { it.discount.replace("%", "").toIntOrNull() ?: 0 }
            SortMode.NEWEST -> filtered.sortedByDescending { it.timestamp }
            SortMode.DISTANCE -> filtered.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
        }

        _filteredOffers.value = filtered
    }
}
