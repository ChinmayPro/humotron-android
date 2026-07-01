package com.humotron.app.ui.decode.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import android.animation.ValueAnimator
import android.graphics.PathMeasure
import android.view.animation.DecelerateInterpolator

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    class Series(
        val name: String,
        val points: List<Float>,
        val colorHex: String
    )

    private var seriesList: List<Series> = emptyList()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#859390") // var(--ink3)
        textSize = 28f
        try {
            val tf = ResourcesCompat.getFont(context, com.humotron.app.R.font.manrope_regular)
            if (tf != null) {
                typeface = tf
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val legendDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#12ffffff") // very light grid lines
        strokeWidth = 2f
    }

    private var animProgress = 0f
    private var animator: ValueAnimator? = null

    fun setSeries(series: List<Series>) {
        this.seriesList = series
        startAnimation()
    }

    private fun startAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                animProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (seriesList.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()

        val paddingLeft = 40f
        val paddingRight = 40f
        val paddingTop = 40f
        val paddingBottom = 80f // leave space for legend

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        // Compute min and max across all series
        var minVal = Float.MAX_VALUE
        var maxVal = Float.MIN_VALUE
        var maxPointsCount = 0

        for (series in seriesList) {
            if (series.points.size > maxPointsCount) {
                maxPointsCount = series.points.size
            }
            for (p in series.points) {
                if (p < minVal) minVal = p
                if (p > maxVal) maxVal = p
            }
        }

        if (minVal == maxVal) {
            minVal -= 1f
            maxVal += 1f
        }

        val valRange = maxVal - minVal

        // Draw horizontal grid lines (top, middle, bottom)
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft + graphWidth, paddingTop, gridPaint)
        canvas.drawLine(paddingLeft, paddingTop + graphHeight / 2, paddingLeft + graphWidth, paddingTop + graphHeight / 2, gridPaint)
        canvas.drawLine(paddingLeft, paddingTop + graphHeight, paddingLeft + graphWidth, paddingTop + graphHeight, gridPaint)

        // Draw line paths
        val path = Path()
        for (series in seriesList) {
            if (series.points.size < 2) continue

            linePaint.color = Color.parseColor(series.colorHex)
            path.reset()

            val stepX = graphWidth / (series.points.size - 1)
            for (i in series.points.indices) {
                val valY = series.points[i]
                val normY = (valY - minVal) / valRange
                val px = paddingLeft + i * stepX
                val py = paddingTop + graphHeight - normY * graphHeight

                if (i == 0) {
                    path.moveTo(px, py)
                } else {
                    path.lineTo(px, py)
                }
            }
            val pathMeasure = PathMeasure(path, false)
            val partialPath = Path()
            pathMeasure.getSegment(0f, pathMeasure.length * animProgress, partialPath, true)
            canvas.drawPath(partialPath, linePaint)
        }

        // Draw legends at the bottom
        var currentX = paddingLeft
        val legendY = height - 20f
        for (series in seriesList) {
            // Draw colored rounded square
            legendDotPaint.color = Color.parseColor(series.colorHex)
            val rectSize = 16f
            val rx = 4f
            val ry = 4f
            val left = currentX
            val top = legendY - 18f
            canvas.drawRoundRect(left, top, left + rectSize, top + rectSize, rx, ry, legendDotPaint)
            
            // Draw name
            canvas.drawText(series.name, currentX + rectSize + 12f, legendY, textPaint)
            
            val textWidth = textPaint.measureText(series.name)
            currentX += rectSize + 12f + textWidth + 60f // gap between items
        }
    }
}
