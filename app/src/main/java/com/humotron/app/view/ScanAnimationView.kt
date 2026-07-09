package com.humotron.app.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.humotron.app.R
import kotlin.math.min

class ScanAnimationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    // --- Properties ---
    private var scanColor: Int = Color.parseColor("#F0795E")
    private var scanProgressBackgroundColor: Int = Color.parseColor("#2A3538")
    private var scanValueColor: Int = Color.WHITE
    private var scanUnitColor: Int = Color.parseColor("#6F7E7D")

    private var scanValue: String = "0"
    private var scanValueSuffix: String = "%"
    private var scanUnit: String = "SCANNING"

    // --- Progress Properties ---
    private var progressMargin: Float = 0f
    private var currentProgress: Float = 0f
    private var ringSweepAngle: Float = 0f

    // --- Paints ---
    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val ringBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val valuePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; isFakeBoldText = true }
    private val suffixPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.LEFT; isFakeBoldText = true }
    private val unitPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; isFakeBoldText = true }

    // --- Animation values ---
    private var sweepAngle = 0f
    private var pulseFraction = 0f

    // --- Animators ---
    private var sweepAnimator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null
    private var progressAnimator: ValueAnimator? = null

    // --- Geometry ---
    private var cx = 0f
    private var cy = 0f
    private var maxRadius = 0f
    private val ringBounds = RectF()

    init {
        setupAttributes(attrs)
        setupPaints()
        // Initialize progress angle based on starting progress
        ringSweepAngle = (currentProgress / 100f) * 360f
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ScanAnimationView)

            scanColor = typedArray.getColor(R.styleable.ScanAnimationView_scanColor, scanColor)
            scanProgressBackgroundColor = typedArray.getColor(
                R.styleable.ScanAnimationView_scanProgressBackgroundColor,
                scanProgressBackgroundColor
            )
            scanValueColor =
                typedArray.getColor(R.styleable.ScanAnimationView_scanValueColor, scanValueColor)
            scanUnitColor =
                typedArray.getColor(R.styleable.ScanAnimationView_scanUnitColor, scanUnitColor)

            scanValue = typedArray.getString(R.styleable.ScanAnimationView_scanValue) ?: scanValue
            scanValueSuffix = typedArray.getString(R.styleable.ScanAnimationView_scanValueSuffix)
                ?: scanValueSuffix
            scanUnit = typedArray.getString(R.styleable.ScanAnimationView_scanUnit) ?: scanUnit

            currentProgress = typedArray.getFloat(R.styleable.ScanAnimationView_scanProgress, 0f)
            progressMargin =
                typedArray.getDimension(R.styleable.ScanAnimationView_scanProgressMargin, 0f)

            typedArray.recycle()
        }
    }

    private fun setupPaints() {
        pulsePaint.strokeWidth = dpToPx(1.5f)

        ringPaint.strokeWidth = dpToPx(6f)
        ringPaint.color = scanColor

        ringBackgroundPaint.strokeWidth = dpToPx(6f)
        ringBackgroundPaint.color = scanProgressBackgroundColor

        valuePaint.color = scanValueColor
        valuePaint.textSize = dpToPx(36f)

        suffixPaint.color = Color.parseColor("#859390")
        suffixPaint.textSize = dpToPx(15f)

        unitPaint.color = scanUnitColor
        unitPaint.textSize = dpToPx(10f)
        unitPaint.letterSpacing = 0.1f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f
        maxRadius = min(w, h) / 2f * 0.9f // 10% padding

        // Setup Radar Sweep Gradient
        val colors = intArrayOf(
            Color.argb(0, Color.red(scanColor), Color.green(scanColor), Color.blue(scanColor)),
            Color.argb(0, Color.red(scanColor), Color.green(scanColor), Color.blue(scanColor)),
            Color.argb(
                (255 * 0.32).toInt(),
                Color.red(scanColor),
                Color.green(scanColor),
                Color.blue(scanColor)
            ),
            Color.argb(0, Color.red(scanColor), Color.green(scanColor), Color.blue(scanColor))
        )
        val positions = floatArrayOf(0f, 1f - (65f / 360f), 0.99f, 1f)
        sweepPaint.shader = SweepGradient(cx, cy, colors, positions)

        // Calculate Bounds for Progress Ring
        val ringRadius = maxRadius - progressMargin
        ringBounds.set(cx - ringRadius, cy - ringRadius, cx + ringRadius, cy + ringRadius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw Radar Sweep
        canvas.save()
        canvas.rotate(sweepAngle, cx, cy)
        canvas.drawCircle(cx, cy, maxRadius, sweepPaint)
        canvas.restore()

        // 2. Draw Pulses
        val pulseBaseRadius = maxRadius * (45f / 110f)
        val phases = listOf(0f, 0.33f, 0.66f)

        for (phaseOffset in phases) {
            val currentPhase = (pulseFraction + phaseOffset) % 1f
            val scale = 0.4f + (2.6f - 0.4f) * currentPhase
            val alpha = (255 * 0.55f * (1f - currentPhase)).toInt()

            pulsePaint.color = scanColor
            pulsePaint.alpha = alpha
            canvas.drawCircle(cx, cy, pulseBaseRadius * scale, pulsePaint)
        }

        // 3. Draw Progress Rings
        canvas.drawArc(ringBounds, 0f, 360f, false, ringBackgroundPaint) // Background Track
        canvas.drawArc(ringBounds, -90f, ringSweepAngle, false, ringPaint) // Active Progress

        // 4. Draw Center Text
        val textYOffset = (valuePaint.descent() + valuePaint.ascent()) / 2
        val valueWidth = valuePaint.measureText(scanValue)

        canvas.drawText(
            scanValue,
            cx - (valueWidth * 0.1f),
            cy - textYOffset - dpToPx(8f),
            valuePaint
        )
        /*canvas.drawText(
            scanValueSuffix,
            cx + (valueWidth / 2) + dpToPx(2f),
            cy - textYOffset - dpToPx(8f),
            suffixPaint
        )*/
        canvas.drawText(
            scanValueSuffix,
            cx + (valueWidth / 2),
            cy - textYOffset - dpToPx(8f),
            suffixPaint
        )
        canvas.drawText(scanUnit, cx, cy + dpToPx(20f), unitPaint)
    }

    // --- Lifecycle ---

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startBackgroundAnimations()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimations()
    }

    private fun startBackgroundAnimations() {
        // Continuous Radar Sweep
        sweepAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2600
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                sweepAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }

        // Continuous Pulses
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2800
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                pulseFraction = it.animatedValue as Float
            }
            start()
        }
    }

    private fun stopAnimations() {
        sweepAnimator?.cancel()
        pulseAnimator?.cancel()
        progressAnimator?.cancel()
    }

    // --- Public API ---

    /**
     * Animates the progress continuously over a specific duration.
     * @param durationMillis Total time for the animation (e.g., 60000L for 60 seconds)
     * @param targetProgress The progress percentage to reach (default is 100f)
     */
    fun animateProgressContinuously(durationMillis: Long, targetProgress: Float = 100f) {
        val safeTarget = targetProgress.coerceIn(0f, 100f)

        progressAnimator?.cancel()

        progressAnimator = ValueAnimator.ofFloat(currentProgress, safeTarget).apply {
            duration = durationMillis
            interpolator = LinearInterpolator() // Perfectly steady speed

            addUpdateListener { animation ->
                currentProgress = animation.animatedValue as Float
                ringSweepAngle = (currentProgress / 100f) * 360f

                // Automatically sync the center text
                scanValue = currentProgress.toInt().toString()

                invalidate()
            }
            start()
        }
    }

    /**
     * Sets a specific progress value instantly or with a quick smooth transition.
     */
    fun setProgress(progress: Float, animate: Boolean = true) {
        val targetProgress = progress.coerceIn(0f, 100f)

        progressAnimator?.cancel()

        if (animate) {
            progressAnimator = ValueAnimator.ofFloat(currentProgress, targetProgress).apply {
                duration = 600
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    currentProgress = it.animatedValue as Float
                    ringSweepAngle = (currentProgress / 100f) * 360f
                    invalidate()
                }
                start()
            }
        } else {
            currentProgress = targetProgress
            ringSweepAngle = (currentProgress / 100f) * 360f
            invalidate()
        }
    }

    fun setProgressMargin(marginPx: Float) {
        progressMargin = marginPx
        requestLayout()
        invalidate()
    }

    fun setScanColor(colorRes: Int) {
        scanColor = ContextCompat.getColor(context, colorRes)
        setupPaints()
        invalidate()
    }

    fun setProgressBackgroundColor(colorRes: Int) {
        scanProgressBackgroundColor = ContextCompat.getColor(context, colorRes)
        setupPaints()
        invalidate()
    }

    fun setScanData(value: String, suffix: String = "%", unit: String = "SCANNING") {
        this.scanValue = value
        this.scanValueSuffix = suffix
        this.scanUnit = unit
        invalidate()
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}