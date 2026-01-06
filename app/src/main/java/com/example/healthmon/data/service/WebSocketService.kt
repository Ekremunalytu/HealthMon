package com.example.healthmon.data.service

import android.util.Log
import com.example.healthmon.data.model.WebSocketVitalData
import com.squareup.moshi.Moshi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket service for real-time vital data communication
 * 
 * Patient: Sends vital data to backend
 * Caregiver: Receives vital data from backend
 */
@Singleton
class WebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) {
    private var webSocket: WebSocket? = null
    private var isConnected = false
    
    companion object {
        private const val TAG = "WebSocketService"
        // Mac Hotspot IP: 192.168.2.1 (physical device via WiFi hotspot)
        private const val WS_BASE_URL = "ws://192.168.2.1:8000/ws"
        private const val NORMAL_CLOSURE_STATUS = 1000
    }

    /**
     * Connect to WebSocket for a specific patient (Caregiver receiving data)
     */
    fun connectToPatientVitals(patientId: String, token: String): Flow<WebSocketVitalData> = callbackFlow {
        val request = Request.Builder()
            .url("$WS_BASE_URL/vitals/$patientId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected for patient: $patientId")
                isConnected = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received: $text")
                try {
                    // Backend sends: {"type": "vital_data", "patient_id": "...", "data": {...}}
                    // We need to extract the nested data
                    val jsonObject = org.json.JSONObject(text)
                    val type = jsonObject.optString("type", "")
                    
                    if (type == "vital_data" || type == "new_measurement") {
                        // Extract from nested data object if present
                        val dataObj = jsonObject.optJSONObject("data") ?: jsonObject
                        
                        val vitalData = WebSocketVitalData(
                            patientId = jsonObject.optString("patient_id", patientId),
                            heartRate = dataObj.optInt("heart_rate", 0),
                            inactivitySeconds = dataObj.optInt("inactivity_seconds", 0),
                            status = dataObj.optString("status", "NORMAL"),
                            timestamp = dataObj.optString("timestamp", java.time.Instant.now().toString())
                        )
                        trySend(vitalData)
                    } else {
                        // Try direct parsing for simple format
                        val adapter = moshi.adapter(WebSocketVitalData::class.java)
                        val vitalData = adapter.fromJson(text)
                        vitalData?.let { trySend(it) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse WebSocket message", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
                webSocket.close(NORMAL_CLOSURE_STATUS, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                isConnected = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
                isConnected = false
                close(t)
            }
        }

        webSocket = okHttpClient.newWebSocket(request, listener)

        awaitClose {
            webSocket?.close(NORMAL_CLOSURE_STATUS, "Flow closed")
            webSocket = null
            isConnected = false
        }
    }

    /**
     * Send vital data through WebSocket (Patient sending data)
     */
    fun sendVitalData(patientId: String, heartRate: Int, inactivitySeconds: Int, status: String): Boolean {
        val vitalData = WebSocketVitalData(
            patientId = patientId,
            heartRate = heartRate,
            inactivitySeconds = inactivitySeconds,
            status = status,
            timestamp = java.time.Instant.now().toString()
        )

        return try {
            val adapter = moshi.adapter(WebSocketVitalData::class.java)
            val json = adapter.toJson(vitalData)
            webSocket?.send(json) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send vital data", e)
            false
        }
    }

    /**
     * Connect as patient to send vitals
     */
    fun connectAsPatient(patientId: String, token: String, onConnected: () -> Unit = {}) {
        val request = Request.Builder()
            .url("$WS_BASE_URL/patient/$patientId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Patient WebSocket connected: $patientId")
                isConnected = true
                onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Handle acknowledgments or commands from server
                Log.d(TAG, "Received from server: $text")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(NORMAL_CLOSURE_STATUS, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Patient WebSocket failure", t)
                isConnected = false
            }
        }

        webSocket = okHttpClient.newWebSocket(request, listener)
    }

    /**
     * Disconnect WebSocket
     */
    fun disconnect() {
        webSocket?.close(NORMAL_CLOSURE_STATUS, "User disconnected")
        webSocket = null
        isConnected = false
    }

    /**
     * Check if WebSocket is connected
     */
    fun isConnected(): Boolean = isConnected
}
