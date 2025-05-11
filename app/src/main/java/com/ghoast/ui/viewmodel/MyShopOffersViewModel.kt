package com.ghoast.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Offer
import com.ghoast.model.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyShopOffersViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    init {
        loadShops()
    }

    fun loadShops() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("Δεν υπάρχει συνδεδεμένος χρήστης")
                val result = db.collection("shops")
                    .whereEqualTo("ownerId", uid)
                    .get()
                    .await()

                val shopList = result.documents.mapNotNull {
                    it.toObject(Shop::class.java)?.copy(id = it.id)
                }
                _shops.value = shopList

            } catch (e: Exception) {
                Log.e("MyShopOffersVM", "❌ Σφάλμα στη φόρτωση καταστημάτων", e)
                errorMessage.value = "Αποτυχία φόρτωσης καταστημάτων"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadOffersForShop(shopId: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val result = db.collection("offers")
                    .whereEqualTo("shopId", shopId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val offerList = result.documents.mapNotNull {
                    it.toObject(Offer::class.java)?.copy(id = it.id)
                }
                _offers.value = offerList
            } catch (e: Exception) {
                Log.e("MyShopOffersVM", "❌ Σφάλμα φόρτωσης προσφορών", e)
                errorMessage.value = "Αποτυχία φόρτωσης προσφορών"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun deleteOffer(offerId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("offers").document(offerId).delete().await()
                _offers.value = _offers.value.filterNot { it.id == offerId }
                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = "Αποτυχία διαγραφής προσφοράς"
            }
        }
    }
}
