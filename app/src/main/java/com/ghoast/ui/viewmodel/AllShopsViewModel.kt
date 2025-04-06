package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ghoast.model.Shop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AllShopsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    private val _favoriteShopIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteShopIds: StateFlow<Set<String>> = _favoriteShopIds

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
}
