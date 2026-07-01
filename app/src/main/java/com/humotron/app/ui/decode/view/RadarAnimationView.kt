package com.humotron.app.ui.decode.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.humotron.app.R

class RadarAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val limeColor = ContextCompat.getColor(context, R.color.insights_green)

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = limeColor
        strokeWidth = dpToPx(1f)
        style = Paint.Style.STROKE
        alpha = 128 // 0.5 opacity
    }

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = limeColor
        style = Paint.Style.FILL
        alpha = 31 // ~0.12 opacity
    }

    private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = limeColor
        style = Paint.Style.FILL
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = limeColor
        strokeWidth = dpToPx(1.5f)
        style = Paint.Style.STROKE
    }

    private val outerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.insights_tag_grey)
        style = Paint.Style.FILL
    }

    // Animation values
    var radiusDp: Float = 68f
        set(value) {
            field = value
            invalidate()
        }
    private var dashOffset = 0f
    private var coreScale = 1.0f

    private var dashAnimator: ValueAnimator? = null
    private var coreAnimator: ValueAnimator? = null

    private val decelerateInterpolator = DecelerateInterpolator()

    init {
        startAnimations()
    }

    private fun startAnimations() {
        // 1. Dash path effect offset animation for crawling dots (matches HTML speed: shifts 20px over 1.7s)
        dashAnimator = ValueAnimator.ofFloat(0f, 20f).apply {
            duration = 1700
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                dashOffset = animator.animatedValue as Float
                invalidate()
            }
            start()
        }

        // 2. Core pulse scaling animation (matches HTML: 0.92 to 1.1 over 2.4s total)
        coreAnimator = ValueAnimator.ofFloat(0.92f, 1.10f).apply {
            duration = 1200
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animator ->
                coreScale = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // Draw 6 dotted axis lines (length matches HTML spacing visually: 68dp)
        linePaint.pathEffect = DashPathEffect(floatArrayOf(dpToPx(4f), dpToPx(6f)), -dashOffset * dpToPx(1f))
        canvas.save()
        for (i in 0 until 6) {
            canvas.drawLine(cx, cy, cx, cy - dpToPx(radiusDp), linePaint)
            canvas.rotate(60f, cx, cy)
        }
        canvas.restore()

        // Draw pulsing core circle
        canvas.save()
        canvas.scale(coreScale, coreScale, cx, cy)
        canvas.drawCircle(cx, cy, dpToPx(34f), corePaint)
        canvas.restore()

        // Draw static center dot
        canvas.drawCircle(cx, cy, dpToPx(8f), centerDotPaint)

        // Draw 6 outer dots at the end of the lines
        canvas.save()
        outerDotPaint.alpha = 255
        for (i in 0 until 6) {
            canvas.drawCircle(cx, cy - dpToPx(radiusDp), dpToPx(4f), outerDotPaint)
            canvas.rotate(60f, cx, cy)
        }
        canvas.restore()
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dashAnimator?.cancel()
        coreAnimator?.cancel()
    }
}
