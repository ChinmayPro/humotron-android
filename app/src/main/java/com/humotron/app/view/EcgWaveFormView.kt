package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.humotron.app.R
import kotlin.math.abs
import kotlin.math.max

class EcgWaveFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var cornerRadiusPx: Float = dp(16f)
        set(value) {
            field = value.coerceAtLeast(0f)
            invalidate()
        }

    var strokeColor: Int = Color.parseColor("#4DFFFFFF")
        set(value) {
            field = value
            invalidate()
        }

    var strokeWidthPx: Float = dp(1f)
        set(value) {
            field = value.coerceAtLeast(0f)
            borderPaint.strokeWidth = field
            invalidate()
        }

    var gridColor: Int = Color.parseColor("#1FFFFFFF")
        set(value) {
            field = value
            invalidate()
        }

    var gridStrokeWidthPx: Float = dp(0.75f)
        set(value) {
            field = value.coerceAtLeast(0f)
            gridPaint.strokeWidth = field
            invalidate()
        }

    var gridGapPx: Float = dp(20f)
        set(value) {
            field = value.coerceAtLeast(dp(2f))
            invalidate()
        }

    var backgroundFillColor: Int = Color.parseColor("#101418")
        set(value) {
            field = value
            invalidate()
        }

    var waveLineColor: Int = Color.parseColor("#39FF88")
        set(value) {
            field = value
            wavePaint.color = field
            invalidate()
        }

    var waveStrokeWidthPx: Float = dp(2.5f)
        set(value) {
            field = value.coerceAtLeast(dp(0.5f))
            wavePaint.strokeWidth = field
            invalidate()
        }

    var waveScale: Float = 0.85f
        set(value) {
            field = value.coerceAtLeast(0.01f)
            invalidate()
        }

    var baselineOffsetPx: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var showGrid: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var showBorder: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var autoScale: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var maxVisibleSamples: Int = 600
        set(value) {
            val normalized = value.coerceAtLeast(2)
            if (field == normalized) return
            field = normalized
            resizeBuffer(normalized)
            invalidate()
        }

    private val backgroundRect = RectF()
    private val contentRect = RectF()
    private val clipPath = Path()
    private val wavePath = Path()

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = strokeColor
        strokeWidth = strokeWidthPx
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = gridColor
        strokeWidth = gridStrokeWidthPx
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = backgroundFillColor
    }

    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = waveLineColor
        strokeWidth = waveStrokeWidthPx
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var waveBuffer = FloatArray(maxVisibleSamples)
    private var sampleCount = 0
    private var writeIndex = 0

    init {
        context.withStyledAttributes(attrs, R.styleable.EcgWaveFormView, defStyleAttr, 0) {
            cornerRadiusPx = getDimension(
                R.styleable.EcgWaveFormView_ecgCornerRadius,
                cornerRadiusPx
            )
            strokeColor = getColor(
                R.styleable.EcgWaveFormView_strokeColor,
                strokeColor
            )
            strokeWidthPx = getDimension(
                R.styleable.EcgWaveFormView_strokeWidth,
                strokeWidthPx
            )
            gridColor = getColor(
                R.styleable.EcgWaveFormView_gridColor,
                gridColor
            )
            gridStrokeWidthPx = getDimension(
                R.styleable.EcgWaveFormView_gridStrokeWidth,
                gridStrokeWidthPx
            )
            gridGapPx = getDimension(
                R.styleable.EcgWaveFormView_gridGap,
                gridGapPx
            )
            backgroundFillColor = getColor(
                R.styleable.EcgWaveFormView_backgroundColor,
                backgroundFillColor
            )
            waveLineColor = getColor(
                R.styleable.EcgWaveFormView_waveLineColor,
                waveLineColor
            )
            waveStrokeWidthPx = getDimension(
                R.styleable.EcgWaveFormView_waveStrokeWidth,
                waveStrokeWidthPx
            )
            waveScale = getFloat(
                R.styleable.EcgWaveFormView_waveScale,
                waveScale
            )
            baselineOffsetPx = getDimension(
                R.styleable.EcgWaveFormView_baselineOffset,
                baselineOffsetPx
            )
            showGrid = getBoolean(
                R.styleable.EcgWaveFormView_showGrid,
                showGrid
            )
            showBorder = getBoolean(
                R.styleable.EcgWaveFormView_showBorder,
                showBorder
            )
            autoScale = getBoolean(
                R.styleable.EcgWaveFormView_autoScale,
                autoScale
            )
            maxVisibleSamples = getInt(
                R.styleable.EcgWaveFormView_maxVisibleSamples,
                maxVisibleSamples
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight + dp(240f).toInt()
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom + dp(120f).toInt()

        val measuredWidth = resolveSize(desiredWidth, widthMeasureSpec)
        val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateRects()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width <= 0 || height <= 0) return

        updateRects()

        backgroundPaint.color = backgroundFillColor
        canvas.drawRoundRect(
            backgroundRect,
            cornerRadiusPx,
            cornerRadiusPx,
            backgroundPaint
        )

        clipPath.reset()
        clipPath.addRoundRect(
            backgroundRect,
            cornerRadiusPx,
            cornerRadiusPx,
            Path.Direction.CW
        )

        canvas.save()
        canvas.clipPath(clipPath)
        if (showGrid) {
            drawGrid(canvas)
        }
        drawWave(canvas)
        canvas.restore()

        if (showBorder) {
            borderPaint.color = strokeColor
            borderPaint.strokeWidth = strokeWidthPx
            canvas.drawRoundRect(
                backgroundRect,
                cornerRadiusPx,
                cornerRadiusPx,
                borderPaint
            )
        }
    }

    fun submitWaveData(values: FloatArray?) {
        clearWaveBuffer()
        appendWaveData(values)
    }

    fun setWaveData(values: FloatArray?) {
        submitWaveData(values)
    }

    fun setDataSrc(values: FloatArray?) {
        submitWaveData(values)
    }

    fun appendWaveData(values: FloatArray?) {
        if (values == null || values.isEmpty()) return

        ensureBufferCapacity(maxVisibleSamples)

        for (value in values) {
            waveBuffer[writeIndex] = value
            writeIndex = (writeIndex + 1) % waveBuffer.size
            if (sampleCount < waveBuffer.size) {
                sampleCount++
            }
        }

        invalidate()
    }

    fun feedWave(values: FloatArray?) {
        appendWaveData(values)
    }

    fun clearWave() {
        clearWaveBuffer()
        invalidate()
    }

    private fun drawGrid(canvas: Canvas) {
        val gap = gridGapPx
        if (gap <= 0f) return

        gridPaint.color = gridColor
        gridPaint.strokeWidth = gridStrokeWidthPx

        var x = contentRect.left
        while (x <= contentRect.right + 0.5f) {
            canvas.drawLine(x, contentRect.top, x, contentRect.bottom, gridPaint)
            x += gap
        }

        var y = contentRect.top
        while (y <= contentRect.bottom + 0.5f) {
            canvas.drawLine(contentRect.left, y, contentRect.right, y, gridPaint)
            y += gap
        }
    }

    private fun drawWave(canvas: Canvas) {
        val data = snapshotWaveData()
        if (data.size < 2) return

        val contentWidth = contentRect.width()
        val contentHeight = contentRect.height()
        if (contentWidth <= 0f || contentHeight <= 0f) return

        val centerY = contentRect.centerY() + baselineOffsetPx
        val amplitudePx = (contentHeight / 2f) * waveScale
        val scaleFactor = if (autoScale) {
            max(1e-4f, data.maxOf { abs(it) })
        } else {
            1f
        }

        val pointCount = data.size
        val stepX = contentWidth / (pointCount - 1).coerceAtLeast(1)

        wavePaint.color = waveLineColor
        wavePaint.strokeWidth = waveStrokeWidthPx

        wavePath.reset()
        for (index in data.indices) {
            val x = contentRect.left + index * stepX
            val y = centerY - (data[index] / scaleFactor) * amplitudePx
            if (index == 0) {
                wavePath.moveTo(x, y)
            } else {
                wavePath.lineTo(x, y)
            }
        }

        canvas.drawPath(wavePath, wavePaint)
    }

    private fun snapshotWaveData(): FloatArray {
        if (sampleCount <= 0) return FloatArray(0)

        val result = FloatArray(sampleCount)
        val startIndex = ((writeIndex - sampleCount) % waveBuffer.size + waveBuffer.size) % waveBuffer.size

        for (i in 0 until sampleCount) {
            result[i] = waveBuffer[(startIndex + i) % waveBuffer.size]
        }

        return result
    }

    private fun updateRects() {
        backgroundRect.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (width - paddingRight).toFloat(),
            (height - paddingBottom).toFloat()
        )

        contentRect.set(
            backgroundRect.left + max(strokeWidthPx, dp(2f)),
            backgroundRect.top + max(strokeWidthPx, dp(2f)),
            backgroundRect.right - max(strokeWidthPx, dp(2f)),
            backgroundRect.bottom - max(strokeWidthPx, dp(2f))
        )
    }

    private fun resizeBuffer(newSize: Int) {
        val snapshot = snapshotWaveData()
        waveBuffer = FloatArray(newSize)
        sampleCount = 0
        writeIndex = 0

        if (snapshot.isEmpty()) return

        val start = max(0, snapshot.size - newSize)
        for (i in start until snapshot.size) {
            waveBuffer[writeIndex] = snapshot[i]
            writeIndex = (writeIndex + 1) % waveBuffer.size
            sampleCount++
        }
    }

    private fun ensureBufferCapacity(required: Int) {
        if (waveBuffer.size != required) {
            resizeBuffer(required)
        }
    }

    private fun clearWaveBuffer() {
        waveBuffer = FloatArray(maxVisibleSamples)
        sampleCount = 0
        writeIndex = 0
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }
}
