package com.restusofyan.crimealert_mobile.utils

import android.util.Log

class ScreamDetectionManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: ScreamDetectionManager? = null

        fun getInstance(): ScreamDetectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScreamDetectionManager().also { INSTANCE = it }
            }
        }

        private const val TAG = "ScreamDetectionManager"
    }

    interface ScreamDetectionListener {
        fun onScreamDetected(latitude: Double, longitude: Double)
    }

    private val listeners = mutableSetOf<ScreamDetectionListener>()

    fun addListener(listener: ScreamDetectionListener) {
        listeners.add(listener)
        Log.d(TAG, "Listener added. Total listeners: ${listeners.size}")
    }

    fun removeListener(listener: ScreamDetectionListener) {
        listeners.remove(listener)
        Log.d(TAG, "Listener removed. Total listeners: ${listeners.size}")
    }

    fun notifyScreamDetected(latitude: Double, longitude: Double) {
        Log.d(TAG, "Notifying ${listeners.size} listeners about scream detection")
        listeners.forEach { listener ->
            try {
                listener.onScreamDetected(latitude, longitude)
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying listener: ${e.message}")
            }
        }
    }

    fun clearAllListeners() {
        listeners.clear()
        Log.d(TAG, "All listeners cleared")
    }
}