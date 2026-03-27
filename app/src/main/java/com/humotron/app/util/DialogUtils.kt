package com.humotron.app.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.humotron.app.R

object DialogUtils {

    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        btnPositiveText: String = "Delete",
        btnNegativeText: String = "No",
        onPositiveClick: () -> Unit
    ) {
        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            .create()

        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_confirmation_dialog, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        val btnNo = dialogView.findViewById<TextView>(R.id.btnNo)
        val btnDelete = dialogView.findViewById<TextView>(R.id.btnDelete)

        tvTitle.text = title
        tvMessage.text = message
        btnNo.text = btnNegativeText
        btnDelete.text = btnPositiveText

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            onPositiveClick()
            dialog.dismiss()
        }

        dialog.setView(dialogView)
        dialog.show()

        // Adjust width to 270dp as per iOS style
        val width = (270 * context.resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }
}
