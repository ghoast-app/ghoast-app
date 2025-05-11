package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.ghoast.model.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.ghoast.util.LocationUtils

enum class FavoriteShopSortMode(val label: String) {
    ALPHABETICAL("Αλφαβητικά"),
    DISTANCE("Μικρότερη Απόσταση"),
    OFFERED_RECENTLY("Πιο Πρόσφατες Προσφορές")
}

enum class FavoriteOfferSortMode(val label: String) {
    NEWEST("Πιο Πρόσφατες"),
    DISCOUNT("Μεγαλύτερη Έκπτωση"),
    DISTANCE("Μικρότερη Απόσταση")
}

class FavoritesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId get() = auth.currentUser?.uid.orEmpty()

    private val _favoriteOffers = MutableStateFlow<List<Offer>>(emptyList())
    val favoriteOffers: StateFlow<List<Offer>> = _favoriteOffers

    private val _favoriteShops = MutableStateFlow<List<Shop>>(emptyList())
    val favoriteShops: StateFlow<List<Shop>> = _favoriteShops

    private val _sortedFavoriteShops = MutableStateFlow<List<Shop>>(emptyList())
    val sortedFavoriteShops: StateFlow<List<Shop>> = _sortedFavoriteShops

    private val _sortedFavoriteOffers = MutableStateFlow<List<Offer>>(emptyList())
    val sortedFavoriteOffers: StateFlow<List<Offer>> = _sortedFavoriteOffers

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _shopSearchQuery = MutableStateFlow("")
    val shopSearchQuery: StateFlow<String> = _shopSearchQuery

    private val _filteredFavoriteOffers = MutableStateFlow<List<Offer>>(emptyList())
    val filteredFavoriteOffers: StateFlow<List<Offer>> = _filteredFavoriteOffers

    private val _filteredFavoriteShops = MutableStateFlow<List<Shop>>(emptyList())
    val filteredFavoriteShops: StateFlow<List<Shop>> = _filteredFavoriteShops

    var userLatitude: Double? = null
    var userLongitude: Double? = null

    private var currentSortMode: FavoriteShopSortMode = FavoriteShopSortMode.ALPHABETICAL
    private var currentOfferSortMode: FavoriteOfferSortMode = FavoriteOfferSortMode.NEWEST

    init {
        loadFavoriteOffers()
        loadFavoriteShops()
    }

    private fun loadFavoriteOffers() {
        viewModelScope.launch {
            val offers = mutableListOf<Offer>()
            try {
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("favorite_offers")
                    .get()
                    .await()

                for (doc in snapshot) {
                    val offerId = doc.getString("offerId") ?: continue
                    val offerSnap = db.collection("offers").document(offerId).get().await()
                    offerSnap.toObject(Offer::class.java)?.let { offers.add(it.copy(id = offerId)) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _favoriteOffers.value = offers
            applyFavoriteOfferSorting()
        }
    }

    private fun loadFavoriteShops() {
        viewModelScope.launch {
            val shops = mutableListOf<Shop>()
            try {
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("favorite_shops")
                    .get()
                    .await()

                for (doc in snapshot) {
                    val shopId = doc.getString("shopId") ?: continue
                    val shopSnap = db.collection("shops").document(shopId).get().await()
                    shopSnap.toObject(Shop::class.java)?.let { shops.add(it.copy(id = shopId)) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _favoriteShops.value = shops
            applyFavoriteShopSorting()
        }
    }

    fun toggleFavoriteOffer(offerId: String) {
        viewModelScope.launch {
            val favRef = db.collection("users")
                .document(userId)
                .collection("favorite_offers")
                .whereEqualTo("offerId", offerId)
                .get()
                .await()

            if (favRef.isEmpty) {
                db.collection("users")
                    .document(userId)
                    .collection("favorite_offers")
                    .add(mapOf("offerId" to offerId))
            } else {
                for (doc in favRef.documents) {
                    db.collection("users")
                        .document(userId)
                        .collection("favorite_offers")
                        .document(doc.id)
                        .delete()
                }
            }
            loadFavoriteOffers()
        }
    }

    fun toggleFavoriteShop(shopId: String) {
        viewModelScope.launch {
            val favRef = db.collection("users")
                .document(userId)
                .collection("favorite_shops")
                .whereEqualTo("shopId", shopId)
                .get()
                .await()

            if (favRef.isEmpty) {
                db.collection("users")
                    .document(userId)
                    .collection("favorite_shops")
                    .add(mapOf("shopId" to shopId))
            } else {
                for (doc in favRef.documents) {
                    db.collection("users")
                        .document(userId)
                        .collection("favorite_shops")
                        .document(doc.id)
                        .delete()
                }
            }
            loadFavoriteShops()
        }
    }

    fun setFavoriteSortMode(mode: FavoriteShopSortMode) {
        currentSortMode = mode
        applyFavoriteShopSorting()
    }

    private fun applyFavoriteShopSorting() {
        val lat = userLatitude
        val lng = userLongitude

        val sorted = when (currentSortMode) {
            FavoriteShopSortMode.ALPHABETICAL -> _favoriteShops.value.sortedBy { it.shopName }
            FavoriteShopSortMode.DISTANCE -> {
                if (lat != null && lng != null) {
                    _favoriteShops.value.sortedBy {
                        LocationUtils.calculateHaversineDistance(lat, lng, it.latitude, it.longitude)
                    }
                } else _favoriteShops.value
            }
            FavoriteShopSortMode.OFFERED_RECENTLY -> _favoriteShops.value.sortedByDescending { it.lastOfferTimestamp ?: 0L }
        }

        _sortedFavoriteShops.value = sorted
        applyShopSearchFilter()
    }

    fun setFavoriteOfferSortMode(mode: FavoriteOfferSortMode) {
        currentOfferSortMode = mode
        applyFavoriteOfferSorting()
    }

    private fun applyFavoriteOfferSorting() {
        val lat = userLatitude
        val lng = userLongitude

        val sorted = when (currentOfferSortMode) {
            FavoriteOfferSortMode.NEWEST -> _favoriteOffers.value.sortedByDescending { it.timestamp }
            FavoriteOfferSortMode.DISCOUNT -> _favoriteOffers.value.sortedByDescending {
                it.discount.replace("%", "").toIntOrNull() ?: 0
            }
            FavoriteOfferSortMode.DISTANCE -> {
                if (lat != null && lng != null) {
                    _favoriteOffers.value.sortedBy {
                        LocationUtils.calculateHaversineDistance(
                            lat, lng, it.latitude ?: 0.0, it.longitude ?: 0.0
                        )
                    }
                } else _favoriteOffers.value
            }
        }

        _sortedFavoriteOffers.value = sorted
        applyOfferSearchFilter()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyOfferSearchFilter()
    }

    private fun applyOfferSearchFilter() {
        val query = _searchQuery.value.trim().lowercase()

        _filteredFavoriteOffers.value = if (query.isBlank()) {
            _sortedFavoriteOffers.value
        } else {
            _sortedFavoriteOffers.value.filter {
                it.title.lowercase().contains(query) ||
                        it.description.lowercase().contains(query)
            }
        }
    }

    fun updateShopSearchQuery(query: String) {
        _shopSearchQuery.value = query
        applyShopSearchFilter()
    }

    private fun applyShopSearchFilter() {
        val query = _shopSearchQuery.value.trim().lowercase()

        _filteredFavoriteShops.value = if (query.isBlank()) {
            _sortedFavoriteShops.value
        } else {
            _sortedFavoriteShops.value.filter {
                it.shopName.lowercase().contains(query)
            }
        }
    }
}
