package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.humotron.app.R

class VerticalGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var minValue = 0f
    private var maxValue = 250f
    private var step = 10f

    var primaryValue = 120f
        set(value) {
            field = value.coerceIn(minValue, maxValue)
            invalidate()
        }

    var secondaryValue = 50f
        set(value) {
            field = value.coerceIn(minValue, maxValue)
            invalidate()
        }

    var highlightTextValue = 220f
        set(value) {
            field = value
            invalidate()
        }

    private var trackColor = Color.parseColor("#66A0A0A0")
    private var fillColor = Color.parseColor("#B2FF33")
    private var textColor = Color.parseColor("#99FFFFFF")
    private var highlightTextColor = Color.WHITE
    private var secondaryMarkerColor = Color.argb(140, 255, 255, 255)

    private var maxTextSize = sp(18f)
    private var minTextSize = sp(8f)

    private var calculatedTextSize = maxTextSize

    private val trackWidth = dp(28f)
    private val trackStroke = dp(2f)
    private val textTrackGap = dp(16f)

    private var trackRect = RectF()

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.LEFT
        typeface = Typeface.SANS_SERIF
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = highlightTextColor
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = trackStroke
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(140, 255, 255, 255)
        strokeWidth = dp(1.5f)
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.VerticalGaugeView) {

            minValue = getFloat(
                R.styleable.VerticalGaugeView_minValue,
                minValue
            )

            maxValue = getFloat(
                R.styleable.VerticalGaugeView_maxValue,
                maxValue
            )

            step = getFloat(
                R.styleable.VerticalGaugeView_step,
                step
            )

            primaryValue = getFloat(
                R.styleable.VerticalGaugeView_primaryValue,
                primaryValue
            )

            secondaryValue = getFloat(
                R.styleable.VerticalGaugeView_secondaryValue,
                secondaryValue
            )

            highlightTextValue = getFloat(
                R.styleable.VerticalGaugeView_highlightTextValue,
                highlightTextValue
            )

            trackColor = getColor(
                R.styleable.VerticalGaugeView_trackColor,
                trackColor
            )

            fillColor = getColor(
                R.styleable.VerticalGaugeView_fillColor,
                fillColor
            )

            textColor = getColor(
                R.styleable.VerticalGaugeView_textColor,
                textColor
            )

            highlightTextColor = getColor(
                R.styleable.VerticalGaugeView_highlightTextColor,
                highlightTextColor
            )

            secondaryMarkerColor = getColor(
                R.styleable.VerticalGaugeView_secondaryMarkerColor,
                secondaryMarkerColor
            )

            maxTextSize = getDimension(
                R.styleable.VerticalGaugeView_maxTextSize,
                maxTextSize
            )

            minTextSize = getDimension(
                R.styleable.VerticalGaugeView_minTextSize,
                minTextSize
            )
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {

        val widestLabel = formatValue(maxValue)

        val tempPaint = Paint(textPaint)
        tempPaint.textSize = maxTextSize

        val textWidth = tempPaint.measureText(widestLabel)

        val desiredWidth =
            paddingLeft +
                    paddingRight +
                    textWidth +
                    textTrackGap +
                    trackWidth

        val measuredWidth = resolveSize(
            desiredWidth.toInt(),
            widthMeasureSpec
        )

        val measuredHeight = resolveSize(
            suggestedMinimumHeight,
            heightMeasureSpec
        )

        setMeasuredDimension(
            measuredWidth,
            measuredHeight
        )
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)

        calculateOptimalTextSize()

        textPaint.textSize = calculatedTextSize
        highlightPaint.textSize = calculatedTextSize

        val labelWidth = textPaint.measureText(
            formatValue(maxValue)
        )

        val left =
            paddingLeft + labelWidth + textTrackGap

        val right = left + trackWidth

        trackRect.set(
            left,
            paddingTop.toFloat(),
            right,
            (height - paddingBottom).toFloat()
        )
    }

    private fun calculateOptimalTextSize() {

        val availableHeight =
            height - paddingTop - paddingBottom

        val labelCount =
            (((maxValue - minValue) / step).toInt() + 1)

        val maxSpacePerLabel =
            availableHeight.toFloat() / labelCount

        var low = minTextSize
        var high = maxTextSize

        while (high - low > 0.5f) {

            val mid = (low + high) / 2f

            textPaint.textSize = mid

            val metrics = textPaint.fontMetrics
            val textHeight =
                metrics.bottom - metrics.top

            if (textHeight <= maxSpacePerLabel * 0.85f) {
                low = mid
            } else {
                high = mid
            }
        }

        calculatedTextSize =
            low.coerceIn(minTextSize, maxTextSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawScale(canvas)
        drawTrack(canvas)
        drawFill(canvas)
        drawSecondaryMarker(canvas)
    }

    private fun drawScale(canvas: Canvas) {

        val fm = textPaint.fontMetrics

        var value = minValue

        while (value <= maxValue + 0.001f) {

            val y = valueToY(value)

            val baseline =
                y - (fm.ascent + fm.descent) / 2f

            val label = formatValue(value)

            val paint =
                if (value == highlightTextValue)
                    highlightPaint
                else
                    textPaint

            canvas.drawText(
                label,
                paddingLeft.toFloat(),
                baseline,
                paint
            )

            value += step
        }
    }

    private fun drawTrack(canvas: Canvas) {

        trackPaint.color = trackColor

        val radius = trackRect.width() / 2f

        canvas.drawRoundRect(
            trackRect,
            radius,
            radius,
            trackPaint
        )
    }

    private fun drawFill(canvas: Canvas) {

        val fillTop = valueToY(primaryValue)

        val radius = trackRect.width() / 2f

        val fillPath = Path()

        fillPath.addRoundRect(
            RectF(
                trackRect.left,
                fillTop,
                trackRect.right,
                trackRect.bottom
            ),
            floatArrayOf(
                0f, 0f,
                0f, 0f,
                radius, radius,
                radius, radius
            ),
            Path.Direction.CW
        )

        fillPaint.color = fillColor

        canvas.drawPath(
            fillPath,
            fillPaint
        )
    }

    private fun drawSecondaryMarker(canvas: Canvas) {

        val y = valueToY(secondaryValue)

        markerPaint.color = secondaryMarkerColor

        canvas.drawLine(
            trackRect.left,
            y,
            trackRect.right,
            y,
            markerPaint
        )
    }

    private fun valueToY(value: Float): Float {

        val ratio =
            (value - minValue) /
                    (maxValue - minValue)

        return trackRect.bottom -
                ratio * trackRect.height()
    }

    private fun formatValue(value: Float): String {
        return value.toInt().toString()
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    private fun sp(value: Float): Float =
        value * resources.displayMetrics.scaledDensity
}