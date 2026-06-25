package com.humotron.app.ui.decode.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.humotron.app.R

class CalendarHeatmapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 30f
        isFakeBoldText = true
    }

    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.deep_dives_ink4)
        textAlign = Paint.Align.CENTER
        textSize = 28f
        isFakeBoldText = true
    }

    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rect = RectF()
    private val cornerRadius = 20f
    private val gap = 16f
    
    private val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    // Mock data for 30 days starting from Wednesday (offset 3)
    private val offset = 3
    private val totalDays = 30

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cellWidth = (width - gap * 6) / 7f
        val cellHeight = cellWidth

        // Draw headers
        for (i in 0 until 7) {
            val x = i * (cellWidth + gap) + cellWidth / 2f
            val y = 40f
            canvas.drawText(daysOfWeek[i], x, y, headerPaint)
        }

        // Draw cells
        val startY = 80f
        var currentDay = 1
        
        for (row in 0 until 6) {
            for (col in 0 until 7) {
                val index = row * 7 + col
                if (index >= offset && currentDay <= totalDays) {
                    val x = col * (cellWidth + gap)
                    val y = startY + row * (cellHeight + gap)
                    
                    rect.set(x, y, x + cellWidth, y + cellHeight)
                    
                    // Mock logic to perfectly match the prototype screenshot for April
                    val isWeekend = col == 0 || col == 6
                    val score = when (currentDay) {
                        1 -> 45
                        2, 3 -> 50
                        else -> if (isWeekend) 60 else 30
                    }
                    
                    cellPaint.color = getZoneColor(score)
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cellPaint)
                    
                    val textY = y + cellHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                    canvas.drawText(currentDay.toString(), x + cellWidth / 2f, textY, textPaint)
                    
                    currentDay++
                }
            }
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val cellWidth = (width - gap * 6) / 7f
        val height = (80f + 6 * (cellWidth + gap)).toInt()
        setMeasuredDimension(width, height)
    }

    private fun getZoneColor(v: Int): Int {
        return when {
            v >= 70 -> ContextCompat.getColor(context, R.color.deep_dives_attention)
            v >= 58 -> ContextCompat.getColor(context, R.color.insights_zone_challenged)
            v >= 48 -> ContextCompat.getColor(context, R.color.deep_dives_watch)
            v >= 40 -> Color.parseColor("#9FBF6A")
            else -> ContextCompat.getColor(context, R.color.deep_dives_lime)
        }
    }
}
