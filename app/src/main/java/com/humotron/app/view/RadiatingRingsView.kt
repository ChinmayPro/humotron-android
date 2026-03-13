package com.humotron.app.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class RadiatingRingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
    }

    private var progress = 0f
    private var isAnimating = false
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isAnimating) {
            return
        }

        val centerX = width / 2f
        val centerY = height / 2f

        val maxRadius = centerX.coerceAtMost(centerY) * 0.9f

        for (i in 0..2) {
            val factor = (progress + i * 0.33f) % 1f

            val radius = factor * maxRadius
            val alpha = ((1 - factor) * 255).toInt().coerceIn(0, 255)
            val strokeWidth = 2f + 5f * factor

            ringPaint.alpha = alpha
            ringPaint.strokeWidth = strokeWidth

            val rect = RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )

            // Draw partial arc (not full circle)
            canvas.drawArc(rect, 120f, 305f, false, ringPaint)
        }
    }

    // ✅ Start animation
    fun startAnimation() {
        if (!isAnimating) {
            animator.start()
            isAnimating = true
        }
    }

    // ✅ Stop animation
    fun stopAnimation() {
        if (isAnimating) {
            animator.cancel()
            isAnimating = false
            invalidate() // Clear last frame
        }
    }
}