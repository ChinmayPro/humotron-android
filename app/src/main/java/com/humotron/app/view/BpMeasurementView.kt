package com.humotron.app.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import com.humotron.app.R
import kotlin.math.round

/**
 * Ported from the HTML blood-pressure measurement screen's `.bp-track` /
 * `.bp-fill` bar (linear-gradient(180deg, #9be84f, #5fbf6e), animated height)
 * plus the `.bp-axis` step labels running down the left side.
 *
 * Range / stepping:
 *   app:minValue="10"
 *   app:maxValue="220"
 *   app:step="30"        // also controls axis label spacing — see below
 *
 * Colors / text:
 *   app:trackColor="#11FFFFFF"
 *   app:fillColorStart="#9BE84F"   // top of the fill, matches the CSS gradient's first stop
 *   app:fillColorEnd="#5FBF6E"     // bottom of the fill, matches the CSS gradient's second stop
 *   app:textColor="#FFFFFF"        // used for both the value label and the axis labels
 *   app:valueTextSize="32sp"       // size of the big current-value label
 *   app:stepTextSize="10sp"        // size of the left-side axis labels
 *   app:minMaxLabelTopMarginBP="4dp"
 *   app:minMaxLabelBottomMarginBP="4dp"
 *   app:barLabelGapBP="8dp"
 *
 * Value — always animates smoothly to the new target:
 *   bpView.setValue(30f)
 *   bpView.setValue(30f, animate = false)                 // jump instantly
 *   bpView.setValue(30f, animate = true, duration = 600)  // custom duration
 *
 * Colors can also be changed at runtime, by property or method:
 *   bpView.trackColor = Color.DKGRAY
 *   bpView.setFillGradient(Color.parseColor("#9BE84F"), Color.parseColor("#5FBF6E"))
 *   bpView.textColor = Color.WHITE
 *
 * Left-side step labels (matches the HTML's .bp-axis, which showed
 * 10/40/70/.../220 down the side): every value from [minValue] to
 * [maxValue] in increments of [step] is drawn as a label to the left of
 * the track, at the height corresponding to that value. Set
 * [showStepLabels] = false to hide them.
 */
class BpMeasurementView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_MIN = 0f
        private const val DEFAULT_MAX = 240f
        private const val DEFAULT_STEP = 30f
        private const val DEFAULT_ANIM_DURATION = 400L
        // Safety cap so a very small `step` can't generate thousands of labels.
        private const val MAX_STEP_LABELS = 200
    }

    // ---------------------------------------------------------------
    // Range / step
    // ---------------------------------------------------------------

    var minValue: Float = DEFAULT_MIN
        set(v) { field = v; invalidate() }

    var maxValue: Float = DEFAULT_MAX
        set(v) { field = v; invalidate() }

    /**
     * Values passed to [setValue] are snapped to the nearest multiple of this,
     * and it's also the spacing used to generate the left-side axis labels
     * (see [showStepLabels]).
     */
    var step: Float = DEFAULT_STEP
        set(v) { field = v; invalidate() }

    // ---------------------------------------------------------------
    // Colors
    // ---------------------------------------------------------------

    @get:ColorInt
    var trackColor: Int = Color.argb(15, 255, 255, 255)
        set(@ColorInt v) { field = v; invalidate() }

    @get:ColorInt
    var fillColorStart: Int = Color.parseColor("#9BE84F")
        private set

    @get:ColorInt
    var fillColorEnd: Int = Color.parseColor("#5FBF6E")
        private set

    /** Used for both the current-value label and the left-side step labels. */
    @get:ColorInt
    var textColor: Int = Color.WHITE
        set(@ColorInt v) { field = v; invalidate() }

    var showValueLabel: Boolean = true
        set(v) { field = v; invalidate() }

    var showStepLabels: Boolean = true
        set(v) { field = v; invalidate() }

    /** Sets both gradient stops at once (property form isn't available since it's two values). */
    fun setFillGradient(@ColorInt start: Int, @ColorInt end: Int) {
        fillColorStart = start
        fillColorEnd = end
        invalidate()
    }

    // NOTE: no explicit setTrackColor()/setTextColor() methods here — Kotlin
    // already generates those JVM setters automatically from the `var`
    // properties above (trackColor, textColor). Adding hand-written ones with
    // the same name+signature causes a "platform declaration clash".
    // Use the property directly: view.trackColor = x / view.textColor = x
    // (Java callers can still call view.setTrackColor(x) / view.setTextColor(x) —
    // that's the auto-generated setter.)

    // ---------------------------------------------------------------
    // Value — always animates smoothly unless animate=false
    // ---------------------------------------------------------------

    private var displayedValue: Float = minValue
    private var targetValue: Float = minValue
    private var valueAnimator: ValueAnimator? = null

    @JvmOverloads
    fun setValue(newValue: Float, animate: Boolean = true, duration: Long = DEFAULT_ANIM_DURATION) {
        val snapped = snapToStep(newValue.coerceIn(minValue, maxValue))
        targetValue = snapped
        valueAnimator?.cancel()
        if (!animate) {
            displayedValue = snapped
            invalidate()
            return
        }
        valueAnimator = ValueAnimator.ofFloat(displayedValue, snapped).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                displayedValue = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun getValue(): Float = targetValue

    private fun snapToStep(v: Float): Float {
        if (step <= 0f) return v
        return round(v / step) * step
    }

    // ---------------------------------------------------------------
    // Paints
    // ---------------------------------------------------------------

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val stepTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
    }
    private val clipPath = Path()

    private var valueTextSizePx: Float = spToPx(32f)
    private var stepTextSizePx: Float = spToPx(10f)
    private var minMaxLabelTopMarginPx: Float = dpToPx(4f)
    private var minMaxLabelBottomMarginPx: Float = dpToPx(4f)
    private var barLabelGapPx: Float = dpToPx(8f)

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.BpMeasurementView, defStyleAttr, 0)
            try {
                minValue = ta.getFloat(R.styleable.BpMeasurementView_minValueBP, DEFAULT_MIN)
                maxValue = ta.getFloat(R.styleable.BpMeasurementView_maxValueBP, DEFAULT_MAX)
                step = ta.getFloat(R.styleable.BpMeasurementView_stepBP, DEFAULT_STEP)
                trackColor = ta.getColor(R.styleable.BpMeasurementView_trackColorBP, trackColor)
                fillColorStart = ta.getColor(R.styleable.BpMeasurementView_fillColorStart, fillColorStart)
                fillColorEnd = ta.getColor(R.styleable.BpMeasurementView_fillColorEnd, fillColorEnd)
                textColor = ta.getColor(R.styleable.BpMeasurementView_textColorBP, textColor)
                showValueLabel = ta.getBoolean(R.styleable.BpMeasurementView_showValueLabel, true)
                showStepLabels = ta.getBoolean(R.styleable.BpMeasurementView_showStepLabels, true)
                valueTextSizePx = ta.getDimension(R.styleable.BpMeasurementView_valueTextSize, valueTextSizePx)
                stepTextSizePx = ta.getDimension(R.styleable.BpMeasurementView_stepTextSize, stepTextSizePx)
                minMaxLabelTopMarginPx = ta.getDimension(
                    R.styleable.BpMeasurementView_minMaxLabelTopMarginBP,
                    minMaxLabelTopMarginPx
                )
                minMaxLabelBottomMarginPx = ta.getDimension(
                    R.styleable.BpMeasurementView_minMaxLabelBottomMarginBP,
                    minMaxLabelBottomMarginPx
                )
                barLabelGapPx = ta.getDimension(
                    R.styleable.BpMeasurementView_barLabelGapBP,
                    barLabelGapPx
                )
            } finally {
                ta.recycle()
            }
        }
        displayedValue = minValue
        targetValue = minValue
    }

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    // ---------------------------------------------------------------
    // Step labels (matches the HTML's .bp-axis)
    // ---------------------------------------------------------------

    /** minValue..maxValue in increments of [step], capped to avoid runaway label counts. */
    private fun stepValues(): List<Float> {
        if (step <= 0f || maxValue <= minValue) return emptyList()
        val values = mutableListOf<Float>()
        var v = minValue
        var guard = 0
        while (v <= maxValue + 0.0001f && guard < MAX_STEP_LABELS) {
            values.add(v)
            v += step
            guard++
        }
        if (values.isEmpty() || values.last() < maxValue - 0.0001f) {
            if (values.size < MAX_STEP_LABELS) values.add(maxValue)
        }
        return values
    }

    private fun widestStepLabelWidth(values: List<Float>): Float {
        stepTextPaint.textSize = stepTextSizePx
        var widest = 0f
        for (v in values) {
            val w = stepTextPaint.measureText(formatStepLabel(v))
            if (w > widest) widest = w
        }
        return widest
    }

    private fun formatStepLabel(v: Float): String =
        if (v == v.toInt().toFloat()) v.toInt().toString() else v.toString()

    // ---------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentLeft = paddingLeft.toFloat()
        val contentTop = paddingTop.toFloat()
        val contentRight = (width - paddingRight).toFloat()
        val contentBottom = (height - paddingBottom).toFloat()
        if (contentRight <= contentLeft || contentBottom <= contentTop) return

        val labelHeight = if (showValueLabel) valueTextSizePx + 10f else 0f

        val steps = if (showStepLabels) stepValues() else emptyList()
        val axisGap = barLabelGapPx
        val axisWidth = if (steps.isEmpty()) 0f else widestStepLabelWidth(steps) + axisGap

        val trackLeft = contentLeft + axisWidth
        val trackTop = contentTop + labelHeight + minMaxLabelTopMarginPx
        val trackBottom = contentBottom - minMaxLabelBottomMarginPx
        if (trackBottom <= trackTop) return
        val trackRect = RectF(trackLeft, trackTop, contentRight, trackBottom)
        val radius = (contentRight - trackLeft) / 2f

        // track background (pill shape, matches .bp-track)
        trackPaint.color = trackColor
        canvas.drawRoundRect(trackRect, radius, radius, trackPaint)

        // fill, clipped to the same pill shape, growing from the bottom (matches .bp-fill)
        val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f
        val fraction = ((displayedValue - minValue) / range).coerceIn(0f, 1f)
        val fillTop = trackRect.bottom - trackRect.height() * fraction
        if (fillTop < trackRect.bottom) {
            val fillRect = RectF(trackRect.left, fillTop, trackRect.right, trackRect.bottom)

            clipPath.reset()
            clipPath.addRoundRect(trackRect, radius, radius, Path.Direction.CW)

            canvas.save()
            canvas.clipPath(clipPath)

            // gradient recomputed against the fill's own current bounds, so the
            // top of the fill is always fillColorStart and the bottom is always
            // fillColorEnd — same behavior as the CSS gradient applied to a
            // growing div.
            fillPaint.shader = LinearGradient(
                0f, fillRect.top, 0f, fillRect.bottom,
                fillColorStart, fillColorEnd,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(fillRect, fillPaint)
            canvas.restore()
            fillPaint.shader = null
        }

        // left-side step labels, min at the bottom to max at the top (matches .bp-axis)
        if (steps.isNotEmpty()) {
            stepTextPaint.color = textColor
            stepTextPaint.textSize = stepTextSizePx
            val fontMetrics = stepTextPaint.fontMetrics
            val textHalfHeight = (fontMetrics.descent - fontMetrics.ascent) / 2f
            val minBaseline = trackRect.top - fontMetrics.ascent
            val maxBaseline = trackRect.bottom - fontMetrics.descent
            for (v in steps) {
                val f = ((v - minValue) / range).coerceIn(0f, 1f)
                val y = trackRect.bottom - trackRect.height() * f
                val desiredBaseline = y + textHalfHeight - fontMetrics.descent
                val baseline = desiredBaseline.coerceIn(minBaseline, maxBaseline)
                canvas.drawText(formatStepLabel(v), trackRect.left - axisGap, baseline, stepTextPaint)
            }
        }

        // big current-value label, centered above the track
        if (showValueLabel) {
            valueTextPaint.color = textColor
            valueTextPaint.textSize = valueTextSizePx
            valueTextPaint.isFakeBoldText = true
            val label = displayedValue.toInt().toString()
            canvas.drawText(label, (trackRect.left + trackRect.right) / 2f, contentTop + valueTextSizePx, valueTextPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        valueAnimator?.cancel()
    }
}
