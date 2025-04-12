package com.ghoast.model

import androidx.annotation.Keep

@Keep
data class WorkingHour(
    var day: String = "",
    var from: String? = null,
    var to: String? = null,
    var enabled: Boolean = false
)
