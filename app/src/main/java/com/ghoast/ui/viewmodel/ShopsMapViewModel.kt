package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.ghoast.model.Shop
import com.ghoast.util.LocationUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopsMapViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    private val _filteredShops = MutableStateFlow<List<Shop>>(emptyList())
    val filteredShops: StateFlow<List<Shop>> = _filteredShops

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    private val activeShopIds = mutableSetOf<String>()

    var selectedCategory: String? = null
    var selectedDistance: Int? = null
    var onlyWithOffers: Boolean = false

    var userLatitude: Double? = null
    var userLongitude: Double? = null

    init {
        fetchShops()
        fetchOffers()
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

    private fun fetchOffers() {
        viewModelScope.launch {
            db.collection("offers")
                .get()
                .addOnSuccessListener { snapshot ->
                    val allOffers = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Offer::class.java)
                    }
                    _offers.value = allOffers
                    activeShopIds.clear()
                    activeShopIds.addAll(allOffers.mapNotNull { it.shopId }.toSet())
                    applyFilters()
                }
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

    fun setOnlyWithOffersFilter(enabled: Boolean) {
        onlyWithOffers = enabled
        applyFilters()
    }

    fun applyFilters() {
        val category = selectedCategory
        val distance = selectedDistance
        val hasLocation = userLatitude != null && userLongitude != null

        _filteredShops.value = _shops.value.filter { shop ->
            val matchCategory = category == null || shop.categories.contains(category)

            val matchDistance = if (!hasLocation || distance == null) {
                true
            } else {
                val shopLat = shop.latitude
                val shopLng = shop.longitude
                if (shopLat != 0.0 && shopLng != 0.0) {
                    val d = LocationUtils.calculateHaversineDistance(
                        userLatitude!!, userLongitude!!,
                        shopLat, shopLng
                    )
                    d <= distance
                } else true
            }

            val matchHasOffers = !onlyWithOffers || activeShopIds.contains(shop.id)

            matchCategory && matchDistance && matchHasOffers
        }
    }
}
