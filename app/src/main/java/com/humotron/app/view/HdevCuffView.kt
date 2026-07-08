package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet

/**
 * Connect animation for the Smart Cuff: outward ripples, an inflating cuff
 * band arc, and an ECG trace.
 */
class HdevCuffView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HdevAnimationView(context, attrs, defStyleAttr) {

    init {
        if (attrs == null) color = Color.parseColor("#F0795E")
    }

    override val defaultStages: List<String> = listOf(
        "Searching for your Cuff",
        "Cuff found",
        "Pairing securely",
        "Calibrating the pump",
        "Connected"
    )

    // Simplified stand-in for the web version's exact SVG arc geometry
    // ("M52 54 A36 36 0 1 1 52 106") — a ~290° "C" shape open on the left,
    // reading visually as a wrapped cuff band.
    private val cuffArcRect = RectF(80f - 36f, 80f - 36f, 80f + 36f, 80f + 36f)

    private val beatPath = Path().apply {
        // Mirrors the web version's "M58 80h9l4-10 5 18 4-10 4 4h11"
        moveTo(58f, 80f)
        lineTo(67f, 80f)
        lineTo(71f, 70f)
        lineTo(76f, 88f)
        lineTo(80f, 78f)
        lineTo(84f, 82f)
        lineTo(95f, 82f)
    }

    override fun drawArt(canvas: Canvas) {
        // two staggered outward ripples
        for (delay in longArrayOf(0L, 800L)) {
            val p = phase(2400, delay)
            val eased = easeOut(p)
            val radius = 38f * (0.45f + 1.05f * eased)
            val alpha = 0.7f * (1f - eased)
            canvas.drawCircle(80f, 80f, radius, glowStroke(2f, alpha, blurRadius = 0f))
        }

        // faint arm-band backing
        canvas.drawRoundRect(RectF(70f, 30f, 90f, 130f), 10f, 10f, glowFill(0.1f))

        // inflating cuff arc — "breathes" via width + alpha rather than geometric
        // scale, since the source path's bounding box isn't centered on (80,80)
        val breathe = pulse01(phase(2600))
        val width = 11f * (0.92f + 0.16f * breathe)
        val alpha = 0.5f + 0.5f * breathe
        canvas.drawArc(cuffArcRect, 125f, 290f, false, glowStroke(width, alpha))

        // ECG trace
        val beatAlpha = 0.5f + 0.5f * pulse01(phase(2200))
        canvas.drawPath(beatPath, glowStroke(2.2f, beatAlpha, blurRadius = 3f))
    }
}
