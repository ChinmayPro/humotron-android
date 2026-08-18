package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class ProgressRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokePx = 16f

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokePx
        color = Color.parseColor("#1F2E2B")
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokePx
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#C4F23E")
    }

    private val oval = RectF()

    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val padding = strokePx / 2f
        oval.set(padding, padding, width.toFloat() - padding, height.toFloat() - padding)

        // Draw dark background ring
        canvas.drawOval(oval, trackPaint)

        // Draw Lime progress arc from 12 o'clock (-90 degrees) clockwise
        if (progress > 0) {
            val sweepAngle = 360f * (progress / 100f)
            canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint)
        }
    }
}
