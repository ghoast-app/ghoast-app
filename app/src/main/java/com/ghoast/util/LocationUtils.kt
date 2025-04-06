package com.ghoast.util

import android.location.Location
import kotlin.math.*

object LocationUtils {

    // Υπολογισμός απόστασης σε χιλιόμετρα μεταξύ δύο συντεταγμένων (Haversine formula)
    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Ακτίνα Γης σε km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    // Υπολογισμός απόστασης σε μέτρα μεταξύ δύο Location αντικειμένων
    fun calculateDistance(start: Location?, end: Location?): Float {
        if (start == null || end == null) return Float.MAX_VALUE
        return start.distanceTo(end)
    }
}
