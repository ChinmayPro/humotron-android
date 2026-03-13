package com.humotron.app.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View


class CustomPagerIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var pageCount = 4
    private var currentPage = 0
    private var textLabel = "Dashboard"
    private var progress = 0f

    private val dotRadius = 16f
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GRAY }
    private val selectedDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(76, 217, 217, 217)
        strokeWidth = 8f
    }
    private val progressLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(127, 255, 255, 255)
        textSize = 24f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val labelBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(76, 255, 255, 255)
    }
    val paddingH = 20f
    val paddingV = 10f

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (pageCount < 2) return

        val centerY = height / 2f
        val labelWidth = textPaint.measureText(textLabel)
        val labelMargin = 24f
        val rightPadding = 32f
        val startX = 32f
        val endX = width - labelWidth - labelMargin - rightPadding
        val availableWidth = endX - startX

        val dotCount = pageCount - 1
        val segmentSpacing = availableWidth / dotCount

        val points = mutableListOf<PointF>()
        for (i in 0 until dotCount) {
            val x = startX + i * segmentSpacing
            points.add(PointF(x, centerY))
        }

        val labelX = startX + dotCount * segmentSpacing
        val labelY = centerY + textPaint.textSize / 3f

        // --- Draw background lines
        for (i in 0 until points.size - 1) {
            canvas.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, linePaint)
        }
        // Last line to label
        canvas.drawLine(points.last().x, centerY, labelX - paddingH, centerY, linePaint)

        // --- Draw progress line
        val maxProgress = pageCount - 1f
        val clampedProgress = progress.coerceIn(0f, maxProgress)
        val fullLength = segmentSpacing * clampedProgress

        var remainingLength = fullLength
        for (i in 0 until points.size) {
            val start = points[i]
            val end = if (i == points.lastIndex) PointF(labelX, centerY) else points[i + 1]
            val segmentLength = end.x - start.x
            if (remainingLength <= 0f) break

            val drawToX = if (remainingLength >= segmentLength) end.x else start.x + remainingLength
            canvas.drawLine(start.x, start.y, drawToX, start.y, progressLinePaint)
            remainingLength -= segmentLength
        }

        // --- Draw dots
        points.forEachIndexed { index, point ->
            val isSelected = index <= currentPage // now last 2 dots can also be "selected"
            val paint = if (isSelected) selectedDotPaint else dotPaint
            canvas.drawCircle(point.x, point.y, dotRadius, paint)
        }

        // --- Draw label background (instead of line)

        val labelHeight = textPaint.textSize + paddingV * 2
        val labelRect = RectF(
            labelX - paddingH,
            centerY - labelHeight / 2f,
            labelX + labelWidth + paddingH,
            centerY + labelHeight / 2f
        )

        if (currentPage == pageCount - 1) {
            labelBackgroundPaint.color = Color.WHITE
            canvas.drawRoundRect(labelRect, 24f, 24f, labelBackgroundPaint)
            textPaint.color = Color.BLACK
            canvas.drawText(textLabel, labelX, labelY, textPaint)
        } else {
            labelBackgroundPaint.color = Color.argb(76, 255, 255, 255)
            canvas.drawRoundRect(labelRect, 24f, 24f, labelBackgroundPaint)
            textPaint.color = Color.argb(127, 255, 255, 255)
            canvas.drawText(textLabel, labelX, labelY, textPaint)
        }


        // --- Draw label text


    }

    fun setPageCount(count: Int) {
        pageCount = count
        invalidate()
    }

    fun setCurrentPage(index: Int) {
        currentPage = index
        invalidate()
    }

    fun setLabel(text: String) {
        textLabel = text
        invalidate()
    }

    fun setProgress(value: Float) {
        progress = value
        invalidate()
    }
}