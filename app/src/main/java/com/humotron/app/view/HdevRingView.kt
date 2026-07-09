package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet

/**
 * Connect animation for the Humotron Ring: two faint concentric glow rings,
 * an orbiting dot, and a breathing core.
 */
class HdevRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : HdevAnimationView(context, attrs, defStyleAttr) {

    init {
        // Falls back to this if no app:hdevColor is set in XML.
        if (attrs == null) color = Color.parseColor("#C4F23E")
    }

    override val defaultStages: List<String> = listOf(
        "Searching for your Ring",
        "Ring found",
        "Pairing securely",
        "Reading vitals baseline",
        "Connected"
    )

    override fun drawArt(canvas: Canvas) {
        // faint thick ring, no glow
        canvas.drawCircle(80f, 80f, 40f, glowStroke(9f, 0.15f, blurRadius = 0f))

        // two glowing thin rings
        canvas.drawCircle(80f, 80f, 46f, glowStroke(2f, 0.45f))
        canvas.drawCircle(80f, 80f, 34f, glowStroke(2f, 0.45f))

        // orbiting dot
        val angle = 360f * phase(2400)
        canvas.save()
        canvas.rotate(angle, 80f, 80f)
        canvas.drawCircle(80f, 40f, 5f, glowFill(1f, blurRadius = 3f))
        canvas.restore()

        // breathing core
        val breathe = pulse01(phase(2200))
        val coreRadius = 13f * (0.85f + 0.15f * breathe)
        canvas.drawCircle(80f, 80f, coreRadius, glowFill(0.16f))
    }
}
