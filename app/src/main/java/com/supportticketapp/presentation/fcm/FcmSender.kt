package com.supportticketapp.presentation.fcm

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object FcmSender {
    private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"

    private val client = OkHttpClient()

    suspend fun sendNotification(toToken: String, title: String, body: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {

            
            Log.d("FcmSender", "Enviando notificación a token: $toToken")
            Log.d("FcmSender", "Título: $title, Mensaje: $body")
            

            true
        } catch (e: Exception) {
            Log.e("FcmSender", "Error al preparar notificación FCM", e)
            false
        }
    }
}
