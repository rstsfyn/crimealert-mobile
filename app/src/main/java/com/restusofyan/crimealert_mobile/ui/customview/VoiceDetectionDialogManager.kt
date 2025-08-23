package com.restusofyan.crimealert_mobile.ui.customview

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.restusofyan.crimealert_mobile.utils.SensitivityLevel
import com.restusofyan.crimealert_mobile.utils.VoiceDetectionService

class VoiceDetectionDialogManager(
    private val context: Context,
    private val fragmentManager: FragmentManager
) : DialogNavigationCallback {

    private var selectedSensitivity: SensitivityLevel = SensitivityLevel.LOW
    private var currentDialog: CustomDialogVoiceDetectionFragment? = null
    private var sensitivityDialog: CustomDialogSensitivityFragment? = null
    private var shareLocationDialog: CustomDialogShareLocationFragment? = null

    fun showVoiceDetectionFlow() {
        showVoiceDetectionDialog()
    }

    private fun showVoiceDetectionDialog() {
        dismissAllDialogs()

        currentDialog = CustomDialogVoiceDetectionFragment().apply {
            onYesClick = { onVoiceDetectionConfirmed() }
            onNoClick = {
                dismissAllDialogs()
                Toast.makeText(context, "Voice detection cancelled", Toast.LENGTH_SHORT).show()
            }
        }
        currentDialog?.show(fragmentManager, "VoiceDetectionDialog")
    }

    private fun showSensitivityDialog() {
        dismissAllDialogs()

        sensitivityDialog = CustomDialogSensitivityFragment().apply {
            onYesClick = { sensitivity -> onSensitivitySelected(sensitivity) }
            onNoClick = { onNavigateBack(DialogNavigationCallback.DialogType.SENSITIVITY) }
        }
        sensitivityDialog?.show(fragmentManager, "SensitivityDialog")
    }

    private fun showShareLocationDialog() {
        dismissAllDialogs()

        shareLocationDialog = CustomDialogShareLocationFragment().apply {
            setSensitivity(selectedSensitivity)
            onYesClick = { onLocationSharingConfirmed(selectedSensitivity) }
            onNoClick = { onNavigateBack(DialogNavigationCallback.DialogType.SHARE_LOCATION) }
        }
        shareLocationDialog?.show(fragmentManager, "ShareLocationDialog")
    }

    override fun onVoiceDetectionConfirmed() {
        Log.d("DialogManager", "Voice detection confirmed, showing sensitivity dialog")
        showSensitivityDialog()
    }

    override fun onSensitivitySelected(sensitivity: SensitivityLevel) {
        selectedSensitivity = sensitivity
        Log.d("DialogManager", "Sensitivity selected: ${sensitivity.displayName}")
        showShareLocationDialog()
    }

    override fun onLocationSharingConfirmed(sensitivity: SensitivityLevel) {
        Log.d("DialogManager", "Location sharing confirmed, starting service with ${sensitivity.displayName}")
        startVoiceDetectionService(sensitivity)
        dismissAllDialogs()
        Toast.makeText(context, "Voice detection started with ${sensitivity.displayName} sensitivity", Toast.LENGTH_SHORT).show()
    }

    override fun onNavigateBack(currentDialog: DialogNavigationCallback.DialogType) {
        Log.d("DialogManager", "Navigating back from $currentDialog")

        when (currentDialog) {
            DialogNavigationCallback.DialogType.SENSITIVITY -> {
                showVoiceDetectionDialog()
            }
            DialogNavigationCallback.DialogType.SHARE_LOCATION -> {
                showSensitivityDialog()
            }
            DialogNavigationCallback.DialogType.VOICE_DETECTION -> {
                dismissAllDialogs()
            }
        }
    }

    private fun startVoiceDetectionService(sensitivity: SensitivityLevel) {
        val serviceIntent = Intent(context, VoiceDetectionService::class.java).apply {
            putExtra(VoiceDetectionService.EXTRA_SENSITIVITY, sensitivity.displayName)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        Log.d("DialogManager", "VoiceDetectionService started with ${sensitivity.displayName} sensitivity")
    }

    private fun dismissAllDialogs() {
        currentDialog?.dismiss()
        sensitivityDialog?.dismiss()
        shareLocationDialog?.dismiss()

        currentDialog = null
        sensitivityDialog = null
        shareLocationDialog = null
    }

    fun cleanup() {
        dismissAllDialogs()
    }
}