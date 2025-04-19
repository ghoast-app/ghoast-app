package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.Shop
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopsMapViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    init {
        fetchShops()
    }

    private fun fetchShops() {
        viewModelScope.launch {
            db.collection("shops")
                .get()
                .addOnSuccessListener { snapshot ->
                    val result = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Shop::class.java)
                    }
                    _shops.value = result
                }
        }
    }
}
