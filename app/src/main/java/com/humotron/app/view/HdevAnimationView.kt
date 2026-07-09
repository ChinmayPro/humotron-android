package com.humotron.app.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.humotron.app.R
import kotlin.math.cos
import kotlin.math.min

/**
 * Base class for the Humotron "connecting device" animation.
 *
 * Ported from the web version's shared scene: breathing halo, three outward
 * pulse waves, and a circular progress ring — plus a per-device [drawArt]
 * hook that subclasses implement for their own art (ring orbit, band scan,
 * scale rise, cuff inflate).
 *
 * Color:
 *   - XML: app:hdevColor="#5FB7C4"
 *   - Kotlin property: view.color = Color.parseColor("#5FB7C4")
 *   - Method form: view.setColor(Color.parseColor("#5FB7C4"))
 *
 * Progress (0f..1f), always animates smoothly unless animate=false:
 *   - view.progress = 0.4f
 *   - view.setProgress(0.4f)
 *   - view.setProgress(0.4f, animate = true, duration = 450)
 *   - view.startConnecting(...) drives progress 0->1 over a duration with stage callbacks
 */
abstract class HdevAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val HALO_PERIOD = 3200L
        private const val WAVE_PERIOD = 3000L
        private const val WAVE_DELAY_STEP = 1000L
        private const val DEFAULT_PROGRESS_ANIM_DURATION = 450L
        private const val SCENE_UNITS = 280f       // matches the web .hscene 280x280 box
        private const val ART_VIEWBOX = 160f        // matches the web <svg viewBox="0 0 160 160">
        private const val ART_DISPLAY = 208f        // matches the web hdevArt() 208x208 render size
    }

    // ---------------------------------------------------------------
    // Color
    // ---------------------------------------------------------------

    @get:ColorInt
    var color: Int = Color.parseColor("#5FB7C4")
        set(@ColorInt value) {
            field = value
            invalidate()
        }

    fun setColorResource(@ColorRes resId: Int) {
        this.color = ContextCompat.getColor(context, resId)
    }

    // ---------------------------------------------------------------
    // Progress
    // ---------------------------------------------------------------

    /** The value currently being drawn (interpolated during an animation). */
    private var displayedProgress = 0f

    /** The last value requested via [setProgress] / [progress]. */
    private var targetProgress = 0f

    private var progressAnimator: ValueAnimator? = null

    var progress: Float
        get() = targetProgress
        @JvmName("setProgressK")
        set(value) = setProgress(value)

    /**
     * Sets progress in the 0f..1f range.
     * @param animate when true (default) the change is smoothly interpolated;
     *                when false it jumps immediately to the new value.
     * @param duration animation duration in ms, used only when [animate] is true.
     */
    @JvmOverloads
    fun setProgress(
        value: Float,
        animate: Boolean = true,
        duration: Long = DEFAULT_PROGRESS_ANIM_DURATION,
    ) {
        val clamped = value.coerceIn(0f, 1f)
        targetProgress = clamped
        progressAnimator?.cancel()
        if (!animate) {
            displayedProgress = clamped
            invalidate()
            return
        }
        progressAnimator = ValueAnimator.ofFloat(displayedProgress, clamped).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                displayedProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // fun getProgress(): Float = targetProgress (Removed to avoid JVM clash with property 'progress')

    /** Stages shown by default in [startConnecting]; override per device if useful. */
    protected open val defaultStages: List<String> =
        listOf("Searching for your device", "Device found", "Pairing securely", "Connected")

    /**
     * Convenience that mirrors the original web `runHdev()` flow: animates progress
     * from its current value up to 1f over [totalDurationMs], invoking [onStage]
     * whenever the active stage index changes, and [onComplete] once it reaches 1f.
     */
    @JvmOverloads
    fun startConnecting(
        totalDurationMs: Long = 5200L,
        stages: List<String> = defaultStages,
        onStage: ((text: String, index: Int) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
    ) {
        progressAnimator?.cancel()
        var lastStage = -1
        progressAnimator = ValueAnimator.ofFloat(displayedProgress, 1f).apply {
            duration = totalDurationMs
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                val p = anim.animatedValue as Float
                displayedProgress = p
                targetProgress = p
                val stageIdx = (p * stages.size).toInt().coerceIn(0, stages.size - 1)
                if (stageIdx != lastStage) {
                    lastStage = stageIdx
                    onStage?.invoke(stages[stageIdx], stageIdx)
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            })
            start()
        }
    }

    fun stopConnecting() {
        progressAnimator?.cancel()
    }

    // ---------------------------------------------------------------
    // Continuous idle animation (halo / waves / device art)
    // driven off a vsync-synced loop, matching independent CSS keyframes.
    // ---------------------------------------------------------------

    private var startUptime = 0L
    private var loopRunning = false

    private val invalidateLoop = object : Runnable {
        override fun run() {
            if (!loopRunning) return
            invalidate()
            postOnAnimation(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startUptime = SystemClock.uptimeMillis()
        loopRunning = true
        postOnAnimation(invalidateLoop)
        // BlurMaskFilter (used for the glow effect) requires a software layer.
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loopRunning = false
        progressAnimator?.cancel()
    }

    /** 0f..1f phase of a repeating [periodMs]-long cycle, offset by [delayMs]. */
    protected fun phase(periodMs: Long, delayMs: Long = 0L): Float {
        val t =
            ((SystemClock.uptimeMillis() - startUptime + delayMs) % periodMs + periodMs) % periodMs
        return t / periodMs.toFloat()
    }

    /** Smooth 0->1->0 wave over one period, matching CSS `0%,100%{a} 50%{b}` keyframes. */
    protected fun pulse01(p: Float): Float = 0.5f - 0.5f * cos((2 * Math.PI * p).toFloat())

    /** Simple quadratic ease-out, 0f..1f -> 0f..1f. */
    protected fun easeOut(p: Float): Float = 1f - (1f - p) * (1f - p)

    // ---------------------------------------------------------------
    // Shared paint objects (rebuilt lazily; color mutated per-draw)
    // ---------------------------------------------------------------

    protected val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    protected val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        alpha = 15
    }

    /** Configures [strokePaint] for the current color, ready to draw a glowing stroke. */
    protected fun glowStroke(widthPx: Float, alphaFraction: Float, blurRadius: Float = 6f): Paint {
        strokePaint.strokeWidth = widthPx
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.color = color
        strokePaint.alpha = (alphaFraction * 255).toInt().coerceIn(0, 255)
        strokePaint.maskFilter =
            if (blurRadius > 0f) BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL) else null
        return strokePaint
    }

    /** Configures [fillPaint] for the current color. */
    protected fun glowFill(alphaFraction: Float, blurRadius: Float = 0f): Paint {
        fillPaint.color = color
        fillPaint.alpha = (alphaFraction * 255).toInt().coerceIn(0, 255)
        fillPaint.maskFilter =
            if (blurRadius > 0f) BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL) else null
        return fillPaint
    }

    // ---------------------------------------------------------------
    // XML attribute parsing — runs last, so it correctly overrides the
    // hard-coded defaults above rather than being overwritten by them.
    // ---------------------------------------------------------------

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(
                attrs,
                R.styleable.HdevAnimationView,
                defStyleAttr,
                0
            )
            try {
                if (ta.hasValue(R.styleable.HdevAnimationView_hdevColor)) {
                    color = ta.getColor(R.styleable.HdevAnimationView_hdevColor, color)
                }
                if (ta.hasValue(R.styleable.HdevAnimationView_hdevProgress)) {
                    val p = ta.getFloat(R.styleable.HdevAnimationView_hdevProgress, 0f)
                    setProgress(p, animate = false)
                }
            } finally {
                ta.recycle()
            }
        }
    }

    // ---------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val sceneScale = min(width, height) / SCENE_UNITS

        drawHalo(canvas, cx, cy, sceneScale)
        drawWaves(canvas, cx, cy, sceneScale)
        drawProgressRing(canvas, cx, cy, sceneScale)

        val artScale = sceneScale * (ART_DISPLAY / ART_VIEWBOX)
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(artScale, artScale)
        canvas.translate(
            -ART_VIEWBOX / 2f,
            -ART_VIEWBOX / 2f
        ) // origin now matches original SVG's 0..160 space
        drawArt(canvas)
        canvas.restore()
    }

    private fun drawHalo(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        val b = pulse01(phase(HALO_PERIOD))          // 0..1..0
        val alpha = 0.4f + 0.52f * b                  // .4 -> .92 -> .4
        val radius = (120f * scale) * (0.9f + 0.15f * b) // scale .9 -> 1.05 -> .9
        val shader = RadialGradient(
            cx, cy, radius,
            intArrayOf(withAlpha(color, 0.24f), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = shader
        fillPaint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
        fillPaint.maskFilter = BlurMaskFilter(7f * scale, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(cx, cy, radius, fillPaint)
        fillPaint.shader = null
        fillPaint.maskFilter = null
    }

    private fun drawWaves(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        for (i in 0..2) {
            val p = phase(WAVE_PERIOD, WAVE_DELAY_STEP * i)
            val eased = easeOut(p)
            val radius = (65f * scale) * (0.5f + 1.55f * eased) // scale .5 -> 2.05
            val alpha = 0.5f * (1f - eased)
            canvas.drawCircle(cx, cy, radius, glowStroke(1.5f * scale, alpha, blurRadius = 0f))
        }
    }

    private fun drawProgressRing(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        val r = 110f * scale
        val rect = RectF(cx - r, cy - r, cx + r, cy + r)
        canvas.drawArc(rect, 0f, 360f, false, trackPaint.also { it.strokeWidth = 3f * scale })
        val sweep = 360f * displayedProgress
        canvas.drawArc(rect, -90f, sweep, false, glowStroke(3f * scale, 0.75f, blurRadius = 0f))
    }

    private fun withAlpha(@ColorInt baseColor: Int, fraction: Float): Int {
        val a = (Color.alpha(baseColor) * fraction).toInt().coerceIn(0, 255)
        return Color.argb(a, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    }

    /**
     * Subclasses draw their device-specific art here, using the ORIGINAL
     * SVG-style coordinate space: a 160x160 box with center at (80,80) —
     * the canvas is already translated/scaled so these coordinates line up
     * directly with the web version's hdevArt() markup.
     */
    protected abstract fun drawArt(canvas: Canvas)
}
