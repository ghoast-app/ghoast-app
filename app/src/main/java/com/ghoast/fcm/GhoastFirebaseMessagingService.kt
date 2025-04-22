package com.ghoast.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ghoast.MainActivity
import com.ghoast.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GhoastFirebaseMessagingService : FirebaseMessagingService() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Νέα Προσφορά!"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Δείτε τώρα την προσφορά στην εφαρμογή."
        val offerId = remoteMessage.data["offerId"] // Προαιρετικό

        Log.d("FCM_SERVICE", "📩 Ελήφθη ειδοποίηση: $title - $body")

        // 🔹 1. Εμφάνιση notification στο χρήστη
        showNotification(title, body)

        // 🔹 2. Αποθήκευση στο Firestore
        saveNotificationToFirestore(title, body, offerId)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "offers_channel"
        val notificationId = (System.currentTimeMillis() % 10000).toInt()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ειδοποιήσεις Προσφορών",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Λαμβάνετε ενημερώσεις για νέες προσφορές από αγαπημένα καταστήματα"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, builder.build())
    }

    private fun saveNotificationToFirestore(title: String, message: String, offerId: String?) {
        val currentUser = auth.currentUser ?: return
        val notification = hashMapOf(
            "title" to title,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "offerId" to offerId
        )

        db.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Log.d("FCM_SERVICE", "✅ Το notification αποθηκεύτηκε στο Firestore")
            }
            .addOnFailureListener {
                Log.e("FCM_SERVICE", "❌ Σφάλμα κατά την αποθήκευση notification", it)
            }
    }
}
