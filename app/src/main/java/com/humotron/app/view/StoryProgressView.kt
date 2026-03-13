package com.humotron.app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.humotron.app.R

class StoryProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var segmentCount = 0
    private var currentSegment = 0
    private var segmentProgress = 1f // 0.0f to 1.0f for the current segment

    private val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val segmentRect = RectF()

    private var segmentGap = 8f // Gap between segments in dp
    private var segmentStrokeWidth = 4f // Thickness of segments in dp
    private var segmentCornerRadius = 2f // Corner radius for segments in dp
    private var listener: OnProgressChangeListener? = null

    @ColorInt
    private var segmentColor = Color.GRAY

    @ColorInt
    private var progressColor = Color.WHITE

    init {
        // Load attributes from XML if defined
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.StoryProgressView, 0, 0) {
                segmentGap = getDimension(R.styleable.StoryProgressView_segmentGap, dpToPx(8f))
                segmentStrokeWidth =
                    getDimension(R.styleable.StoryProgressView_segmentStrokeWidth, dpToPx(4f))
                segmentCornerRadius =
                    getDimension(R.styleable.StoryProgressView_segmentCornerRadius, dpToPx(2f))
                segmentColor = getColor(R.styleable.StoryProgressView_segmentColor, Color.GRAY)
                progressColor = getColor(R.styleable.StoryProgressView_progressColor, Color.WHITE)
            }
        }

        segmentPaint.color = segmentColor
        segmentPaint.style = Paint.Style.FILL // Or Paint.Style.STROKE if you want outlines

        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.FILL
    }

    fun setSegmentCount(count: Int) {
        if (count < 0) return
        segmentCount = count
        currentSegment = 0 // Reset when segment count changes
        segmentProgress = 0f
        invalidate() // Redraw
    }

    fun setCurrentSegment(index: Int) {
        if (index < 0 || index >= segmentCount) return
        currentSegment = index
        segmentProgress = 0f // Reset progress for the new segment
        listener?.onSegmentChanged(currentSegment)
        invalidate()
    }

    /**
     * @param progress Progress for the current segment (0.0f to 1.0f)
     */
    fun setSegmentProgress(progress: Float) {
        segmentProgress = progress.coerceIn(0f, 1f)
        invalidate()
    }

    fun nextSegment(): Boolean {
        if (currentSegment < segmentCount - 1) {
            currentSegment++
            segmentProgress = 0f
            listener?.onSegmentChanged(currentSegment)
            invalidate()
            return true
        }
        return false // No more segments
    }

    fun previousSegment(): Boolean {
        if (currentSegment > 0) {
            currentSegment--
            segmentProgress = 0f // Or you might want to set it to 1f if it was completed
            listener?.onSegmentChanged(currentSegment)
            invalidate()
            return true
        }
        return false
    }

    fun getCurrentSegment(): Int = currentSegment

    fun getSegmentCount(): Int = segmentCount


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segmentCount <= 0) return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val totalGapSpace = segmentGap * (segmentCount - 1).coerceAtLeast(0)
        val availableWidthForSegments = viewWidth - totalGapSpace
        val singleSegmentWidth = if (segmentCount >= 0) availableWidthForSegments / segmentCount else 0f

        var currentX = 0f

        for (i in 0 until segmentCount) {
            segmentRect.set(currentX, 0f, currentX + singleSegmentWidth, viewHeight)

            // Draw the base segment (background)
            canvas.drawRoundRect(
                segmentRect,
                segmentCornerRadius,
                segmentCornerRadius,
                segmentPaint
            )

            // Draw the progress for segments that are completed,
            // including the current one.
            if (i <= currentSegment) {
                canvas.drawRoundRect(
                    segmentRect,
                    segmentCornerRadius,
                    segmentCornerRadius,
                    progressPaint
                )
            }

            currentX += singleSegmentWidth + segmentGap
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    // --- Optional: Methods to set colors and dimensions programmatically ---
    fun setSegmentGap(gapInDp: Float) {
        segmentGap = dpToPx(gapInDp)
        invalidate()
    }

    fun setSegmentStrokeWidth(widthInDp: Float) {
        segmentStrokeWidth = dpToPx(widthInDp)
        // If you were using Paint.Style.STROKE, you'd set segmentPaint.strokeWidth here
        invalidate()
    }

    fun setSegmentColor(@ColorInt color: Int) {
        segmentColor = color
        segmentPaint.color = segmentColor
        invalidate()
    }

    fun setProgressColor(@ColorInt color: Int) {
        progressColor = color
        progressPaint.color = progressColor
        invalidate()
    }

    fun setOnProgressChangeListener(listener: OnProgressChangeListener) {
        this.listener = listener
    }

    interface OnProgressChangeListener {
        fun onSegmentChanged(segmentIndex: Int)
    }
}