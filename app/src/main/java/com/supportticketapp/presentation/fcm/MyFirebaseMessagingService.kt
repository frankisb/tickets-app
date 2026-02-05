package com.supportticketapp.presentation.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.supportticketapp.presentation.NotificationHelper
import com.supportticketapp.presentation.UserPreferences

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "FCM token refreshed: $token")
        // Guardar token en UserPreferences o enviar a Firestore
        UserPreferences.setFcmToken(this, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received: ${remoteMessage.notification?.title}")

        val title = remoteMessage.notification?.title ?: "Nuevo mensaje"
        val body = remoteMessage.notification?.body ?: "Tienes una actualización en tu ticket"

        // Mostrar notificación local (puedes usar NotificationHelper si ya existe)
        NotificationHelper.showNotification(this, title, body)
    }
}
