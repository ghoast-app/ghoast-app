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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GhoastFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Νέα Προσφορά!"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Δείτε τώρα την προσφορά στην εφαρμογή."

        Log.d("FCM_SERVICE", "📩 Ελήφθη ειδοποίηση: $title - $body")
        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "offers_channel"
        val notificationId = (System.currentTimeMillis() % 10000).toInt()

        // Intent για όταν πατηθεί το notification (προαιρετικό)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // 🔁 Φρόντισε να υπάρχει, αλλιώς άλλαξε το σε android.R.drawable.ic_dialog_info
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)

        // Δημιουργία καναλιού για Android 8+
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
}
