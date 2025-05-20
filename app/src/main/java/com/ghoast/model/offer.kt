package com.ghoast.model

import com.google.firebase.Timestamp

data class Offer(
    val id: String = "",
    val shopName: String = "",
    val profilePhotoUri: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val discount: String = "",
    var distanceKm: Double? = null,
    val isNew: Boolean = false,
    val endsSoon: Boolean = false,
    val shopId: String = "",
    val imageUrls: List<String> = emptyList(),
    val location: String = "",
    val timestamp: Timestamp? = null,  // ✅ ΤΥΠΟΣ ΣΩΣΤΟΣ
    val latitude: Double? = null,
    val longitude: Double? = null,
    var distanceFromUser: Double? = null,
    val shopOwnerId: String = ""
)
