package com.restusofyan.crimealert_mobile.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.ui.users.MainActivity

class VoiceDetectionService : Service() {

    companion object {
        const val CHANNEL_ID = "voice_detection_channel"
        const val CHANNEL_NAME = "Voice Detection Service"
        const val NOTIF_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildNotification(): Notification {
        // Buat Intent yang akan membuka aplikasi saat notifikasi diklik
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Detection Active")
            .setContentText("Mendeteksi suara teriakan dan membagikan lokasi...")
            .setSmallIcon(R.drawable.ic_mic_putih) // pastikan drawable ini ada
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)  // Set PendingIntent
            .setAutoCancel(true)  // Agar notifikasi hilang setelah diklik
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Bisa tambahkan logika tambahan di sini
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
