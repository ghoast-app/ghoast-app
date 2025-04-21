package com.ghoast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _messageSent = MutableStateFlow(false)
    val messageSent = _messageSent.asStateFlow()

    fun sendMessage(name: String, email: String, message: String) {
        val data = hashMapOf(
            "name" to name,
            "email" to email,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        viewModelScope.launch {
            db.collection("contactMessages")
                .add(data)
                .addOnSuccessListener {
                    _messageSent.value = true
                }
                .addOnFailureListener {
                    _messageSent.value = false
                }
        }
    }

    fun resetMessageState() {
        _messageSent.value = false
    }
}
