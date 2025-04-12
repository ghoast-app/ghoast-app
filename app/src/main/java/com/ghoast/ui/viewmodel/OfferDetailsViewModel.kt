package com.ghoast.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ghoast.model.Offer
import com.ghoast.model.Shop
import com.google.firebase.firestore.FirebaseFirestore
import com.ghoast.viewmodel.OfferDetailsViewModel

class OfferDetailsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    val offerState = mutableStateOf<Offer?>(null)
    val shopState = mutableStateOf<Shop?>(null)
    val isLoading = mutableStateOf(false)

    fun loadOfferAndShop(offerId: String) {
        isLoading.value = true

        db.collection("offers").document(offerId).get()
            .addOnSuccessListener { offerSnapshot ->
                val offer = offerSnapshot.toObject(Offer::class.java)
                offerState.value = offer

                val shopId = offer?.shopId
                if (shopId != null) {
                    db.collection("shops").document(shopId).get()
                        .addOnSuccessListener { shopSnapshot ->
                            val shop = shopSnapshot.toObject(Shop::class.java)
                            shopState.value = shop
                            isLoading.value = false
                        }
                        .addOnFailureListener {
                            isLoading.value = false
                        }
                } else {
                    isLoading.value = false
                }
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }
}
