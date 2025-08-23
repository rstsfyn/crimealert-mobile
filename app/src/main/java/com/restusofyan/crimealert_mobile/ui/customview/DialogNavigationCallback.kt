
package com.restusofyan.crimealert_mobile.ui.customview

import com.restusofyan.crimealert_mobile.utils.SensitivityLevel

interface DialogNavigationCallback {
    fun onVoiceDetectionConfirmed()
    fun onSensitivitySelected(sensitivity: SensitivityLevel)
    fun onLocationSharingConfirmed(sensitivity: SensitivityLevel)
    fun onNavigateBack(currentDialog: DialogType)

    enum class DialogType {
        VOICE_DETECTION,
        SENSITIVITY,
        SHARE_LOCATION
    }
}