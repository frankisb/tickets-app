package com.supportticketapp.presentation

import android.content.Context
import org.json.JSONObject
import java.io.File

// #region log de agente
object DebugLog {
    private const val LOG_FILE = "debug.log"

    fun ingest(context: Context, location: String, message: String, data: Map<String, Any?>, hypothesisId: String) {
        try {
            val dataJson = JSONObject()
            data.forEach { (k, v) -> dataJson.put(k, v?.toString() ?: "null") }
            val line = JSONObject().apply {
                put("location", location)
                put("message", message)
                put("data", dataJson)
                put("timestamp", System.currentTimeMillis())
                put("sessionId", "debug-session")
                put("hypothesisId", hypothesisId)
            }.toString() + "\n"
            File(context.filesDir, LOG_FILE).appendText(line)
        } catch (_: Exception) { }
    }
}
// #endregion
