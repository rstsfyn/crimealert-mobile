package com.restusofyan.crimealert_mobile.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.ui.users.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.tensorflow.lite.support.label.Category
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VoiceDetectionService : Service() {
    @Inject
    lateinit var repository: CrimeAlertRepository

    companion object {
        const val CHANNEL_ID = "voice_detection_channel"
        const val CHANNEL_NAME = "Voice Detection Service"
        const val NOTIF_ID = 1
        private const val TAG = "VoiceDetectionService"
    }

    private var audioHelper: AudioClassificationHelper? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isScreamDetected = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupAudioHelper()
        startVoiceDetection()
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
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Detection Active")
            .setContentText("Mendeteksi suara teriakan dan membagikan lokasi...")
            .setSmallIcon(R.drawable.ic_mic_putih)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    private fun setupAudioHelper() {
        audioHelper = AudioClassificationHelper(
            context = this,
            classifierListener = object : AudioClassificationHelper.ClassifierListener {
                override fun onError(error: String) {
                    Log.e(TAG, "Audio classification error: $error")
                }

                override fun onResults(results: List<Category>, inferenceTime: Long) {
                    for (category in results) {
                        Log.d(TAG, "Label: ${category.label}, Score: ${category.score}")
                        if (category.label == "scream" && category.score > 0.9) {
                            Log.d(TAG, "Screaming sound detected with score ${category.score}")
                            isScreamDetected = true
                            handleScreamDetected()
                            break
                        }
                    }
                }
            }
        )
    }

    private fun startVoiceDetection() {
        audioHelper?.startAudioClassification()
        Log.d(TAG, "Voice detection started in service")
    }

    private fun handleScreamDetected() {
        Log.d(TAG, "Handling scream detection...")

        // Update notification
        updateNotificationForScreamDetected()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted")
            stopForeground(true)
            stopSelf()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                // ✅ Better validation for coordinates
                if (isValidCoordinate(latitude, longitude)) {
                    Log.d(TAG, "Location captured: Lat: $latitude, Lng: $longitude")
                    saveScreamLocation(latitude, longitude)
                    showScreamDetectedNotification(latitude, longitude)
                    stopForeground(true)
                    stopSelf()
                } else {
                    Log.e(TAG, "Invalid coordinates: Lat: $latitude, Lng: $longitude")
                    // Try to get fresh location
                    requestFreshLocation()
                }
            } else {
                Log.e(TAG, "Location is null, trying to get fresh location")
                requestFreshLocation()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting location: ${exception.message}")
            requestFreshLocation()
        }
    }

    private fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
        return latitude != 0.0 && longitude != 0.0 &&
                latitude >= -90.0 && latitude <= 90.0 &&
                longitude >= -180.0 && longitude <= 180.0 &&
                !latitude.isNaN() && !longitude.isNaN() &&
                !latitude.isInfinite() && !longitude.isInfinite()
    }

    private fun requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted")
            stopForeground(true)
            stopSelf()
            return
        }

        // Use getCurrentLocation for fresh location
        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location: Location? ->
            if (location != null && isValidCoordinate(location.latitude, location.longitude)) {
                Log.d(TAG, "Fresh location obtained: Lat: ${location.latitude}, Lng: ${location.longitude}")
                saveScreamLocation(location.latitude, location.longitude)
                showScreamDetectedNotification(location.latitude, location.longitude)
            } else {
                Log.e(TAG, "Failed to get valid fresh location")
                // Show notification without coordinates
                showScreamDetectedNotification(0.0, 0.0, isLocationUnavailable = true)
            }
            stopForeground(true)
            stopSelf()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting fresh location: ${exception.message}")
            showScreamDetectedNotification(0.0, 0.0, isLocationUnavailable = true)
            stopForeground(true)
            stopSelf()
        }
    }

    private fun updateNotificationForScreamDetected() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ Scream Detected!")
            .setContentText("Emergency sound detected. Getting location...")
            .setSmallIcon(R.drawable.ic_mic_putih)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID, notification)
    }

    private fun showScreamDetectedNotification(
        latitude: Double,
        longitude: Double,
        isLocationUnavailable: Boolean = false
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val contentText = if (isLocationUnavailable) {
            "Scream detected! Location unavailable"
        } else {
            "Scream detected! Location: $latitude, $longitude"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 Emergency Alert!")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_mic_putih)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIF_ID + 1, notification)

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val normalNotification = buildNotification()
            notificationManager.notify(NOTIF_ID, normalNotification)
        }, 5000)
    }

    private fun saveScreamLocation(latitude: Double, longitude: Double) {
        Log.d(TAG, "Saving scream location to API...")

        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        if (token == null) {
            Log.e(TAG, "Token not found")
            return
        }

        // ✅ Remove the problematic 0.0 check - use proper validation instead
        if (!isValidCoordinate(latitude, longitude)) {
            Log.e(TAG, "Invalid coordinates: Lat: $latitude, Lng: $longitude")
            return
        }

        val voiceDetection = RequestBody.create("text/plain".toMediaTypeOrNull(), "scream")
        val latBody = RequestBody.create("text/plain".toMediaTypeOrNull(), latitude.toString())
        val longBody = RequestBody.create("text/plain".toMediaTypeOrNull(), longitude.toString())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.uploadScreamDetection(
                    token, voiceDetection, latBody, longBody
                )
                if (response.isSuccessful) {
                    Log.d(TAG, "Scream location saved to API successfully")
                } else {
                    Log.e(TAG, "Failed to save scream to API: ${response.code()}")
                    // Log response body for debugging
                    response.errorBody()?.let { errorBody ->
                        Log.e(TAG, "Error response: ${errorBody.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving scream to API: ${e.message}")
            }
        }

        ScreamDetectionManager.getInstance().notifyScreamDetected(latitude, longitude)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand called")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        audioHelper?.stopAudioClassification()
        audioHelper = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}