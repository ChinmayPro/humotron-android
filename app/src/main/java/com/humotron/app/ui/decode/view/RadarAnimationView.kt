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

    // Animation values
    private var dashOffset = 0f
    private var coreScale = 1.0f
    private var ring1Progress = 0.0f // 0 to 1
    private var ring2Progress = 0.5f // 0 to 1 (offset by 0.5)

    private var dashAnimator: ValueAnimator? = null
    private var coreAnimator: ValueAnimator? = null
    private var ringAnimator: ValueAnimator? = null

    private val decelerateInterpolator = DecelerateInterpolator()

    init {
        startAnimations()
    }

    private fun startAnimations() {
        // 1. Dash path effect offset animation for crawling dots (matches HTML speed: shifts 20px over 1.6s)
        dashAnimator = ValueAnimator.ofFloat(0f, 20f).apply {
            duration = 1600
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                dashOffset = animator.animatedValue as Float
                invalidate()
            }
            start()
        }

        // 2. Core pulse scaling animation
        coreAnimator = ValueAnimator.ofFloat(0.9f, 1.12f).apply {
            duration = 1100
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animator ->
                coreScale = animator.animatedValue as Float
                invalidate()
            }
            start()
        }

        // 3. Expanding rings animation
        ringAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2400
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                ring1Progress = fraction
                ring2Progress = (fraction + 0.5f) % 1.0f
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // Draw 6 dotted axis lines (length matches HTML: 49dp)
        linePaint.pathEffect = DashPathEffect(floatArrayOf(dpToPx(4f), dpToPx(6f)), -dashOffset * dpToPx(1f))
        canvas.save()
        for (i in 0 until 6) {
            canvas.drawLine(cx, cy, cx, cy - dpToPx(49f), linePaint)
            canvas.rotate(60f, cx, cy)
        }
        canvas.restore()

        // Draw expanding ring 1
        drawExpandingRing(canvas, cx, cy, ring1Progress)

        // Draw expanding ring 2
        drawExpandingRing(canvas, cx, cy, ring2Progress)

        // Draw pulsing core circle
        canvas.save()
        canvas.scale(coreScale, coreScale, cx, cy)
        canvas.drawCircle(cx, cy, dpToPx(42f), corePaint)
        canvas.restore()

        // Draw static center dot
        canvas.drawCircle(cx, cy, dpToPx(8f), centerDotPaint)
    }

    private fun drawExpandingRing(canvas: Canvas, cx: Float, cy: Float, progress: Float) {
        // Ring starts at scale 0.6 (radius 27.6dp) and expands to scale 2.4 (radius 110.4dp)
        // Alpha goes from 0.7 to 0, using decelerate interpolator (ease-out in CSS)
        val interpolatedProgress = decelerateInterpolator.getInterpolation(progress)
        val baseRadius = dpToPx(46f)
        val scale = 0.6f + interpolatedProgress * 1.8f
        val radius = baseRadius * scale

        val alpha = if (interpolatedProgress < 1.0f) {
            (0.7f * (1f - interpolatedProgress) * 255).toInt().coerceIn(0, 255)
        } else {
            0
        }

        ringPaint.alpha = alpha
        canvas.drawCircle(cx, cy, radius, ringPaint)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dashAnimator?.cancel()
        coreAnimator?.cancel()
        ringAnimator?.cancel()
    }
}
