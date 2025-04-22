package com.ghoast.ui.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = 0L
)

class NotificationsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { result ->
                        val list = result.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            NotificationItem(
                                id = doc.id,
                                title = data["title"] as? String ?: "",
                                body = data["body"] as? String ?: "",
                                timestamp = data["timestamp"] as? Long ?: 0L
                            )
                        }
                        _notifications.value = list
                        _isLoading.value = false
                    }
                    .addOnFailureListener {
                        _notifications.value = emptyList()
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}
