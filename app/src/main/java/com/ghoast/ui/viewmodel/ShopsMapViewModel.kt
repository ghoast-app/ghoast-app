package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopsMapViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    private val _filteredShops = MutableStateFlow<List<Shop>>(emptyList())
    val filteredShops: StateFlow<List<Shop>> = _filteredShops

    private val _favoriteShopIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteShopIds: StateFlow<Set<String>> = _favoriteShopIds

    var selectedCategory: String? = null
    var selectedDistance: Int? = null
    var onlyWithOffers: Boolean = false
    var onlyFromFavorites: Boolean = false

    var userLatitude: Double? = null
    var userLongitude: Double? = null

    init {
        fetchShops()
        fetchFavoriteShops()
    }

    private fun fetchShops() {
        viewModelScope.launch {
            db.collection("shops")
                .get()
                .addOnSuccessListener { snapshot ->
                    val result = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Shop::class.java)?.copy(id = doc.id)
                    }
                    _shops.value = result
                    applyFilters()
                }
        }
    }

    private fun fetchFavoriteShops() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("favorite_shops")
            .get()
            .addOnSuccessListener { snapshot ->
                _favoriteShopIds.value = snapshot.documents.map { it.id }.toSet()
                applyFilters()
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

    fun setOnlyWithOffersFilter(onlyWith: Boolean) {
        onlyWithOffers = onlyWith
        applyFilters()
    }

    fun setOnlyFromFavoritesFilter(onlyFav: Boolean) {
        onlyFromFavorites = onlyFav
        applyFilters()
    }

    fun applyFilters() {
        val category = selectedCategory
        val distance = selectedDistance
        val hasLocation = userLatitude != null && userLongitude != null
        val favoriteShops = favoriteShopIds.value

        _filteredShops.value = _shops.value.filter { shop ->
            val matchCategory = category == null || shop.categories.contains(category)
            val matchDistance = if (!hasLocation || distance == null) {
                true
            } else {
                val distanceKm = com.ghoast.util.LocationUtils.calculateHaversineDistance(
                    userLatitude!!, userLongitude!!,
                    shop.latitude, shop.longitude
                )
                distanceKm <= distance
            }
            val matchOffers = !onlyWithOffers || shopHasOffers(shop.id)
            val matchFav = !onlyFromFavorites || favoriteShops.contains(shop.id)

            matchCategory && matchDistance && matchOffers && matchFav
        }
    }

    private fun shopHasOffers(shopId: String): Boolean {
        // Αυτό μπορεί να βελτιωθεί με caching/flag, προς το παρόν placeholder always true
        return true
    }
}
