package com.ghoast.model

data class Offer(
    val id: String = "",
    val shopName: String = "",           // ✅ Όνομα καταστήματος
    val profilePhotoUri: String = "", // ✅ Εικόνα προφίλ καταστήματος
    val title: String = "",              // ✅ Τίτλος προσφοράς
    val description: String = "",        // ✅ Περιγραφή
    val category: String = "",           // ✅ Κατηγορία
    val discount: String = "",           // ✅ Ποσοστό έκπτωσης
    val distanceKm: Int? = null,         // ✅ Απόσταση
    val isNew: Boolean = false,          // ✅ Σήμανση ΝΕΟ
    val endsSoon: Boolean = false,       // ✅ Σήμανση λήξης
    val shopId: String = "",
    val imageUrls: List<String> = emptyList(),
    val location: String = "",
    val timestamp: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null
)
