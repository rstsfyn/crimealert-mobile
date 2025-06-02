//package com.restusofyan.crimealert_mobile.utils
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import io.socket.client.IO
//import io.socket.client.Socket
//import io.socket.emitter.Emitter
//
//class SocketManager(private val context: Context) {
//
//    companion object {
//        private const val TAG = "SocketManager"
//        private const val CHANNEL_ID = "push_notifications"
//        private const val CHANNEL_NAME = "Push Notifications"
//    }
//
//    private lateinit var mSocket: Socket
//
//    fun initializeSocket() {
//        createNotificationChannel()
//
//        try {
//            val opts = IO.Options()
//            opts.forceNew = true
//            opts.reconnection = true
//
//            mSocket = IO.socket("http://YOUR_SERVER_IP:3000", opts) // Ganti IP backend kamu
//            mSocket.connect()
//
//            mSocket.on(Socket.EVENT_CONNECT) {
//                Log.d(TAG, "Socket connected")
//            }
//
//            mSocket.on("newInsiden", onNewInsiden)
//            mSocket.on("newReport", onNewReport)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Socket connection error: ${e.message}")
//        }
//    }
//
//    private val onNewInsiden = Emitter.Listener { args ->
//        if (args.isNotEmpty()) {
//            val data = args[0].toString()
//            Log.d(TAG, "New Insiden: $data")
//            showNotification("New Insiden", data)
//        }
//    }
//
//    private val onNewReport = Emitter.Listener { args ->
//        if (args.isNotEmpty()) {
//            val data = args[0].toString()
//            Log.d(TAG, "New Report: $data")
//            showNotification("New Report", data)
//        }
//    }
//
//    private fun showNotification(title: String, message: String) {
//        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(android.R.drawable.ic_dialog_info)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//
//        with(NotificationManagerCompat.from(context)) {
//            notify(System.currentTimeMillis().toInt(), builder.build())
//        }
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                CHANNEL_NAME,
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Channel for push notifications"
//            }
//            val notificationManager: NotificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    fun disconnect() {
//        if (::mSocket.isInitialized) {
//            mSocket.disconnect()
//            mSocket.off()
//        }
//    }
//}
