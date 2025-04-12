package com.ghoast.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ghoast.model.Offer
import com.ghoast.model.Shop
import com.ghoast.model.WorkingHour
import com.google.firebase.firestore.FirebaseFirestore

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

                            // ✅ Μετατροπή workingHours από Firestore
                            val workingHoursList = (shopSnapshot["workingHours"] as? List<Map<String, Any>>)?.mapNotNull { map ->
                                val day = map["day"] as? String ?: return@mapNotNull null
                                val from = map["from"] as? String
                                val to = map["to"] as? String
                                val enabled = map["enabled"] as? Boolean ?: false
                                WorkingHour(day = day, from = from, to = to, enabled = enabled)
                            } ?: emptyList()

                            shopState.value = shop?.copy(workingHours = workingHoursList)
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
