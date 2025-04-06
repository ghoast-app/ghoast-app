package com.ghoast.model

data class Shop(
    val id: String = "",
    val shopName: String = "",
    val address: String = "",
    val email: String = "",
    val phone: String = "",
    val website: String = "",
    val profilePhotoUri: String = "",
    val categories: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0

)
