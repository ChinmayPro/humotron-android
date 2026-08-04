package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class HeroGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gridSpacingPx: Float = 34f * context.resources.displayMetrics.density

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * context.resources.displayMetrics.density
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            val cx = w / 2f
            val cy = h * 0.40f
            val radius = max(w.toFloat(), h.toFloat()) * 0.46f

            val radialColors = intArrayOf(
                Color.argb(58, 95, 183, 196), // Balanced cyan grid lines at center
                Color.argb(20, 95, 183, 196), // Mid cyan fading
                Color.argb(0, 95, 183, 196)   // Transparent at edges
            )
            val radialStops = floatArrayOf(0.0f, 0.50f, 1.0f)

            val radialShader = RadialGradient(
                cx, cy, radius,
                radialColors, radialStops,
                Shader.TileMode.CLAMP
            )

            val linearShader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(
                    Color.argb(0, 95, 183, 196),
                    Color.argb(255, 95, 183, 196),
                    Color.argb(255, 95, 183, 196),
                    Color.argb(0, 95, 183, 196)
                ),
                floatArrayOf(0.0f, 0.20f, 0.80f, 1.0f),
                Shader.TileMode.CLAMP
            )

            paint.shader = ComposeShader(radialShader, linearShader, PorterDuff.Mode.DST_IN)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        if (w <= 0 || h <= 0) return

        val halfW = w / 2f
        val halfH = h * 0.40f

        // Draw vertical grid lines expanding outwards from center
        var xRight = halfW
        while (xRight <= w + gridSpacingPx) {
            canvas.drawLine(xRight, 0f, xRight, h, paint)
            xRight += gridSpacingPx
        }
        var xLeft = halfW - gridSpacingPx
        while (xLeft >= -gridSpacingPx) {
            canvas.drawLine(xLeft, 0f, xLeft, h, paint)
            xLeft -= gridSpacingPx
        }

        // Draw horizontal grid lines expanding outwards from center
        var yDown = halfH
        while (yDown <= h + gridSpacingPx) {
            canvas.drawLine(0f, yDown, w, yDown, paint)
            yDown += gridSpacingPx
        }
        var yUp = halfH - gridSpacingPx
        while (yUp >= -gridSpacingPx) {
            canvas.drawLine(0f, yUp, w, yUp, paint)
            yUp -= gridSpacingPx
        }
    }
}
