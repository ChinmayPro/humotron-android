package com.humotron.app.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.humotron.app.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.core.graphics.toColorInt

/**
 * ProcessingView
 *
 * A self-contained, fully custom Android [View] that reproduces the "Building your picture"
 * processing / assessment-loading screen (hex orbit art + center glyph + floating label chips)
 * from the exported Humotron HTML screen, `assess_processing`.
 *
 * Everything visual is driven by Canvas drawing (no child views), so a single instance of this
 * class is the whole widget -- "one custom view", as requested. All colors, text, the center
 * icon, chip styling and spacing are customizable via XML attributes (see
 * attrs_processing_view.xml) or the public setters below. Because this is a plain [View],
 * `android:layout_margin` and `android:padding` work exactly as they do for any other view --
 * no special support is needed for margins, they're inherited from the Android view system.
 *
 * IMPORTANT SETUP STEP: copy attrs_processing_view.xml into your module's res/values/ folder.
 * This class references the generated R.styleable.ProcessingView_* fields directly, so it will
 * only compile once that attrs file is present in the same module (or a module it depends on).
 *
 * Usage (XML):
 * ```xml
 * <com.humotron.ui.processing.ProcessingView
 *     android:id="@+id/processingView"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:layout_margin="16dp"
 *     app:pv_labels="@array/processing_labels"
 *     app:pv_accentColor="#F0795E" />
 * ```
 *
 * Usage (Kotlin):
 * ```kotlin
 * processingView.setAccentColor(Color.parseColor("#5FB7C4"))
 * processingView.setLabels(listOf("Activity", "Stress", "Sleep", "Diet", "Sun", "Rest"))
 * processingView.setCenterIcon(R.drawable.ic_heart_pulse)
 * processingView.start()
 * ```
 */
class ProcessingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    // ---------------------------------------------------------------------
    // Design-space constants. The original screen was authored as a 230x230
    // SVG viewBox centered at (115,115). Every art coordinate below is kept
    // in that same design space and scaled into real pixels at draw time,
    // so the layout stays "same to same" as the source at any view size.
    // ---------------------------------------------------------------------
    private companion object {
        const val DESIGN_SIZE = 230f
        const val DESIGN_CENTER = 115f
        const val DEFAULT_ORBIT_RADIUS = 58f
        const val DEFAULT_LABEL_RADIUS = 76f
        const val DEFAULT_DOT_RADIUS = 3.6f
        const val DEFAULT_CENTER_CIRCLE_RADIUS = 19f
        const val DEFAULT_CENTER_BREATH_RADIUS = 30f
        const val DEFAULT_RING_RADIUS = 46f
        const val ICON_VIEWBOX = 24f

        const val LINE_PERIOD_MS = 1700L
        const val RING_PERIOD_MS = 2600L
        const val RING_DELAY_MS = 1300L
        const val DOT_PERIOD_MS = 2400L
        const val DOT_STAGGER_MS = 250L
        const val LABEL_PERIOD_MS = 2400L
        const val LABEL_STAGGER_MS = 300L
        const val CORE_PERIOD_MS = 2400L
    }

    // ---------------------------------------------------------------------
    // Public model
    // ---------------------------------------------------------------------

    private var labels: MutableList<CharSequence> = mutableListOf(
        "Activity", "Stress", "Sleep", "Tobacco", "Family", "BP"
    )

    // ---------------------------------------------------------------------
    // Colors / sizes (all overridable). A value of 0 for a "derived" color
    // means "fall back to accentColor" -- mirrors how the source CSS reuses
    // one accent (--ac) for lines/dots/rings/center/footer-icon together.
    // ---------------------------------------------------------------------

    @ColorInt
    private var accentColor: Int = "#F0795E".toColorInt()
    @ColorInt
    private var lineColor: Int = 0
    @ColorInt
    private var dotColor: Int = "#A9C2BE".toColorInt()
    @ColorInt
    private var centerCircleColor: Int = 0
    @ColorInt
    private var centerIconTint: Int = "#0D1618".toColorInt()
    private var centerIconSizeRatio: Float = 0.55f
    private var centerIconDrawable: Drawable? = null

    @ColorInt
    private var chipBackgroundColor: Int = Color.argb(13, 255, 255, 255)
    @ColorInt
    private var chipStrokeColor: Int = Color.argb(23, 255, 255, 255)
    private var chipStrokeWidthPx: Float = dp(1f)
    @ColorInt
    private var chipTextColor: Int = "#A9C2BE".toColorInt()
    private var chipTextSizePx: Float = sp(9.5f)
    private var chipCornerRadiusPx: Float = dp(11f)
    private var chipHPaddingPx: Float = dp(8f)
    private var chipVPaddingPx: Float = dp(4f)

    private var artSizePx: Float = dp(230f)
    private var orbitRadiusDesign: Float = DEFAULT_ORBIT_RADIUS
    private var labelRadiusDesign: Float = DEFAULT_LABEL_RADIUS
    private var dotRadiusDesign: Float = DEFAULT_DOT_RADIUS
    private var centerCircleRadiusDesign: Float = DEFAULT_CENTER_CIRCLE_RADIUS

    private var maxContentWidthPx: Float = dp(280f)
    private var spacingArtToTitlePx: Float = dp(16f)

    private var autoStart: Boolean = true

    // ---------------------------------------------------------------------
    // Paints (allocated once, reused every frame)
    // ---------------------------------------------------------------------

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val coreBreathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val coreSolidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val chipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val chipStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val chipTextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    private val reusableRect = RectF()
    private val heartPath = Path()

    // ---------------------------------------------------------------------
    // Animation clock -- a single free-running frame loop. Every animated
    // element derives its own phase from elapsed time + its own period/delay,
    // mirroring how the original CSS keyframe animations were independent.
    // ---------------------------------------------------------------------

    private val startTimeMs = SystemClock.uptimeMillis()
    private var running = false
    private val frameTick = object : Runnable {
        override fun run() {
            invalidate()
            if (running) postOnAnimation(this)
        }
    }

    // Cached layouts + the pixel width they were built for, rebuilt in onMeasure

    init {
        buildDefaultIconPaths()
        attrs?.let { applyAttrs(context, it, defStyleAttr) }
        setLayerType(LAYER_TYPE_SOFTWARE, null) // DashPathEffect renders more reliably in software
    }

    // ---------------------------------------------------------------------
    // Attribute parsing
    // ---------------------------------------------------------------------

    private fun applyAttrs(context: Context, attrs: AttributeSet, defStyleAttr: Int) {
        val ta: TypedArray = context.obtainStyledAttributes(
            attrs, R.styleable.ProcessingView, defStyleAttr, 0
        )
        try {
            if (ta.hasValue(R.styleable.ProcessingView_pv_accentColor))
                accentColor = ta.getColor(R.styleable.ProcessingView_pv_accentColor, accentColor)
            if (ta.hasValue(R.styleable.ProcessingView_pv_lineColor))
                lineColor = ta.getColor(R.styleable.ProcessingView_pv_lineColor, lineColor)
            if (ta.hasValue(R.styleable.ProcessingView_pv_dotColor))
                dotColor = ta.getColor(R.styleable.ProcessingView_pv_dotColor, dotColor)
            if (ta.hasValue(R.styleable.ProcessingView_pv_centerCircleColor))
                centerCircleColor =
                    ta.getColor(R.styleable.ProcessingView_pv_centerCircleColor, centerCircleColor)
            if (ta.hasValue(R.styleable.ProcessingView_pv_centerIconTint))
                centerIconTint =
                    ta.getColor(R.styleable.ProcessingView_pv_centerIconTint, centerIconTint)
            if (ta.hasValue(R.styleable.ProcessingView_pv_centerIcon))
                centerIconDrawable = ta.getDrawable(R.styleable.ProcessingView_pv_centerIcon)
            centerIconSizeRatio =
                ta.getFloat(R.styleable.ProcessingView_pv_centerIconSizeRatio, centerIconSizeRatio)

            if (ta.hasValue(R.styleable.ProcessingView_pv_chipBackgroundColor))
                chipBackgroundColor = ta.getColor(
                    R.styleable.ProcessingView_pv_chipBackgroundColor,
                    chipBackgroundColor
                )
            if (ta.hasValue(R.styleable.ProcessingView_pv_chipStrokeColor))
                chipStrokeColor =
                    ta.getColor(R.styleable.ProcessingView_pv_chipStrokeColor, chipStrokeColor)
            if (ta.hasValue(R.styleable.ProcessingView_pv_chipTextColor))
                chipTextColor =
                    ta.getColor(R.styleable.ProcessingView_pv_chipTextColor, chipTextColor)
            chipStrokeWidthPx =
                ta.getDimension(R.styleable.ProcessingView_pv_chipStrokeWidth, chipStrokeWidthPx)
            chipTextSizePx =
                ta.getDimension(R.styleable.ProcessingView_pv_chipTextSize, chipTextSizePx)
            chipCornerRadiusPx =
                ta.getDimension(R.styleable.ProcessingView_pv_chipCornerRadius, chipCornerRadiusPx)
            chipHPaddingPx =
                ta.getDimension(R.styleable.ProcessingView_pv_chipHorizontalPadding, chipHPaddingPx)
            chipVPaddingPx =
                ta.getDimension(R.styleable.ProcessingView_pv_chipVerticalPadding, chipVPaddingPx)

            val labelsResId = ta.getResourceId(R.styleable.ProcessingView_pv_labels, 0)
            if (labelsResId != 0) {
                labels = context.resources.getStringArray(labelsResId).toMutableList<CharSequence>()
            }

            artSizePx = ta.getDimension(R.styleable.ProcessingView_pv_artSize, artSizePx)
            orbitRadiusDesign = pxToDesign(
                ta.getDimension(
                    R.styleable.ProcessingView_pv_orbitRadius,
                    designToPx(orbitRadiusDesign)
                )
            )
            labelRadiusDesign = pxToDesign(
                ta.getDimension(
                    R.styleable.ProcessingView_pv_labelRadius,
                    designToPx(labelRadiusDesign)
                )
            )
            dotRadiusDesign = pxToDesign(
                ta.getDimension(
                    R.styleable.ProcessingView_pv_dotRadius,
                    designToPx(dotRadiusDesign)
                )
            )
            centerCircleRadiusDesign = pxToDesign(
                ta.getDimension(
                    R.styleable.ProcessingView_pv_centerCircleRadius,
                    designToPx(centerCircleRadiusDesign)
                )
            )

            maxContentWidthPx =
                ta.getDimension(R.styleable.ProcessingView_pv_maxContentWidth, maxContentWidthPx)
            spacingArtToTitlePx = ta.getDimension(
                R.styleable.ProcessingView_pv_spacingArtToTitle,
                spacingArtToTitlePx
            )

            autoStart = ta.getBoolean(R.styleable.ProcessingView_pv_autoStart, autoStart)
        } finally {
            ta.recycle()
        }
    }

    // ---------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------

    fun setLabels(newLabels: List<CharSequence>) {
        labels = newLabels.toMutableList(); requestLayout(); invalidate()
    }

    fun setAccentColor(@ColorInt color: Int) {
        accentColor = color; invalidate()
    }

    fun setLineColor(@ColorInt color: Int) {
        lineColor = color; invalidate()
    }

    fun setDotColor(@ColorInt color: Int) {
        dotColor = color; invalidate()
    }

    fun setCenterCircleColor(@ColorInt color: Int) {
        centerCircleColor = color; invalidate()
    }

    fun setCenterIconTint(@ColorInt color: Int) {
        centerIconTint = color; invalidate()
    }

    fun setCenterIconSizeRatio(ratio: Float) {
        centerIconSizeRatio = ratio; invalidate()
    }

    fun setCenterIcon(drawable: Drawable?) {
        centerIconDrawable = drawable?.mutate(); invalidate()
    }

    fun setCenterIcon(@DrawableRes resId: Int) {
        setCenterIcon(ContextCompat.getDrawable(context, resId))
    }

    fun setChipBackgroundColor(@ColorInt color: Int) {
        chipBackgroundColor = color; invalidate()
    }

    fun setChipStrokeColor(@ColorInt color: Int) {
        chipStrokeColor = color; invalidate()
    }

    fun setChipTextColor(@ColorInt color: Int) {
        chipTextColor = color; invalidate()
    }

    fun setChipTextSize(sizePx: Float) {
        chipTextSizePx = sizePx; invalidate()
    }

    fun setChipCornerRadius(radiusPx: Float) {
        chipCornerRadiusPx = radiusPx; invalidate()
    }

    fun setArtSize(sizePx: Float) {
        artSizePx = sizePx; requestLayout(); invalidate()
    }

    /** Starts the looping animation. Called automatically on attach unless pv_autoStart=false. */
    fun start() {
        if (running) return
        running = true
        removeCallbacks(frameTick)
        postOnAnimation(frameTick)
    }

    /** Stops the looping animation and freezes the current frame. */
    fun stop() {
        running = false
        removeCallbacks(frameTick)
    }

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (autoStart) start()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    // ---------------------------------------------------------------------
    // Measure
    // ---------------------------------------------------------------------

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSizeSpec = MeasureSpec.getSize(widthMeasureSpec)

        val desiredContentWidth = maxOf(artSizePx, maxContentWidthPx)
        val resolvedWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSizeSpec
            MeasureSpec.AT_MOST -> min(
                widthSizeSpec.toFloat(),
                desiredContentWidth + paddingLeft + paddingRight
            ).toInt()

            else -> (desiredContentWidth + paddingLeft + paddingRight).toInt()
        }

        val contentHeight = artSizePx + spacingArtToTitlePx

        val resolvedHeight =
            resolveSize((contentHeight + paddingTop + paddingBottom).toInt(), heightMeasureSpec)

        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    // ---------------------------------------------------------------------
    // Draw
    // ---------------------------------------------------------------------

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val elapsed = SystemClock.uptimeMillis() - startTimeMs

        val artLeft = paddingLeft + (measuredWidth - paddingLeft - paddingRight - artSizePx) / 2f
        val artTop = paddingTop.toFloat()
        val scale = artSizePx / DESIGN_SIZE
        val cx = artLeft + artSizePx / 2f
        val cy = artTop + artSizePx / 2f

        fun mapX(x: Float) = cx + (x - DESIGN_CENTER) * scale
        fun mapY(y: Float) = cy + (y - DESIGN_CENTER) * scale
        fun mapLen(l: Float) = l * scale

        drawOrbitLines(canvas, elapsed, ::mapX, ::mapY)
        drawPulseRings(canvas, elapsed, cx, cy, mapLen(DEFAULT_RING_RADIUS))
        drawOrbitDots(canvas, elapsed, ::mapX, ::mapY, mapLen(dotRadiusDesign))
        drawCenterBreath(canvas, elapsed, cx, cy, mapLen(DEFAULT_CENTER_BREATH_RADIUS))
        drawCenterSolid(canvas, cx, cy, mapLen(centerCircleRadiusDesign))
        drawCenterIcon(canvas, cx, cy, mapLen(centerCircleRadiusDesign) * 2f * centerIconSizeRatio)
        drawLabelChips(canvas, elapsed, ::mapX, ::mapY)
    }

    // --- orbit lines (dashed, "marching ants") ---
    private fun drawOrbitLines(
        canvas: Canvas,
        elapsed: Long,
        mapX: (Float) -> Float,
        mapY: (Float) -> Float,
    ) {
        linePaint.color = if (lineColor != 0) lineColor else withAlpha(accentColor, 128)
        linePaint.strokeWidth = designToPx(1f)
        val dashOn = designToPx(4f)
        val dashOff = designToPx(6f)
        val cyclePx = dashOn + dashOff
        val dashPhase = phase(elapsed, LINE_PERIOD_MS) * cyclePx * 5f
        linePaint.pathEffect = DashPathEffect(floatArrayOf(dashOn, dashOff), -dashPhase)

        for (i in 0 until 6) {
            val angle = angleForIndex(i)
            val nx = DESIGN_CENTER + orbitRadiusDesign * cos(angle)
            val ny = DESIGN_CENTER + orbitRadiusDesign * sin(angle)
            canvas.drawLine(mapX(DESIGN_CENTER), mapY(DESIGN_CENTER), mapX(nx), mapY(ny), linePaint)
        }
    }

    // --- two staggered expanding/fading rings around the center ---
    private fun drawPulseRings(
        canvas: Canvas,
        elapsed: Long,
        cx: Float,
        cy: Float,
        baseRadiusPx: Float,
    ) {
        ringPaint.color = if (lineColor != 0) lineColor else accentColor
        ringPaint.strokeWidth = designToPx(1.5f)
        for (ring in 0 until 2) {
            val delay = if (ring == 0) 0L else RING_DELAY_MS
            val p = phase(elapsed, RING_PERIOD_MS, delay)
            val alpha = (0.7f * (1f - p)).coerceIn(0f, 1f)
            ringPaint.alpha = (alpha * 255).toInt()
            canvas.drawCircle(cx, cy, baseRadiusPx * (0.5f + p * 2f), ringPaint)
        }
    }

    // --- 6 small breathing dots at each orbit node ---
    private fun drawOrbitDots(
        canvas: Canvas,
        elapsed: Long,
        mapX: (Float) -> Float,
        mapY: (Float) -> Float,
        radiusPx: Float,
    ) {
        dotPaint.color = dotColor
        for (i in 0 until 6) {
            val angle = angleForIndex(i)
            val nx = DESIGN_CENTER + orbitRadiusDesign * cos(angle)
            val ny = DESIGN_CENTER + orbitRadiusDesign * sin(angle)
            val eased = breathe(elapsed, DOT_PERIOD_MS, i * DOT_STAGGER_MS)
            dotPaint.alpha = ((0.3f + 0.7f * eased) * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(mapX(nx), mapY(ny), radiusPx, dotPaint)
        }
    }

    // --- translucent breathing halo behind the solid center disc ---
    private fun drawCenterBreath(
        canvas: Canvas,
        elapsed: Long,
        cx: Float,
        cy: Float,
        baseRadiusPx: Float,
    ) {
        val eased = breathe(elapsed, CORE_PERIOD_MS, 0L)
        val scaleFactor = 0.9f + 0.18f * eased
        val alpha = (0.14f * (0.8f + 0.2f * eased) * 255).toInt().coerceIn(0, 255)
        coreBreathPaint.color = if (centerCircleColor != 0) centerCircleColor else accentColor
        coreBreathPaint.alpha = alpha
        canvas.drawCircle(cx, cy, baseRadiusPx * scaleFactor, coreBreathPaint)
    }

    private fun drawCenterSolid(canvas: Canvas, cx: Float, cy: Float, radiusPx: Float) {
        coreSolidPaint.color = if (centerCircleColor != 0) centerCircleColor else accentColor
        coreSolidPaint.alpha = 255
        canvas.drawCircle(cx, cy, radiusPx, coreSolidPaint)
    }

    private fun drawCenterIcon(canvas: Canvas, cx: Float, cy: Float, sizePx: Float) {
        val drawable = centerIconDrawable
        if (drawable != null) {
            val half = (sizePx / 2f).toInt()
            drawable.setBounds(
                (cx - half).toInt(),
                (cy - half).toInt(),
                (cx + half).toInt(),
                (cy + half).toInt()
            )
            DrawableCompat.setTint(drawable, centerIconTint)
            drawable.draw(canvas)
            return
        }
        // Built-in default: heart glyph, reproduced from the original SVG path, 24x24 viewBox
        iconPaint.color = centerIconTint
        val iconScale = sizePx / ICON_VIEWBOX
        canvas.save()
        canvas.translate(cx - sizePx / 2f, cy - sizePx / 2f)
        canvas.scale(iconScale, iconScale)
        iconPaint.strokeWidth = 1.7f / iconScale
        canvas.drawPath(heartPath, iconPaint)
        canvas.restore()
    }

    // --- floating label chips (e.g. Activity / Stress / Sleep...) ---
    private fun drawLabelChips(
        canvas: Canvas,
        elapsed: Long,
        mapX: (Float) -> Float,
        mapY: (Float) -> Float,
    ) {
        chipTextPaint.color = chipTextColor
        chipTextPaint.textSize = chipTextSizePx
        chipTextPaint.isFakeBoldText = true
        chipStrokePaint.strokeWidth = chipStrokeWidthPx

        val count = labels.size
        for (i in 0 until count) {
            val angle = angleForIndex(i, count)
            val lx = DESIGN_CENTER + labelRadiusDesign * cos(angle)
            val ly = DESIGN_CENTER + labelRadiusDesign * sin(angle)
            val px = mapX(lx)
            val py = mapY(ly)

            val eased = breathe(elapsed, LABEL_PERIOD_MS, i * LABEL_STAGGER_MS)
            val alpha = (0.3f + 0.7f * eased).coerceIn(0f, 1f)

            val label = labels[i]
            val textWidth = chipTextPaint.measureText(label, 0, label.length)
            val chipWidth = textWidth + chipHPaddingPx * 2f
            val chipHeight = chipTextSizePx + chipVPaddingPx * 2f

            reusableRect.set(
                px - chipWidth / 2f,
                py - chipHeight / 2f,
                px + chipWidth / 2f,
                py + chipHeight / 2f
            )

            chipBgPaint.color = chipBackgroundColor
            chipBgPaint.alpha = (Color.alpha(chipBackgroundColor) * alpha).toInt()
            canvas.drawRoundRect(reusableRect, chipCornerRadiusPx, chipCornerRadiusPx, chipBgPaint)

            chipStrokePaint.color = chipStrokeColor
            chipStrokePaint.alpha = (Color.alpha(chipStrokeColor) * alpha).toInt()
            canvas.drawRoundRect(
                reusableRect,
                chipCornerRadiusPx,
                chipCornerRadiusPx,
                chipStrokePaint
            )

            chipTextPaint.alpha = (255 * alpha).toInt()
            val fm = chipTextPaint.fontMetrics
            val textY = py - (fm.ascent + fm.descent) / 2f
            canvas.drawText(label, 0, label.length, px, textY, chipTextPaint)
        }
    }

    // ---------------------------------------------------------------------
    // Math / animation helpers
    // ---------------------------------------------------------------------

    /** Hexagon vertices start at the top (-90deg) and go clockwise every 60deg, matching the source. */
    private fun angleForIndex(i: Int, count: Int = 6): Float {
        val step = (2 * Math.PI / count).toFloat()
        return (-Math.PI / 2).toFloat() + i * step
    }

    /** 0..1 phase of a repeating period, with an optional start delay (delay shifts the phase). */
    private fun phase(elapsedMs: Long, periodMs: Long, delayMs: Long = 0L): Float {
        val t = elapsedMs - delayMs
        val m = ((t % periodMs) + periodMs) % periodMs
        return m.toFloat() / periodMs
    }

    /** Smooth 0->1->0 "breathing" easing matching the CSS `np`/`pb` keyframes (ease-in-out sine). */
    private fun breathe(elapsedMs: Long, periodMs: Long, delayMs: Long = 0L): Float {
        val p = phase(elapsedMs, periodMs, delayMs)
        return (1f - cos((p * 2f * Math.PI).toFloat())) / 2f
    }

    private fun withAlpha(@ColorInt color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    private fun designToPx(designValue: Float): Float = designValue * (artSizePx / DESIGN_SIZE)
    private fun pxToDesign(px: Float): Float = px / (artSizePx / DESIGN_SIZE)

    private fun dp(v: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics)

    private fun sp(v: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)

    // ---------------------------------------------------------------------
    // Default icon paths (24x24 viewBox), hand-ported 1:1 from the source SVG
    // ---------------------------------------------------------------------

    private fun buildDefaultIconPaths() {
        // Heart glyph -- center icon default
        // Source: M12 20.3C12 20.3 3.6 15.3 3.6 9.4C3.6 6.7 5.8 4.8 8.2 4.8C9.8 4.8 11.2 5.7 12 7
        //         C12.8 5.7 14.2 4.8 15.8 4.8C18.2 4.8 20.4 6.7 20.4 9.4C20.4 15.3 12 20.3 12 20.3Z
        heartPath.apply {
            moveTo(12f, 20.3f)
            cubicTo(12f, 20.3f, 3.6f, 15.3f, 3.6f, 9.4f)
            cubicTo(3.6f, 6.7f, 5.8f, 4.8f, 8.2f, 4.8f)
            cubicTo(9.8f, 4.8f, 11.2f, 5.7f, 12f, 7f)
            cubicTo(12.8f, 5.7f, 14.2f, 4.8f, 15.8f, 4.8f)
            cubicTo(18.2f, 4.8f, 20.4f, 6.7f, 20.4f, 9.4f)
            cubicTo(20.4f, 15.3f, 12f, 20.3f, 12f, 20.3f)
            close()
        }
    }
}
