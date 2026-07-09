package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet

/**
 * Connect animation for the Humotron Band: a rounded band outline with a
 * scanning line and an ECG trace sweeping inside it.
 */
class HdevBandView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HdevAnimationView(context, attrs, defStyleAttr) {

    init {
        if (attrs == null) color = Color.parseColor("#C4F23E")
    }

    override val defaultStages: List<String> = listOf(
        "Searching for your Band",
        "Band found",
        "Pairing securely",
        "Syncing your day",
        "Connected"
    )

    private val beatPath = Path().apply {
        // Mirrors the web version's "M54 80h9l4-12 6 24 5-14 4 6h17"
        moveTo(54f, 80f)
        lineTo(63f, 80f)
        lineTo(67f, 68f)
        lineTo(73f, 92f)
        lineTo(78f, 78f)
        lineTo(82f, 84f)
        lineTo(99f, 84f)
    }

    override fun drawArt(canvas: Canvas) {
        // top and bottom band nubs
        canvas.drawRoundRect(RectF(64f, 20f, 96f, 50f), 9f, 9f, glowStroke(2f, 0.22f, blurRadius = 0f))
        canvas.drawRoundRect(RectF(64f, 110f, 96f, 140f), 9f, 9f, glowStroke(2f, 0.22f, blurRadius = 0f))

        // main body outline
        canvas.drawRoundRect(RectF(50f, 48f, 110f, 112f), 17f, 17f, glowStroke(2.4f, 0.5f))

        // clip to the inner face and draw the scanning line + ECG trace inside it
        canvas.save()
        val clipRect = RectF(52f, 50f, 108f, 110f)
        canvas.clipRect(clipRect)

        val scanPulse = pulse01(phase(2400))
        val scanY = 78f + (-18f + 36f * scanPulse)
        val scanAlpha = 0.15f + 0.8f * scanPulse
        canvas.drawRect(48f, scanY, 112f, scanY + 3f, glowFill(scanAlpha, blurRadius = 3f))

        val beatAlpha = 0.5f + 0.5f * pulse01(phase(2200))
        canvas.drawPath(beatPath, glowStroke(2.2f, beatAlpha, blurRadius = 3f))

        canvas.restore()
    }
}
