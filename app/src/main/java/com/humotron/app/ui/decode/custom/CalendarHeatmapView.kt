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
import com.humotron.app.domain.modal.response.WorkDayStressReportDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    private var offset = 0
    private var totalDays = 30
    private var dailyScores = mapOf<Int, Int?>()
    
    fun setData(days: List<WorkDayStressReportDay>) {
        if (days.isEmpty()) return
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        
        val scores = mutableMapOf<Int, Int?>()
        var firstDayCal: Calendar? = null
        
        for (day in days) {
            try {
                val date = day.date?.let { sdf.parse(it) } ?: continue
                val cal = Calendar.getInstance().apply { time = date }
                
                if (firstDayCal == null || cal.timeInMillis < firstDayCal.timeInMillis) {
                    firstDayCal = cal
                }
                
                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                scores[dayOfMonth] = day.workdayStressScore
            } catch (e: Exception) { }
        }
        
        dailyScores = scores
        
        firstDayCal?.let {
            // Set to 1st of month
            it.set(Calendar.DAY_OF_MONTH, 1)
            offset = it.get(Calendar.DAY_OF_WEEK) - 1 // Sunday=1 -> 0 offset
            totalDays = it.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        
        invalidate()
    }

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
                    
                    val score = dailyScores[currentDay]
                    
                    if (score != null) {
                        cellPaint.color = getZoneColor(score)
                        cellPaint.style = Paint.Style.FILL
                    } else {
                        cellPaint.color = ContextCompat.getColor(context, R.color.heatmap_empty_cell)
                        cellPaint.style = Paint.Style.FILL
                    }
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
