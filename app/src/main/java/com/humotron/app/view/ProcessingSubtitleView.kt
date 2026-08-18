package com.humotron.app.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.SystemClock
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import com.humotron.app.R
import androidx.core.graphics.toColorInt

/**
 * ProcessingSubtitleView
 *
 * A custom View extracted from ProcessingView that handles cycling crossfading subtitles.
 */
class ProcessingSubtitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private companion object {
        const val CROSSFADE_MS = 250L
    }

    private var subtitles: MutableList<CharSequence> = mutableListOf("Weighing your risk factors")
    private var subtitleCycleIntervalMs: Long = 2400L
    private var repeatEnabled: Boolean = false

    @ColorInt private var subtitleColor: Int = "#8FA7A3".toColorInt()
    private var subtitleTextSizePx: Float = sp(12.5f)

    private val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
    }

    private val subtitleLayoutCache = HashMap<Int, StaticLayout>()
    private var lastWidth = 0

    private var onAllShownListener: (() -> Unit)? = null
    private var delayAfterLastMs: Long = 0L
    private var firedCompletion = false

    private val startTimeMs = SystemClock.uptimeMillis()
    private var running = false
    private val frameTick = object : Runnable {
        override fun run() {
            if (!running) return
            
            invalidate()
            
            val n = subtitles.size
            if (!repeatEnabled && n > 0) {
                val elapsed = SystemClock.uptimeMillis() - startTimeMs
                val totalDuration = n * subtitleCycleIntervalMs
                // If we have a listener with delay, wait for it
                val waitTime = if (onAllShownListener != null) delayAfterLastMs else 0L
                if (elapsed >= totalDuration + waitTime) {
                    running = false
                    return
                }
            }
            
            postOnAnimation(this)
        }
    }

    init {
        attrs?.let { applyAttrs(context, it, defStyleAttr) }
    }

    private fun applyAttrs(context: Context, attrs: AttributeSet, defStyleAttr: Int) {
        val ta: TypedArray = context.obtainStyledAttributes(
            attrs, R.styleable.ProcessingView, defStyleAttr, 0
        )
        try {
            val singleSubtitle = ta.getString(R.styleable.ProcessingView_pv_subtitle)
            if (!singleSubtitle.isNullOrEmpty()) subtitles = mutableListOf(singleSubtitle)
            val subtitlesResId = ta.getResourceId(R.styleable.ProcessingView_pv_subtitles, 0)
            if (subtitlesResId != 0) {
                subtitles = context.resources.getStringArray(subtitlesResId).toMutableList<CharSequence>()
            }
            if (ta.hasValue(R.styleable.ProcessingView_pv_subtitleColor))
                subtitleColor = ta.getColor(R.styleable.ProcessingView_pv_subtitleColor, subtitleColor)
            subtitleTextSizePx = ta.getDimension(R.styleable.ProcessingView_pv_subtitleTextSize, subtitleTextSizePx)
            subtitleCycleIntervalMs = ta.getInt(
                R.styleable.ProcessingView_pv_subtitleCycleIntervalMs, subtitleCycleIntervalMs.toInt()
            ).toLong()
            repeatEnabled = ta.getBoolean(R.styleable.ProcessingView_pv_repeatEnabled, repeatEnabled)
        } finally {
            ta.recycle()
        }
    }

    fun setSubtitle(text: CharSequence) = setSubtitleMessages(listOf(text))

    fun setSubtitleMessages(messages: List<CharSequence>, cycleIntervalMs: Long = subtitleCycleIntervalMs) {
        subtitles = if (messages.isEmpty()) mutableListOf("") else messages.toMutableList()
        subtitleCycleIntervalMs = cycleIntervalMs
        subtitleLayoutCache.clear()
        firedCompletion = false
        requestLayout()
        invalidate()
    }

    /**
     * Set a listener to be notified after the last subtitle in the list has been displayed.
     * @param delayAfterLastMs Extra time to wait after the last subtitle's cycle finishes before calling [listener].
     * @param listener The callback to trigger.
     */
    fun setOnAllShownListener(delayAfterLastMs: Long, listener: () -> Unit) {
        this.onAllShownListener = listener
        this.delayAfterLastMs = delayAfterLastMs
        this.firedCompletion = false
    }

    fun setSubtitleColor(@ColorInt color: Int) {
        subtitleColor = color
        invalidate()
    }

    fun setRepeatEnabled(enabled: Boolean) {
        repeatEnabled = enabled
        invalidate()
    }

    fun setSubtitleTextSize(sizePx: Float) {
        subtitleTextSizePx = sizePx
        subtitleLayoutCache.clear()
        requestLayout()
        invalidate()
    }

    fun start() {
        if (running) return
        running = true
        removeCallbacks(frameTick)
        postOnAnimation(frameTick)
    }

    fun stop() {
        running = false
        removeCallbacks(frameTick)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        // Ensure paint has correct size for measurement
        subtitlePaint.textSize = subtitleTextSizePx

        val maxWidthPossible = (widthSize - paddingLeft - paddingRight).coerceAtLeast(0)

        val desiredTextWidth = if (widthMode != MeasureSpec.EXACTLY) {
            var maxW = 0f
            subtitles.forEach {
                maxW = maxOf(maxW, subtitlePaint.measureText(it.toString()))
            }
            maxW.toInt().coerceAtMost(maxWidthPossible)
        } else {
            maxWidthPossible
        }

        val resolvedWidth = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            (desiredTextWidth + paddingLeft + paddingRight).coerceAtMost(widthSize)
        }

        val textWidthForLayout = (resolvedWidth - paddingLeft - paddingRight).coerceAtLeast(1)
        buildTextLayouts(textWidthForLayout)

        val maxLayoutHeight = subtitleLayoutCache.values.maxOfOrNull { it.height } ?: 0
        val height = resolveSize(maxLayoutHeight + paddingTop + paddingBottom, heightMeasureSpec)
        setMeasuredDimension(resolvedWidth, height)
    }

    private fun buildTextLayouts(textWidth: Int) {
        // We rebuild if size changed or width changed to ensure alignment is correct for the current width
        lastWidth = textWidth

        subtitlePaint.color = subtitleColor
        subtitlePaint.textSize = subtitleTextSizePx

        subtitleLayoutCache.clear()
        subtitles.forEachIndexed { i, s ->
            subtitleLayoutCache[i] = staticLayout(s, subtitlePaint, textWidth, Layout.Alignment.ALIGN_CENTER)
        }
    }

    private fun staticLayout(text: CharSequence, paint: TextPaint, width: Int, align: Layout.Alignment): StaticLayout {
        return if (Build.VERSION.SDK_INT >= 23) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(align)
                .setLineSpacing(0f, 1.3f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, width, align, 1.3f, 0f, false)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val elapsed = SystemClock.uptimeMillis() - startTimeMs
        val areaLeft = paddingLeft.toFloat()
        val areaTop = paddingTop.toFloat()

        drawSubtitleCrossfade(canvas, elapsed, areaLeft, areaTop)

        // Check for completion listener
        val n = subtitles.size
        if (n > 0 && onAllShownListener != null && !firedCompletion) {
            val totalCycleTime = n * subtitleCycleIntervalMs
            if (elapsed >= totalCycleTime + delayAfterLastMs) {
                firedCompletion = true
                onAllShownListener?.invoke()
            }
        }
    }

    private fun drawSubtitleCrossfade(canvas: Canvas, elapsed: Long, left: Float, top: Float) {
        val n = subtitles.size
        if (n == 0) return
        val cycleLen = subtitleCycleIntervalMs
        
        val totalDuration = n * cycleLen
        
        if (!repeatEnabled && elapsed >= totalDuration - CROSSFADE_MS) {
            // Stay on the last subtitle
            val last = subtitleLayoutCache[n - 1] ?: return
            canvas.save()
            canvas.translate(left, top)
            subtitlePaint.alpha = 255
            last.draw(canvas)
            canvas.restore()
            return
        }

        val idx = ((elapsed / cycleLen) % n).toInt()
        val nextIdx = (idx + 1) % n
        val intoStep = elapsed % cycleLen
        val fadeStart = cycleLen - CROSSFADE_MS

        val current = subtitleLayoutCache[idx] ?: return
        
        // Only crossfade if we are repeating OR we are not at the very last item
        val shouldCrossfade = n > 1 && (repeatEnabled || idx < n - 1)

        if (shouldCrossfade && intoStep >= fadeStart) {
            val t = ((intoStep - fadeStart).toFloat() / CROSSFADE_MS).coerceIn(0f, 1f)
            
            canvas.save()
            canvas.translate(left, top)
            subtitlePaint.alpha = (255 * (1f - t)).toInt()
            current.draw(canvas)
            canvas.restore()
            
            val next = subtitleLayoutCache[nextIdx]
            if (next != null) {
                canvas.save()
                canvas.translate(left, top)
                subtitlePaint.alpha = (255 * t).toInt()
                next.draw(canvas)
                canvas.restore()
            }
            subtitlePaint.alpha = 255
        } else {
            canvas.save()
            canvas.translate(left, top)
            subtitlePaint.alpha = 255
            current.draw(canvas)
            canvas.restore()
        }
    }

    private fun sp(v: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)
}
