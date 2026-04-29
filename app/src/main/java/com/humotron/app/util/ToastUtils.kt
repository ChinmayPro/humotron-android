package com.humotron.app.util

import android.content.Context
import android.widget.Toast

object ToastUtils {

    private var currentToast: Toast? = null

    fun showShort(context: Context, message: String) {
        // Cancel previous toast immediately
        currentToast?.cancel()

        // Show new toast
        currentToast = Toast.makeText(
            context.applicationContext,
            message,
            Toast.LENGTH_SHORT
        )

        currentToast?.show()
    }
}