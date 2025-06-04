package com.restusofyan.crimealert_mobile.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.restusofyan.crimealert_mobile.data.notification.NotificationItem
import com.restusofyan.crimealert_mobile.data.notification.NotificationType
import com.restusofyan.crimealert_mobile.data.repository.NotificationRepository
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import com.restusofyan.crimealert_mobile.ui.users.detailcases.DetailCasesActivity
import com.restusofyan.crimealert_mobile.ui.alluserpage.detailinsidens.DetailInsidensActivity
import com.restusofyan.crimealert_mobile.ui.police.detailcasespolice.DetailCasesPoliceActivity

class SocketManager(private val context: Context) {

    private lateinit var notificationRepository: NotificationRepository

    companion object {
        private const val TAG = "SocketManager"
        private const val CHANNEL_ID = "push_notifications"
        private const val CHANNEL_NAME = "Push Notifications"
        private const val NOTIFICATION_REQUEST_CODE_INCIDENT = 1001
        private const val NOTIFICATION_REQUEST_CODE_REPORT = 1002
    }

    private lateinit var mSocket: Socket

    fun initializeSocket() {
        createNotificationChannel()

        notificationRepository = NotificationRepository(context)

        try {
            val opts = IO.Options()
            opts.forceNew = true
            opts.reconnection = true

            mSocket = IO.socket("http://20.11.0.124", opts)
            mSocket.connect()

            mSocket.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected")
            }

            mSocket.on("newInsiden", onNewInsiden)
            mSocket.on("newReport", onNewReport)

        } catch (e: Exception) {
            Log.e(TAG, "Socket connection error: ${e.message}")
        }
    }

    private val onNewInsiden = Emitter.Listener { args ->
        if (args.isNotEmpty()) {
            try {
                val data = args[0].toString()
                Log.d(TAG, "New Insiden received: $data")
                val jsonData = JSONObject(data)

                val notificationItem = NotificationItem(
                    id = jsonData.optString("id_insiden", System.currentTimeMillis().toString()),
                    type = NotificationType.INCIDENT,
                    title = jsonData.optString("title", "Disini ada orang yang membutuhkan bantuan, ada deteksi kejahatan disini, ayo validasi!!"),
                    description = jsonData.optString("description", ""),
                    timestamp = jsonData.optString("created_at", ""),
                    latitude = jsonData.optDouble("latitude", 0.0),
                    longitude = jsonData.optDouble("longitude", 0.0),
                    reporterName = jsonData.optString("name", "Unknown"),
                    reporterAvatar = jsonData.optString("avatar", null)
                )

                notificationRepository.saveNotification(notificationItem)

                val intent = Intent(context, DetailInsidensActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

                    putExtra("incident_id", jsonData.optString("id_insiden", ""))
                    putExtra("incident_title", jsonData.optString("title", "Disini ada orang yang membutuhkan bantuan, ada deteksi kejahatan disini, ayo validasi!!"))
                    putExtra("incident_latitude", jsonData.optDouble("latitude", 0.0))
                    putExtra("incident_longitude", jsonData.optDouble("longitude", 0.0))
                    putExtra("incident_time", jsonData.optString("created_at", ""))
                    putExtra("incident_date", jsonData.optString("created_at", ""))
                    putExtra("incident_description", jsonData.optString("description", ""))
                    putExtra("incident_reportername", jsonData.optString("name", ""))
                    putExtra("incident_reporteravatar", jsonData.optString("avatar", ""))
                }

                showNotificationWithIntent(
                    "New Incident",
                    jsonData.optString("title", "Disini ada orang yang membutuhkan bantuan, ada deteksi kejahatan disini, ayo validasi!!"),
                    intent,
                    NOTIFICATION_REQUEST_CODE_INCIDENT
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error parsing incident data: ${e.message}")
                e.printStackTrace()
                showNotification("New Incident", args[0].toString())
            }
        }
    }

    private fun getUserRole(): String? {
        return context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
            .getString("role", null)
    }

    private val onNewReport = Emitter.Listener { args ->
        if (args.isNotEmpty()) {
            try {
                val data = args[0].toString()
                Log.d(TAG, "New Report: $data")

                val jsonData = JSONObject(data)
                val userRole = getUserRole()

                val notificationItem = NotificationItem(
                    id = jsonData.optString("id", System.currentTimeMillis().toString()),
                    type = NotificationType.REPORT,
                    title = jsonData.optString("title", "New Report"),
                    description = jsonData.optString("description", ""),
                    timestamp = jsonData.optString("created_at", ""),
                    latitude = jsonData.optDouble("latitude", 0.0),
                    longitude = jsonData.optDouble("longitude", 0.0),
                    imageUrl = jsonData.optString("picture", null),
                    statusKasus = jsonData.optString("status_kasus", null),
                    reporterName = jsonData.optString("name", "Unknown"),
                    reporterAvatar = jsonData.optString("avatar", null)
                )

                notificationRepository.saveNotification(notificationItem)

                val intent = if (userRole == "polisi") {
                    Intent(context, DetailCasesPoliceActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

                        putExtra("report_title", jsonData.optString("title", "New Report"))
                        putExtra("report_description", jsonData.optString("description", ""))
                        putExtra("report_date", jsonData.optString("created_at", ""))
                        putExtra("report_timestamp", jsonData.optString("created_at", ""))
                        putExtra("report_latitude", jsonData.optDouble("latitude", 0.0))
                        putExtra("report_longitude", jsonData.optDouble("longitude", 0.0))
                        putExtra("status_kasus", jsonData.optString("status_kasus", ""))
                        putExtra("report_image_url", jsonData.optString("picture", ""))
                        putExtra("report_id", jsonData.optString("id", ""))
                        putExtra("avatar_reporter", jsonData.optString("avatar", ""))
                        putExtra("name_reporter", jsonData.optString("name", ""))
                    }
                } else {
                    Intent(context, DetailCasesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("news_title", jsonData.optString("title", "New Report"))
                        putExtra("news_description", jsonData.optString("description", ""))
                        putExtra("news_date", jsonData.optString("created_at", ""))
                        putExtra("news_timestamp", jsonData.optString("created_at", ""))
                        putExtra("news_latitude", jsonData.optDouble("latitude", 0.0))
                        putExtra("news_longitude", jsonData.optDouble("longitude", 0.0))
                        putExtra("status_kasus", jsonData.optString("status_kasus", ""))
                        putExtra("news_image", jsonData.optString("picture", ""))
                        putExtra("report_id", jsonData.optString("id", ""))
//                        putExtra("incident_reportername", jsonData.optString("name", ""))
//                        putExtra("incident_reporteravatar", jsonData.optString("avatar", ""))
                    }
                }

                showNotificationWithIntent(
                    "New Report",
                    jsonData.optString("title", "New Report"),
                    intent,
                    NOTIFICATION_REQUEST_CODE_REPORT
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error parsing report data: ${e.message}")
                e.printStackTrace()
                showNotification("New Report", args[0].toString())
            }
        }
    }



    private fun showNotificationWithIntent(title: String, message: String, intent: Intent, requestCode: Int) {
        Log.d(TAG, "Attempting to show notification with intent")
        Log.d(TAG, "Title: $title, Message: $message")
        Log.d(TAG, "Request code: $requestCode")

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted")
            return
        }

        try {
            val packageManager = context.packageManager
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo == null) {
                Log.e(TAG, "Target activity not found: ${intent.component}")
                return
            } else {
                Log.d(TAG, "Target activity found: ${resolveInfo.activityInfo.name}")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )

            Log.d(TAG, "PendingIntent created successfully")

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationId = System.currentTimeMillis().toInt()
            Log.d(TAG, "Showing notification with ID: $notificationId")

            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }

            Log.d(TAG, "Notification displayed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error creating or showing notification: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showNotification(title: String, message: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } else {
            Log.w(TAG, "Notifikasi tidak bisa ditampilkan, izin belum diberikan.")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for push notifications"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    fun disconnect() {
        if (::mSocket.isInitialized) {
            mSocket.disconnect()
            mSocket.off()
        }
    }
}