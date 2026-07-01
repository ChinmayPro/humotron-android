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

class DonutProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0f
    private var progressColor: Int = ContextCompat.getColor(context, R.color.deep_dives_watch)
    private var trackColor: Int = Color.parseColor("#14FFFFFF") // 8% white
    
    private val strokeWidth = 24f

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.strokeWidth = this@DonutProgressView.strokeWidth
        color = trackColor
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.strokeWidth = this@DonutProgressView.strokeWidth
        strokeCap = Paint.Cap.ROUND
        color = progressColor
    }

    private val rectF = RectF()

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 100f)
        invalidate()
    }
    
    fun setProgressColor(colorRes: Int) {
        progressColor = colorRes
        progressPaint.color = progressColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = strokeWidth / 2f
        rectF.set(padding, padding, width - padding, height - padding)

        // Draw background track
        canvas.drawArc(rectF, 0f, 360f, false, trackPaint)

        // Draw progress arc
        val sweepAngle = (progress / 100f) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)
    }
}
