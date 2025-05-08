package com.ghoast.ui.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    private val userId get() = auth.currentUser?.uid ?: ""

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        if (userId.isBlank()) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    NotificationItem(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        body = data["body"] as? String ?: "",
                        timestamp = data["timestamp"] as? Long ?: 0L
                    )
                }
                _notifications.value = list
            } catch (e: Exception) {
                _notifications.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        if (userId.isBlank()) return

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notificationId)
                    .delete()
                    .await()

                _notifications.value = _notifications.value.filterNot { it.id == notificationId }
            } catch (e: Exception) {
                // Handle error (log or notify)
            }
        }
    }

    fun clearAllNotifications() {
        if (userId.isBlank()) return

        viewModelScope.launch {
            try {
                val batch = db.batch()
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .get()
                    .await()

                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
                _notifications.value = emptyList()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
