package com.restusofyan.crimealert_mobile.ui.customview

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.restusofyan.crimealert_mobile.R

class CustomDialogVoiceDetectionFragment : DialogFragment() {

    var onYesClick: (() -> Unit)? = null
    var onNoClick: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.fragment_custom_dialogue_voice_detection, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnYes = dialogView.findViewById<Button>(R.id.btn_enable)
        val btnNo = dialogView.findViewById<Button>(R.id.btn_cancel)

        btnYes.setOnClickListener {
            onYesClick?.invoke() // Memanggil fungsi dari Fragment utama
            dialog.dismiss()
            showShareLocationDialog()
        }

        btnNo.setOnClickListener {
            onNoClick?.invoke()
            dialog.dismiss()
        }

        return dialog
    }

    private fun showShareLocationDialog() {
        val shareLocationDialog = CustomDialogShareLocationFragment()
        shareLocationDialog.show(parentFragmentManager, CustomDialogShareLocationFragment::class.java.simpleName)
    }
}
