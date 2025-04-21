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

class FavoritesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid.orEmpty()

    private val _favoriteOffers = MutableStateFlow<List<Offer>>(emptyList())
    val favoriteOffers: StateFlow<List<Offer>> = _favoriteOffers

    private val _favoriteShops = MutableStateFlow<List<Shop>>(emptyList())
    val favoriteShops: StateFlow<List<Shop>> = _favoriteShops

    init {
        loadFavoriteOffers()
        loadFavoriteShops()
    }

    private fun loadFavoriteOffers() {
        viewModelScope.launch {
            val offers = mutableListOf<Offer>()
            try {
                val snapshot = db.collection("favorites_offers")
                    .whereEqualTo("userId", userId)
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
        }
    }

    private fun loadFavoriteShops() {
        viewModelScope.launch {
            val shops = mutableListOf<Shop>()
            try {
                val snapshot = db.collection("favorites_shops")
                    .whereEqualTo("userId", userId)
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
        }
    }

    fun toggleFavoriteOffer(offerId: String) {
        viewModelScope.launch {
            val favRef = db.collection("favorites_offers")
                .whereEqualTo("userId", userId)
                .whereEqualTo("offerId", offerId)
                .get()
                .await()

            if (favRef.isEmpty) {
                // Add to favorites
                db.collection("favorites_offers")
                    .add(mapOf("userId" to userId, "offerId" to offerId))
                loadFavoriteOffers()
            } else {
                // Remove from favorites
                for (doc in favRef.documents) {
                    db.collection("favorites_offers").document(doc.id).delete()
                }
                loadFavoriteOffers()
            }
        }
    }

    fun toggleFavoriteShop(shopId: String) {
        viewModelScope.launch {
            val favRef = db.collection("favorites_shops")
                .whereEqualTo("userId", userId)
                .whereEqualTo("shopId", shopId)
                .get()
                .await()

            if (favRef.isEmpty) {
                // Add to favorites
                db.collection("favorites_shops")
                    .add(mapOf("userId" to userId, "shopId" to shopId))
                loadFavoriteShops()
            } else {
                // Remove from favorites
                for (doc in favRef.documents) {
                    db.collection("favorites_shops").document(doc.id).delete()
                }
                loadFavoriteShops()
            }
        }
    }
}
