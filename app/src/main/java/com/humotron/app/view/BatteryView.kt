package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.humotron.app.R

class BatteryView  @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var batteryLevel: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            invalidate()
        }

    var isCharging: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val borderDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_battery)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A8FF00") // light green fill
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the battery border image
        borderDrawable?.setBounds(0, 0, width, height)
        borderDrawable?.draw(canvas)

        // Calculate padding to avoid overlapping the image border
        val padding = width * 0.1f
        val innerWidth = width - 2 * padding
        val innerHeight = height - 2 * padding

        // Draw the battery level fill
        val fillWidth = (batteryLevel / 100f) * innerWidth
        val fillRect = RectF(
            padding,
            padding,
            padding + fillWidth,
            padding + innerHeight
        )
        canvas.drawRoundRect(fillRect, 12f, 12f, fillPaint)

        // Optionally, draw a bolt symbol if charging
        if (isCharging) {
            val boltPath = Path().apply {
                val centerX = width / 2f
                val centerY = height / 2f
                moveTo(centerX - 10, centerY - 20)
                lineTo(centerX + 5, centerY)
                lineTo(centerX - 5, centerY)
                lineTo(centerX + 10, centerY + 20)
                close()
            }
            val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.YELLOW
            }
            canvas.drawPath(boltPath, boltPaint)
        }
    }
}