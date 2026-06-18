package com.humotron.app.ui.decode.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var percentage: Float = 0f
    private var centerValue: String = ""
    private var centerLabel: String = ""

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
    }

    private val textValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 64f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val textLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#859390") // var(--ink3)
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private val rectF = RectF()

    fun setData(pct: Float, valueText: String, labelText: String) {
        this.percentage = pct
        this.centerValue = valueText
        this.centerLabel = labelText
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val size = Math.min(width, height)
        val stroke = ringPaint.strokeWidth
        val radius = (size - stroke * 2) / 2f
        val cx = width / 2f
        val cy = height / 2f

        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // Draw background ring
        ringPaint.color = Color.parseColor("#14ffffff") // transparent white
        canvas.drawCircle(cx, cy, radius, ringPaint)

        // Draw active arc
        ringPaint.color = Color.parseColor("#B2F042") // brand lime
        val sweepAngle = (percentage / 100f) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, ringPaint)

        // Draw value text
        val valueOffset = (textValuePaint.descent() + textValuePaint.ascent()) / 2f
        if (centerLabel.isEmpty()) {
            canvas.drawText(centerValue, cx, cy - valueOffset, textValuePaint)
        } else {
            canvas.drawText(centerValue, cx, cy - valueOffset - 16f, textValuePaint)
            val labelOffset = (textLabelPaint.descent() + textLabelPaint.ascent()) / 2f
            canvas.drawText(centerLabel, cx, cy - labelOffset + 36f, textLabelPaint)
        }
    }
}
