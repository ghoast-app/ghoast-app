package com.ghoast.model

data class Offer(
    val id: String = "",
    val shopName: String = "",           // ✅ Όνομα καταστήματος
    val shopImageUrl: String = "",       // ✅ Εικόνα προφίλ καταστήματος
    val title: String = "",              // ✅ Τίτλος προσφοράς
    val description: String = "",        // ✅ Περιγραφή (αν θες να την εμφανίσουμε αργότερα)
    val category: String = "",           // ✅ Κατηγορία (για φίλτρα)
    val discount: String = "",           // ✅ Ποσοστό έκπτωσης ή "1+1"
    val distanceKm: Int? = null, // ✅ Απόσταση από τον χρήστη (mock)
    val isNew: Boolean = false,           // ✅ Σήμανση ΝΕΟ
    val endsSoon: Boolean = false,        // ✅ Σήμανση λήξης
    val shopId: String = "",
    val imageUrls: List<String> = emptyList(),
    val location: String = "",
    val timestamp: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null
)
