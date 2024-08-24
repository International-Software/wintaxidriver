package com.example.taxi.socket

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.taxi.di.MAIN
import com.example.taxi.domain.model.socket.SocketMessage
import com.example.taxi.domain.preference.UserPreferenceManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI


class SocketRepository constructor(
    val context: Context,
    private var viewModelScope: CoroutineScope? = null,
    private val userPreferenceManager: UserPreferenceManager,
    private var socketMessageProcessor: SocketMessageProcessor? = null,
    private val onMessageReceived: () -> Unit
) {

    private var webSocket: WebSocketClient? = null
    private var shouldReconnect = true

    private var _isConnected: Boolean = false
    val socketLive=  MutableLiveData<Boolean>().apply {
        value = false
    }
    var isConnected: Boolean
        get() = _isConnected
        set(value) {
            if (_isConnected != value) {
                _isConnected = value

                sendSocketStatusBroadcast()

//                updateViewColor()
            }
        }
    var reconnectJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())


    private fun sendSocketStatusBroadcast() {
        val intent = Intent("SOCKET_STATUS")
        intent.putExtra("IS_CONNECTED", isConnected)
        context.sendBroadcast(intent)
    }


    fun initSocket(token: String) {
        if (reconnectJob?.isActive == true || isConnected) {
            return
        }

        reconnectJob = viewModelScope?.launch {
            connectSocket(token)
        }
    }

    private fun connectSocket(token: String) {
        shouldReconnect = true

        webSocket = object : WebSocketClient(URI("wss://$MAIN/connect/?token=$token")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
//                isConnectedSocket.value = true
                socketLive.postValue(true)
                isConnected = true
                userPreferenceManager.saveToggleState(true)

                Log.d("WebSocket", "Connection onOpen: ")

            }

            override fun onMessage(message: String?) {
                Log.d("pul", "onMessage: $message")
                val gson = Gson()
                val orderResponse = gson.fromJson(message, SocketMessage::class.java)
                if (orderResponse.key == "order_new" && userPreferenceManager.getDriverStatus() == UserPreferenceManager.DriverStatus.COMPLETED){
                    onMessageReceived()
                }
                message?.let {
                    socketMessageProcessor?.handleMessage(it)
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                isConnected = false
                socketLive.postValue(false)
                userPreferenceManager.saveToggleState(false)

//                isConnectedSocket.value = false
                if (shouldReconnect) {
                    reconnectSocket(token)
                }
//                reconnectSocket(token)
            }

            override fun onError(ex: Exception?) {
                Log.e("WebSocket", "Error occurred", ex)
                isConnected = false
                socketLive.postValue(false)
                userPreferenceManager.saveToggleState(false)

//                isConnectedSocket.postValue(false)
                if (shouldReconnect) {
                    reconnectSocket(token)
                }

            }
        }

        webSocket?.connect()
    }

    private fun reconnectSocket(token: String) {
        reconnectJob?.cancel()
        if (reconnectJob?.isActive != true || !isConnected) {
            reconnectJob = viewModelScope?.launch {
                delay(SocketConfig.RECONNECT_DELAY_MS)
                connectSocket(token)
            }
        }
    }


    private fun updateViewColor() {
        viewModelScope?.launch() {

//            socketViewModel?.setConnected(isConnected)
        }
    }

    fun disconnectSocket() {
        shouldReconnect = false
        userPreferenceManager.saveToggleState(false)

        handler.removeCallbacksAndMessages(null)
        reconnectJob?.cancel()
        webSocket?.close()

    }
}