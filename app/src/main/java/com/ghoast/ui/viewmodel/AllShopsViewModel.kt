package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ghoast.model.Shop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ShopSortMode(val label: String) {
    ALPHABETICAL("Αλφαβητικά"),
    NEWEST("Νεότερα"),
    DISTANCE("Μικρότερη Απόσταση"),
    OFFERED_RECENTLY("Καταστήματα Με Πρόσφατες Προσφορές")
}

class AllShopsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    private val _favoriteShopIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteShopIds: StateFlow<Set<String>> = _favoriteShopIds

    var userLatitude: Double? = null
    var userLongitude: Double? = null

    var selectedSortMode: ShopSortMode = ShopSortMode.ALPHABETICAL

    private val _sortedShops = MutableStateFlow<List<Shop>>(emptyList())
    val sortedShops: StateFlow<List<Shop>> = _sortedShops

    init {
        fetchShops()
        fetchFavoriteShops()
    }

    fun fetchShops() {
        db.collection("shops")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { doc ->
                    doc.toObject(Shop::class.java).copy(id = doc.id)
                }
                _shops.value = list
                applySorting()
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

    fun toggleFavorite(shopId: String) {
        val uid = auth.currentUser?.uid ?: return
        val favRef = db.collection("users").document(uid).collection("favorite_shops").document(shopId)

        val isFavorite = _favoriteShopIds.value.contains(shopId)
        val updatedFavorites = _favoriteShopIds.value.toMutableSet()

        if (isFavorite) {
            favRef.delete()
            updatedFavorites.remove(shopId)
        } else {
            favRef.set(mapOf("shopId" to shopId))
            updatedFavorites.add(shopId)
        }

        _favoriteShopIds.value = updatedFavorites
    }

    fun isFavorite(shopId: String): Boolean {
        return _favoriteShopIds.value.contains(shopId)
    }

    fun setSortMode(mode: ShopSortMode) {
        selectedSortMode = mode
        applySorting()
    }

    fun applySorting() {
        val lat = userLatitude
        val lng = userLongitude

        val sorted = when (selectedSortMode) {
            ShopSortMode.ALPHABETICAL -> _shops.value.sortedBy { it.shopName }
            ShopSortMode.NEWEST -> _shops.value.sortedByDescending { it.id } // ή it.timestamp αν έχεις
            ShopSortMode.DISTANCE -> {
                if (lat != null && lng != null) {
                    _shops.value.sortedBy {
                        com.ghoast.util.LocationUtils.calculateHaversineDistance(
                            lat, lng, it.latitude, it.longitude
                        )
                    }
                } else _shops.value
            }
            ShopSortMode.OFFERED_RECENTLY -> _shops.value.sortedByDescending { it.lastOfferTimestamp ?: 0L }
        }

        _sortedShops.value = sorted
    }
}
