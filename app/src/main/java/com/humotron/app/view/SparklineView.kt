package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.humotron.app.R

class SparklineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var points: List<Float> = emptyList()
    private var lineColor: Int = Color.parseColor("#5FB7C4") // Default: var(--cool)

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    init {
        lineColor = ContextCompat.getColor(context, R.color.deep_dives_cool)
    }

    fun setData(newPoints: List<Float>) {
        this.points = newPoints
        invalidate()
    }

    fun setLineColor(color: Int) {
        this.lineColor = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (points.size < 2) return

        val width = width.toFloat()
        val height = height.toFloat()

        // Padding to prevent dot or line thickness from being clipped
        val paddingLeft = 12f
        val paddingRight = 12f
        val paddingTop = 12f
        val paddingBottom = 12f

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        var minVal = points.minOrNull() ?: 0f
        var maxVal = points.maxOrNull() ?: 1f
        if (minVal == maxVal) {
            minVal -= 1f
            maxVal += 1f
        }
        val valRange = maxVal - minVal

        val stepX = graphWidth / (points.size - 1)
        val path = Path()
        val fillPath = Path()

        var lastX = 0f
        var lastY = 0f

        for (i in points.indices) {
            val valY = points[i]
            val normY = (valY - minVal) / valRange
            val px = paddingLeft + i * stepX
            val py = paddingTop + graphHeight - normY * graphHeight

            if (i == 0) {
                path.moveTo(px, py)
                fillPath.moveTo(px, py)
            } else {
                path.lineTo(px, py)
                fillPath.lineTo(px, py)
            }

            if (i == points.size - 1) {
                lastX = px
                lastY = py
            }
        }

        // Complete fill path down to the bottom
        fillPath.lineTo(lastX, paddingTop + graphHeight)
        fillPath.lineTo(paddingLeft, paddingTop + graphHeight)
        fillPath.close()

        // Setup fill gradient
        // 28% opacity of line color for top, 0% opacity for bottom
        val startColor = (0x47 shl 24) or (lineColor and 0x00FFFFFF) // ~28% opacity
        val endColor = 0x00FFFFFF and lineColor // 0% opacity
        fillPaint.shader = LinearGradient(
            0f, paddingTop,
            0f, paddingTop + graphHeight,
            startColor, endColor,
            Shader.TileMode.CLAMP
        )

        // Draw fill first
        canvas.drawPath(fillPath, fillPaint)

        // Draw stroke line
        linePaint.color = lineColor
        canvas.drawPath(path, linePaint)

        // Draw end circle dot
        dotPaint.color = lineColor
        canvas.drawCircle(lastX, lastY, 9f, dotPaint)
    }
}
