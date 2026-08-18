package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.humotron.app.R

class RoundCornerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var cornerRadius: Float = 0f
    private val path = Path()
    private val rect = RectF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // uses the view's background color if set, else default
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    init {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.RoundCornerView)
            cornerRadius = ta.getDimension(R.styleable.RoundCornerView_appCornerRadius, 0f)
            ta.recycle()
        }

        // Optional: pick up backgroundTint/background color if user set one via XML "background"
        background?.let { bg ->
            if (bg is android.graphics.drawable.ColorDrawable) {
                paint.color = bg.color
            }
        }
        // We draw manually, so remove the default background to avoid double-draw
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
    }

    fun setCornerRadius(radiusPx: Float) {
        cornerRadius = radiusPx
        invalidate()
    }

    fun setFillColor(color: Int) {
        paint.color = color
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(0f, 0f, w.toFloat(), h.toFloat())
        path.reset()
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }
}