package com.humotron.app.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFade

fun String.toTitleFromCamelCase(): String {
    return try {
        this.replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        this
    }
}

fun ViewGroup.showWithFade(duration: Long = 1000, block: () -> Unit) {
    val transition = MaterialFade().apply {
        this.duration = duration
    }
    TransitionManager.beginDelayedTransition(this, transition)
    this.isVisible=true
    block()
}

fun View.fadeIn(duration: Long = 300) {
    alpha = 0f
    isVisible = true
    animate()
        .alpha(1f)
        .setDuration(duration)
        .start()
}