package com.restusofyan.crimealert_mobile.ui.customview

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.restusofyan.crimealert_mobile.R
import com.restusofyan.crimealert_mobile.utils.SensitivityLevel

class CustomDialogSensitivityFragment : DialogFragment() {

    var onYesClick: ((SensitivityLevel) -> Unit)? = null
    var onNoClick: (() -> Unit)? = null

    private var selectedSensitivity = SensitivityLevel.LOW

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.fragment_custom_dialog_sensitivity, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setupSliderComponents(dialogView)

        val btnYes = dialogView.findViewById<Button>(R.id.btn_next)
        val btnNo = dialogView.findViewById<Button>(R.id.btn_back)

        btnYes.setOnClickListener {
            onYesClick?.invoke(selectedSensitivity)
            dismiss()
        }

        btnNo.setOnClickListener {
            onNoClick?.invoke()
            dismiss()
        }

        return dialog
    }

    private fun setupSliderComponents(dialogView: View) {
        val dotLow = dialogView.findViewById<View>(R.id.dot_low)
        val dotMid = dialogView.findViewById<View>(R.id.dot_mid)
        val dotHigh = dialogView.findViewById<View>(R.id.dot_high)

        val tvLow = dialogView.findViewById<TextView>(R.id.tv_low)
        val tvMid = dialogView.findViewById<TextView>(R.id.tv_mid)
        val tvHigh = dialogView.findViewById<TextView>(R.id.tv_high)

        val sliderContainer = dialogView.findViewById<View>(R.id.rl_slider_container)
        val sliderTrack = dialogView.findViewById<View>(R.id.view_slider_track)

        dotLow.setOnClickListener { updateSlider(SensitivityLevel.LOW, dialogView) }
        dotMid.setOnClickListener { updateSlider(SensitivityLevel.MID, dialogView) }
        dotHigh.setOnClickListener { updateSlider(SensitivityLevel.HIGH, dialogView) }

        tvLow.setOnClickListener { updateSlider(SensitivityLevel.LOW, dialogView) }
        tvMid.setOnClickListener { updateSlider(SensitivityLevel.MID, dialogView) }
        tvHigh.setOnClickListener { updateSlider(SensitivityLevel.HIGH, dialogView) }

        sliderTrack.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    handleSliderTouch(event.x, view, dialogView)
                    true
                }
                else -> false
            }
        }

        sliderContainer.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    handleSliderTouch(event.x, view, dialogView)
                    true
                }
                else -> false
            }
        }
        updateSlider(SensitivityLevel.LOW, dialogView)
    }

    private fun handleSliderTouch(touchX: Float, parentView: View, dialogView: View) {
        val containerWidth = parentView.width
        val marginHorizontal = dpToPx(8)
        val availableWidth = containerWidth - (marginHorizontal * 2)

        val relativeX = touchX - marginHorizontal
        val percentage = (relativeX / availableWidth).coerceIn(0f, 1f)

        val newSensitivity = when {
            percentage < 0.33f -> SensitivityLevel.LOW
            percentage < 0.67f -> SensitivityLevel.MID
            else -> SensitivityLevel.HIGH
        }

        updateSlider(newSensitivity, dialogView)
    }

    private fun updateSlider(sensitivity: SensitivityLevel, dialogView: View) {
        selectedSensitivity = sensitivity

        val dotLow = dialogView.findViewById<View>(R.id.dot_low)
        val dotMid = dialogView.findViewById<View>(R.id.dot_mid)
        val dotHigh = dialogView.findViewById<View>(R.id.dot_high)

        val tvLow = dialogView.findViewById<TextView>(R.id.tv_low)
        val tvMid = dialogView.findViewById<TextView>(R.id.tv_mid)
        val tvHigh = dialogView.findViewById<TextView>(R.id.tv_high)

        val sliderFill = dialogView.findViewById<View>(R.id.view_slider_fill)
        val descriptionText = dialogView.findViewById<TextView>(R.id.tv_sensitivity_description)

        resetAllDots(dotLow, dotMid, dotHigh)
        resetAllLabels(tvLow, tvMid, tvHigh)

        when (sensitivity) {
            SensitivityLevel.LOW -> {
                setActiveDot(dotLow)
                setActiveLabel(tvLow)
                updateSliderFill(sliderFill, 0)
                descriptionText.text = "More accurate detection, fewer false alarms. Best for quiet environments."
            }
            SensitivityLevel.MID -> {
                setActiveDot(dotMid)
                setActiveLabel(tvMid)
                updateSliderFill(sliderFill, 50)
                descriptionText.text = "Balanced detection with moderate sensitivity. Recommended for most situations."
            }
            SensitivityLevel.HIGH -> {
                setActiveDot(dotHigh)
                setActiveLabel(tvHigh)
                updateSliderFill(sliderFill, 100)
                descriptionText.text = "Most responsive detection, may pick up quieter sounds. Best when you cannot shout loudly."
            }
        }
    }

    private fun resetAllDots(vararg dots: View) {
        dots.forEach { dot ->
            dot.setBackgroundResource(R.drawable.slider_dot_inactive)
        }
    }

    private fun resetAllLabels(vararg labels: TextView) {
        labels.forEach { label ->
            label.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            label.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun setActiveDot(dot: View) {
        dot.setBackgroundResource(R.drawable.slider_dot_active)
    }

    private fun setActiveLabel(label: TextView) {
        label.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary))
        label.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    private fun updateSliderFill(sliderFill: View, percentageWidth: Int) {
        val containerWidth = (sliderFill.parent as View).width
        val marginHorizontal = dpToPx(8)
        val availableWidth = containerWidth - (marginHorizontal * 2)

        val targetWidth = when (percentageWidth) {
            0 -> dpToPx(20)
            50 -> availableWidth / 2
            100 -> availableWidth - dpToPx(20)
            else -> dpToPx(20)
        }

        sliderFill.layoutParams.width = targetWidth
        sliderFill.requestLayout()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}