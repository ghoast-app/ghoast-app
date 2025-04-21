package com.ghoast.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghoast.model.ContactMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContactMessagesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<ContactMessage>>(emptyList())
    val messages: StateFlow<List<ContactMessage>> = _messages

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchMessages()
    }

    fun fetchMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val snapshot = db.collection("contact_messages")
                    .orderBy("timestamp")
                    .get()
                    .await()

                val messagesList = snapshot.documents.map { doc ->
                    val message = doc.toObject(ContactMessage::class.java)
                    message?.copy(id = doc.id)
                }.filterNotNull()

                _messages.value = messagesList
                Log.d("ContactMessages", "✅ Fetched ${messagesList.size} messages")

            } catch (e: Exception) {
                Log.e("ContactMessages", "❌ Error fetching messages", e)
                _error.value = "Σφάλμα κατά την ανάκτηση των μηνυμάτων."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
