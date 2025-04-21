package com.ghoast.model

data class ContactMessage(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)
