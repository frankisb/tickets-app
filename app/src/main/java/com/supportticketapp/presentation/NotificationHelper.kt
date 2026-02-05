package com.supportticketapp.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.supportticketapp.R

object NotificationHelper {

    private const val CHANNEL_ID = "ticket_updates"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Actualizaciones de Tickets",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Recibe notificaciones sobre cambios en el estado de tus tickets"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showTicketStatusChanged(context: Context, ticketId: String, newStatus: String) {
        Log.d("NotificationHelper", "Intentando mostrar notificación local")
        Log.d("NotificationHelper", "Ticket ID: $ticketId, Nuevo estado: $newStatus")
        
        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Estado del ticket actualizado")
            .setContentText("Ticket $ticketId: $newStatus")
            .setStyle(NotificationCompat.BigTextStyle().bigText("El ticket $ticketId ha cambiado su estado a: $newStatus"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(ticketId.hashCode(), notification)
            Log.d("NotificationHelper", "Notificación enviada exitosamente")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error al enviar notificación", e)
        }
    }

    fun showNotification(context: Context, title: String, body: String) {
        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        NotificationManagerCompat.from(context)
            .notify((title + body).hashCode(), notification)
    }
}
