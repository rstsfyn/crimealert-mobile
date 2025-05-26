package com.restusofyan.crimealert_mobile.utils

import android.content.Context
import android.media.AudioRecord
import android.util.Log
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.Classifications
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AudioClassificationHelper (val threshold: Float = 0.2f,
                                 val maxResults: Int = 2,
                                 val modelName: String = "optimize_300mb_metadata.tflite",
//                                 val modelName: String = "CrimeAlert_model_metadata.tflite",
                                 val overlap: Float = 0.5f,
                                 val context: Context,
                                 var classifierListener: ClassifierListener? = null
) {
    private var audioClassifier: AudioClassifier? = null
    private var tensorAudio: TensorAudio? = null
    private var audioRecord: AudioRecord? = null

    private var executor: ScheduledThreadPoolExecutor? = null


    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(results: List<Category>, inferenceTime: Long)
    }

    companion object {
        private const val TAG = "AudioClassifierHelper"
        private const val SAMPLING_RATE_IN_HZ = 16000
        private const val EXPECTED_INPUT_LENGTH = 1.500f // dalam detik
        private const val REQUIRE_INPUT_BUFFER_SIZE =
            (SAMPLING_RATE_IN_HZ * EXPECTED_INPUT_LENGTH).toInt()
    }

    init {
        setupAudioClassifier()
    }

    private fun setupAudioClassifier() {
        try {
            println(threshold)
            val options = AudioClassifier.AudioClassifierOptions.builder()
                .setScoreThreshold(threshold)
                .setMaxResults(maxResults)
                .build()

            audioClassifier = AudioClassifier.createFromFileAndOptions(context, modelName, options)
            tensorAudio = audioClassifier?.createInputTensorAudio()
            audioRecord = audioClassifier?.createAudioRecord()
        } catch (e: Exception) {
            classifierListener?.onError("Error initializing audio classifier: ${e.message}")
            Log.e(TAG, "Error: ${e.message}")
        }
    }

    fun startAudioClassification() {
        if (audioRecord == null || tensorAudio == null) {
            classifierListener?.onError("AudioRecord or TensorAudio is not initialized")
            return
        }

        if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            println("sedang recording")
            return
        }


        audioRecord?.startRecording()

        executor = ScheduledThreadPoolExecutor(1)

        val classifyRunnable = Runnable {
            audioRecord?.let { classifyAudioAsync(it) }
        }

        val lengthInMilliSeconds = ((REQUIRE_INPUT_BUFFER_SIZE * 1.0f) / SAMPLING_RATE_IN_HZ) * 1000
        val interval = (lengthInMilliSeconds * (1 - overlap)).toLong()

        executor?.scheduleAtFixedRate(
            classifyRunnable,
            0,
            interval,
            TimeUnit.MILLISECONDS
        )
    }

    private fun classifyAudioAsync(audioRecord: AudioRecord) {
        tensorAudio?.load(audioRecord)
        val inferenceStartTime = System.currentTimeMillis()
        val results = audioClassifier?.classify(tensorAudio!!)
        val inferenceTime = System.currentTimeMillis() - inferenceStartTime

        processResults(results, inferenceTime)
    }


    fun stopAudioClassification() {
        executor?.shutdownNow()
        audioClassifier?.close()
        audioClassifier = null
        tensorAudio = null
        audioRecord?.stop()
    }


    private fun processResults(results: List<Classifications>?, inferenceTime: Long) {
        results?.flatMap { it.categories }
            ?.filter { it.score > threshold }
            ?.sortedByDescending { it.score }
            ?.take(maxResults)
            ?.let { classifierListener?.onResults(it, inferenceTime) }
    }

}


