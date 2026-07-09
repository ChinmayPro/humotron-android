package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet

/**
 * Connect animation for the Smart Scale: outward ripples, a scale body
 * outline, a readout panel, and three rising dots simulating weight settling.
 */
class HdevScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HdevAnimationView(context, attrs, defStyleAttr) {

    init {
        if (attrs == null) color = Color.parseColor("#C4F23E")
    }

    override val defaultStages: List<String> = listOf(
        "Searching for your Scale",
        "Scale found",
        "Pairing securely",
        "Calibrating the load cells",
        "Connected"
    )

    override fun drawArt(canvas: Canvas) {
        // two staggered outward ripples
        for (delay in longArrayOf(0L, 800L)) {
            val p = phase(2400, delay)
            val eased = easeOut(p)
            val radius = 42f * (0.45f + 1.05f * eased)
            val alpha = 0.7f * (1f - eased)
            canvas.drawCircle(80f, 80f, radius, glowStroke(2f, alpha, blurRadius = 0f))
        }

        // body outline
        canvas.drawRoundRect(RectF(42f, 42f, 118f, 118f), 18f, 18f, glowStroke(2.4f, 0.5f))

        // readout panel
        canvas.drawRoundRect(RectF(64f, 94f, 96f, 106f), 4f, 4f, glowFill(0.2f))

        // three rising dots, staggered
        val dotXs = floatArrayOf(68f, 80f, 92f)
        val delays = longArrayOf(0L, 800L, 1500L)
        for (i in 0..2) {
            val p = phase(2400, delays[i])
            val y = 68f + (16f - 38f * p) // translateY 16 -> -22
            val alpha = when {
                p < 0.3f -> p / 0.3f
                else -> 1f - (p - 0.3f) / 0.7f
            }
            canvas.drawCircle(dotXs[i], y, 3f, glowFill(alpha.coerceIn(0f, 1f), blurRadius = 3f))
        }
    }
}
