package com.humotron.app.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.humotron.app.R

fun Context.cmdErrorTip(code: Int) {
    when (code) {
        0 -> toast(R.string.cmd_execute_success)
        1 -> toast(R.string.cmd_execute_failed_1)
        2 -> toast(R.string.cmd_execute_failed_2)
        3 -> toast(R.string.cmd_execute_failed_3)
        4 -> toast(R.string.cmd_execute_failed_4)
        5 -> toast(R.string.cmd_execute_failed_5)
        6 -> toast(R.string.cmd_execute_failed_6)
    }
}

var toast: Toast? = null

fun Context.toast(tip: String) {
    toast?.cancel()
    toast = Toast.makeText(this, tip, Toast.LENGTH_SHORT)
        .apply { show() }
}

fun Context.toast(@StringRes tip: Int) {
    toast?.cancel()
    toast = Toast.makeText(this, tip, Toast.LENGTH_SHORT)
        .apply { show() }
}

fun androidx.fragment.app.Fragment.toast(tip: String) {
    context?.toast(tip)
}

fun androidx.fragment.app.Fragment.toast(@StringRes tip: Int) {
    context?.toast(tip)
}