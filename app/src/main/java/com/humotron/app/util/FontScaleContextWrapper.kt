package com.humotron.app.util

import android.content.Context
import android.content.ContextWrapper

class FontScaleContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context): Context {
            val configuration = context.resources.configuration
            if (configuration.fontScale != 1.0f) {
                configuration.fontScale = 1.0f
                val newContext = context.createConfigurationContext(configuration)
                val metrics = newContext.resources.displayMetrics
                metrics.scaledDensity = configuration.fontScale * metrics.density
                return newContext
            }
            return context
        }
    }
}