package com.example.taxi.socket

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taxi.R
import com.example.taxi.components.service.KillStateDialogService
import com.example.taxi.domain.model.location.LocationRequest
import com.example.taxi.domain.model.socket.SocketMessage
import com.example.taxi.domain.model.socket.SocketOnlyForYouData
import com.example.taxi.domain.model.socket.SocketOrderCancelledData
import com.example.taxi.ui.home.order.OrderViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.IOException
import org.json.JSONObject


class SocketMessageProcessor(
    private val activity: Context,
    private val orderViewModel: OrderViewModel
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()


    fun sendLocation(request: LocationRequest) {
        orderViewModel.sendLocation(request = request)
    }

    fun handleMessage(message: String) {
        val type = Types.newParameterizedType(
            SocketMessage::class.java,
            Any::class.java
        )
        val adapter: JsonAdapter<SocketMessage<*>> = moshi.adapter(type)
        try {
            val socketMessage = adapter.fromJson(message)
            socketMessage?.let {
                handleSocketMessage(it, message)
            }
        } catch (e: JsonDataException) {
            Log.e("WebSocket", "Error parsing JSON", e)
        } catch (e: IOException) {
            Log.e("WebSocket", "Error reading JSON", e)
        }
    }

    private fun handleSocketMessage(socketMessage: SocketMessage<*>, message: String) {
        val status = socketMessage.status
        val key = socketMessage.key

        val type = Types.newParameterizedType(
            SocketMessage::class.java,
            SocketOnlyForYouData::class.java
        )
        val canceltype = Types.newParameterizedType(
            SocketMessage::class.java,
            SocketOrderCancelledData::class.java
        )


        if (status == 200) {
            when (key) {
                SocketConfig.ORDER_NEW -> {
                    val intent = Intent("com.example.taxi.ORDER_DATA_ACTION")
                    intent.putExtra("OrderData_new", message)
                    activity.sendBroadcast(intent)
                    showNotification("Пришёл новый заказ...","Cообщение")

                }
                SocketConfig.ORDER_UPDATE ->{
                    val intent = Intent("com.example.taxi.ORDER_DATA_ACTION")
                    intent.putExtra("orderData_update", message)
                    activity.sendBroadcast(intent)
                }

                SocketConfig.ORDER_CANCELLED -> {
                    val intent = Intent("com.example.taxi.ORDER_DATA_ACTION")
                    val adapter: JsonAdapter<SocketMessage<SocketOrderCancelledData>> =
                        moshi.adapter(canceltype)
                    val orderCancelledData = adapter.fromJson(message)

                    orderCancelledData?.let {
                        intent.putExtra("OrderData_canceled", orderCancelledData.data.order_id)
                        intent.putExtra(SocketConfig.ORDER_CANCELLED, SocketConfig.ORDER_CANCELLED)
                        intent.putExtra("driver_id", orderCancelledData.data.driver_id ?: -1)
                    }
                    activity.sendBroadcast(intent)


                }
                SocketConfig.SEND_NOTIFICATION_NEW_ORDER ->{
                    val jsonObject = JSONObject(message)
                    val data = jsonObject.getJSONObject("data")
                    val messageText = data.getString("message")

                    showNotification(messageText,"Xabar")
                }

                SocketConfig.SEND_NOTIFICATION ->{
                    val jsonObject = JSONObject(message)
                    val data = jsonObject.getJSONObject("data").getJSONObject("data")
                    val messageText = data.getString("message")
                    val time = data.getString("time")
                    showNotification(messageText,"Xabar")
                }
                SocketConfig.ORDER_ONLY_FOR_YOU -> {
                    startForegroundServiceWithMessage(message)
                }
                SocketConfig.ORDER_ACCEPTED -> {
                    // Handle ORDER_ACCEPTED case
                    val intent = Intent("com.example.taxi.ORDER_DATA_ACTION")
                    val adapter: JsonAdapter<SocketMessage<SocketOrderCancelledData>> =
                        moshi.adapter(canceltype)
                    val orderCancelledData = adapter.fromJson(message)
                    orderCancelledData?.let {
                        intent.putExtra("OrderData_canceled", orderCancelledData.data.order_id)
                        intent.putExtra(SocketConfig.ORDER_CANCELLED, SocketConfig.ORDER_ACCEPTED)

                    }
                    activity.sendBroadcast(intent)

                }


            }
        }
    }

    private fun showNotification(contentTex: String,title: String) {

        val notification = NotificationCompat.Builder(activity,"channel_notif")
            .setSmallIcon(R.drawable.ic_message)
            .setContentText(contentTex)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
        val notificationManager = NotificationManagerCompat.from(activity)
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(1, notification)
    }

    private fun startForegroundServiceWithMessage(message: String) {

        val intent = Intent(activity, KillStateDialogService::class.java)
        intent.putExtra("message", message)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent)
        } else {
            activity.startService(intent)
        }
    }

}