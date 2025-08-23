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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.ui.users.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
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
        const val EXTRA_SENSITIVITY = "sensitivity_level"
        private const val TAG = "VoiceDetectionService"
    }

    private var audioHelper: AudioClassificationHelper? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isScreamDetected = false
    private var sensitivityLevel: SensitivityLevel? = null // Initialize as null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()

        // Don't set default sensitivity here - wait for onStartCommand
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand called")

        // Priority 1: Get sensitivity from intent
        val sensitivityFromIntent = intent?.getStringExtra(EXTRA_SENSITIVITY)
        Log.d(TAG, "Received sensitivity from intent: $sensitivityFromIntent")

        if (sensitivityFromIntent != null) {
            sensitivityLevel = SensitivityLevel.fromString(sensitivityFromIntent)
            // Save to SharedPreferences for future use
            saveSensitivityToPrefs(sensitivityLevel!!)
            Log.d(TAG, "Set sensitivity from intent: ${sensitivityLevel!!.displayName} (${sensitivityLevel!!.threshold})")
        } else {
            // Priority 2: Load from SharedPreferences only if not set from intent
            sensitivityLevel = loadSensitivityFromPrefs()
            Log.d(TAG, "Loaded sensitivity from prefs: ${sensitivityLevel!!.displayName} (${sensitivityLevel!!.threshold})")
        }

        Log.d(TAG, "Final sensitivity level: ${sensitivityLevel!!.displayName} (threshold: ${sensitivityLevel!!.threshold})")

        // Update notification with current sensitivity
        startForeground(NOTIF_ID, buildNotification())

        // Stop any existing audio helper
        audioHelper?.stopAudioClassification()
        audioHelper = null

        setupAudioHelper()
        startVoiceDetection()

        return START_NOT_STICKY
    }

    private fun saveSensitivityToPrefs(sensitivity: SensitivityLevel) {
        val sharedPref = getSharedPreferences("voice_detection_settings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("sensitivity_level", sensitivity.displayName)
            putFloat("sensitivity_threshold", sensitivity.threshold.toFloat())
            apply()
        }
        Log.d(TAG, "Saved sensitivity to prefs: ${sensitivity.displayName} (${sensitivity.threshold})")
    }

    private fun loadSensitivityFromPrefs(): SensitivityLevel {
        val sharedPref = getSharedPreferences("voice_detection_settings", MODE_PRIVATE)
        val sensitivityName = sharedPref.getString("sensitivity_level", SensitivityLevel.LOW.displayName)
        val sensitivity = SensitivityLevel.fromString(sensitivityName ?: SensitivityLevel.LOW.displayName)
        Log.d(TAG, "Loading from prefs - Name: $sensitivityName, Sensitivity: ${sensitivity.displayName} (${sensitivity.threshold})")
        return sensitivity
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

        // Use current sensitivity or default
        val currentSensitivity = sensitivityLevel ?: SensitivityLevel.LOW

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Detection Active")
            .setContentText("Mendeteksi suara teriakan dengan sensitivitas ${currentSensitivity.displayName}...")
            .setSmallIcon(R.drawable.ic_mic_putih)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    private fun setupAudioHelper() {
        // Ensure sensitivity is set before setting up audio helper
        if (sensitivityLevel == null) {
            sensitivityLevel = loadSensitivityFromPrefs()
        }

        audioHelper = AudioClassificationHelper(
            context = this,
            classifierListener = object : AudioClassificationHelper.ClassifierListener {
                override fun onError(error: String) {
                    Log.e(TAG, "Audio classification error: $error")
                }

                override fun onResults(results: List<Category>, inferenceTime: Long) {
                    val currentSensitivity = sensitivityLevel ?: SensitivityLevel.LOW

                    for (category in results) {
                        Log.d(TAG, "Detection - Label: ${category.label}, Score: ${category.score}, Current Threshold: ${currentSensitivity.threshold}, Sensitivity: ${currentSensitivity.displayName}")

                        // Use the dynamic threshold based on sensitivity level
                        if (category.label == "scream" && category.score > currentSensitivity.threshold) {
                            Log.d(TAG, "🚨 SCREAM DETECTED! Score: ${category.score} > Threshold: ${currentSensitivity.threshold} (${currentSensitivity.displayName} sensitivity)")
                            isScreamDetected = true
                            audioHelper?.stopAudioClassification()
                            handleScreamDetected()
                            break
                        } else if (category.label == "scream") {
                            Log.d(TAG, "Scream detected but below threshold: ${category.score} <= ${currentSensitivity.threshold}")
                        }
                    }
                }
            }
        )
    }

    private fun startVoiceDetection() {
        val currentSensitivity = sensitivityLevel ?: SensitivityLevel.LOW
        audioHelper?.startAudioClassification()
        Log.d(TAG, "Voice detection started in service with ${currentSensitivity.displayName} sensitivity (threshold: ${currentSensitivity.threshold})")
    }

    private fun handleScreamDetected() {
        Log.d(TAG, "Handling scream detection...")

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

                if (isValidCoordinate(latitude, longitude)) {
                    Log.d(TAG, "Location captured: Lat: $latitude, Lng: $longitude")
                    saveScreamLocation(latitude, longitude)
                    showScreamDetectedNotification(latitude, longitude)
                    stopForeground(true)
                    stopSelf()
                } else {
                    Log.e(TAG, "Invalid coordinates: Lat: $latitude, Lng: $longitude")
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
        val currentSensitivity = sensitivityLevel ?: SensitivityLevel.LOW

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ Scream Detected!")
            .setContentText("Emergency sound detected (${currentSensitivity.displayName} sensitivity). Getting location...")
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
        val currentSensitivity = sensitivityLevel ?: SensitivityLevel.LOW

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val contentText = if (isLocationUnavailable) {
            "Scream detected with ${currentSensitivity.displayName} sensitivity! Location unavailable"
        } else {
            "Kamu melaporkan di lokasi ini $latitude, $longitude, semoga bantuan cepat datang"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Berhasil Melaporkan Insiden!")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_alertreport)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIF_ID + 1, notification)
    }

    private fun saveScreamLocation(latitude: Double, longitude: Double) {
        Log.d(TAG, "Saving scream location to API...")

        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        if (token == null) {
            Log.e(TAG, "Token not found")
            return
        }

        if (!isValidCoordinate(latitude, longitude)) {
            Log.e(TAG, "Invalid coordinates: Lat: $latitude, Lng: $longitude")
            return
        }

        Log.d(TAG, "Sending coordinates as Double - Lat: $latitude, Lng: $longitude")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.uploadScreamDetection(
                    token,
                    "scream",
                    latitude,
                    longitude
                )
                if (response.isSuccessful) {
                    Log.d(TAG, "Scream location saved to API successfully")
                    response.body()?.let { responseBody ->
                        Log.d(TAG, "Success response: $responseBody")
                    }
                } else {
                    Log.e(TAG, "Failed to save scream to API: ${response.code()}")
                    response.errorBody()?.let { errorBody ->
                        val errorString = errorBody.string()
                        Log.e(TAG, "Error response: $errorString")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving scream to API: ${e.message}", e)
            } finally {
                stopForeground(true)
                stopSelf()
            }
        }
        ScreamDetectionManager.getInstance().notifyScreamDetected(latitude, longitude)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        audioHelper?.stopAudioClassification()
        audioHelper = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}