package com.humotron.app.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.humotron.app.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.toColorInt

/**
 * Single self-contained custom View that reproduces the entire ".uphero" animated scene
 * from the upload_intro export in one canvas: a scanned document (outline + content lines
 * + a moving scanline), a row of markers traveling from the document toward the device,
 * and a device ring with a breathing core:
 * 
 * 
 * .upscanline{animation:upscan 2.6s ease-in-out infinite;}
 * 
 * @keyframes upscan{0%,100%{transform:translateY(-26px);opacity:.25}50%{transform:translateY(26px);opacity:1}}
 * .updot{opacity:0;animation:updot 2.6s ease-out infinite;}                (x3, staggered .9s / 1.8s)
 * @keyframes updot{0%{opacity:0;transform:translateX(0) scale(.6)}25%{opacity:1}70%{opacity:1}100%{opacity:0;transform:translateX(58px) scale(.9)}}
 * .hcore{animation:hbreathe 2.2s ease-in-out infinite;}
 * @keyframes hbreathe{0%,100%{opacity:.4;transform:scale(.9)}50%{opacity:.92;transform:scale(1.05)}}
 * 
 * 
 * Everything is drawn in a fixed 280x150 reference space (matching the original SVG
 * viewBox) and scaled to fit the view's content box (width/height minus padding), so the
 * view behaves like any other View for layout_margin / layout_width / layout_height and
 * needs no special host container.
 * 
 * 
 * Three independent animatable properties (each with a public getter/setter, so you can
 * drive them yourself with ObjectAnimator instead of the built-in animations):
 * - scanPhase    (0-1, repeats every scanDurationMs)    — scanline position + opacity
 * - dotPhase     (0-1, repeats every dotDurationMs)     — all dots' travel, staggered internally
 * - breathePhase (0-1, repeats every breatheDurationMs) — device core scale + opacity
 * 
 * 
 * Call startAnimating() for the built-in infinite animations matching the original
 * durations/easing, or stopAnimating() to cancel them (also called automatically from
 * onDetachedFromWindow). autoStart="true" (the default) starts them as soon as the view
 * attaches to a window.
 */
class UploadHeroView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    // ===== configurable (via attrs / setters) =====
    @ColorInt
    private var documentColor = "#C4F23E".toColorInt()

    @ColorInt
    private var deviceColor = "#5FB7C4".toColorInt()
    private var documentCornerRadius = 10f // in reference units (~px at 1:1 scale)
    private var dotCount = 3
    private var showWaveIcon = true
    private var waveEndMargin = 10f // in reference units
    private var deviceEndMargin = 36f // in reference units
    var scanDurationMs: Int = 2600
    var dotDurationMs: Int = 2600
    var breatheDurationMs: Int = 2200
    var isAutoStart: Boolean = true

    // ===== animatable properties =====
    private var scanPhase = 0f
    private var dotPhase = 0f
    private var breathePhase = 0f

    // ===== paints =====
    private val docOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val docLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scanlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val deviceRingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val deviceCorePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var scanAnimator: ValueAnimator? = null
    private var dotAnimator: ValueAnimator? = null
    private var breatheAnimator: ValueAnimator? = null

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.UploadHeroView)
            documentColor = a.getColor(R.styleable.UploadHeroView_documentColor, documentColor)
            deviceColor = a.getColor(R.styleable.UploadHeroView_deviceColor, deviceColor)
            documentCornerRadius = a.getDimension(
                R.styleable.UploadHeroView_documentCornerRadius,
                documentCornerRadius
            )
            dotCount = a.getInt(R.styleable.UploadHeroView_dotCount, dotCount)
            showWaveIcon = a.getBoolean(R.styleable.UploadHeroView_showWaveIcon, showWaveIcon)
            waveEndMargin = a.getDimension(R.styleable.UploadHeroView_waveEndMargin, waveEndMargin)
            deviceEndMargin = a.getDimension(R.styleable.UploadHeroView_deviceEndMargin, deviceEndMargin)
            scanDurationMs = a.getInt(R.styleable.UploadHeroView_scanDurationMs, scanDurationMs)
            dotDurationMs = a.getInt(R.styleable.UploadHeroView_dotDurationMs, dotDurationMs)
            breatheDurationMs =
                a.getInt(R.styleable.UploadHeroView_breatheDurationMs, breatheDurationMs)
            this.isAutoStart = a.getBoolean(
                R.styleable.UploadHeroView_autoStart,
                this.isAutoStart
            )
            a.recycle()
        }

        // BlurMaskFilter (used for the glow on the scanline/dots) needs a software layer
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        initPaints()
    }

    private fun initPaints() {
        docOutlinePaint.style = Paint.Style.STROKE
        docOutlinePaint.strokeWidth = 2f

        docLinePaint.style = Paint.Style.FILL

        scanlinePaint.style = Paint.Style.FILL
        scanlinePaint.setMaskFilter(BlurMaskFilter(2.4f, BlurMaskFilter.Blur.NORMAL))

        dotPaint.style = Paint.Style.FILL
        dotPaint.setMaskFilter(BlurMaskFilter(2.4f, BlurMaskFilter.Blur.NORMAL))

        deviceRingPaint.style = Paint.Style.STROKE
        deviceRingPaint.strokeWidth = 2f

        deviceCorePaint.style = Paint.Style.STROKE
        deviceCorePaint.strokeWidth = 2f

        wavePaint.style = Paint.Style.STROKE
        wavePaint.strokeWidth = 1.7f
        wavePaint.strokeCap = Paint.Cap.ROUND
        wavePaint.strokeJoin = Paint.Join.ROUND

        applyColors()
    }

    private fun applyColors() {
        docOutlinePaint.setColor(ColorUtils.setAlphaComponent(documentColor, (255 * 0.5f).toInt()))
        deviceRingPaint.setColor(ColorUtils.setAlphaComponent(deviceColor, (255 * 0.35f).toInt()))
        wavePaint.setColor(deviceColor)
    }

    // ===================== public configuration API =====================
    fun setDocumentColor(@ColorInt color: Int) {
        this.documentColor = color
        applyColors()
        invalidate()
    }

    fun getDocumentColor(): Int {
        return documentColor
    }

    fun setDeviceColor(@ColorInt color: Int) {
        this.deviceColor = color
        applyColors()
        invalidate()
    }

    fun getDeviceColor(): Int {
        return deviceColor
    }

    fun setDocumentCornerRadius(radiusPx: Float) {
        this.documentCornerRadius = radiusPx
        invalidate()
    }

    fun getDocumentCornerRadius(): Float {
        return documentCornerRadius
    }

    /**
     * Clamped to 1-3, matching the original layout's fixed dot lane positions.
     */
    fun setDotCount(count: Int) {
        this.dotCount = max(1, min(3, count))
        invalidate()
    }

    fun getDotCount(): Int {
        return dotCount
    }

    fun setShowWaveIcon(show: Boolean) {
        this.showWaveIcon = show
        invalidate()
    }

    fun isShowWaveIcon(): Boolean {
        return showWaveIcon
    }

    fun setWaveEndMargin(marginPx: Float) {
        this.waveEndMargin = marginPx
        invalidate()
    }

    fun getWaveEndMargin(): Float {
        return waveEndMargin
    }

    fun setDeviceEndMargin(marginPx: Float) {
        this.deviceEndMargin = marginPx
        invalidate()
    }

    fun getDeviceEndMargin(): Float {
        return deviceEndMargin
    }

    // ===================== animatable properties =====================
    fun getScanPhase(): Float {
        return scanPhase
    }

    fun setScanPhase(phase: Float) {
        this.scanPhase = phase % 1f
        invalidate()
    }

    fun getDotPhase(): Float {
        return dotPhase
    }

    fun setDotPhase(phase: Float) {
        this.dotPhase = phase % 1f
        invalidate()
    }

    fun getBreathePhase(): Float {
        return breathePhase
    }

    fun setBreathePhase(phase: Float) {
        this.breathePhase = phase % 1f
        invalidate()
    }

    // ===================== animation control =====================
    /**
     * Starts all three built-in infinite animations, matching the original CSS durations/easing.
     */
    fun startAnimating() {
        stopAnimating()

        scanAnimator = ValueAnimator.ofFloat(0f, 1f)
        scanAnimator!!.setDuration(scanDurationMs.toLong())
        scanAnimator!!.repeatCount = ValueAnimator.INFINITE
        scanAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        scanAnimator!!.addUpdateListener(ValueAnimator.AnimatorUpdateListener { a: ValueAnimator? ->
            setScanPhase(
                a!!.getAnimatedValue() as Float
            )
        })
        scanAnimator!!.start()

        dotAnimator = ValueAnimator.ofFloat(0f, 1f)
        dotAnimator!!.setDuration(dotDurationMs.toLong())
        dotAnimator!!.repeatCount = ValueAnimator.INFINITE
        dotAnimator!!.interpolator = DecelerateInterpolator()
        dotAnimator!!.addUpdateListener(ValueAnimator.AnimatorUpdateListener { a: ValueAnimator? ->
            setDotPhase(
                a!!.getAnimatedValue() as Float
            )
        })
        dotAnimator!!.start()

        breatheAnimator = ValueAnimator.ofFloat(0f, 1f)
        breatheAnimator!!.setDuration(breatheDurationMs.toLong())
        breatheAnimator!!.repeatCount = ValueAnimator.INFINITE
        breatheAnimator!!.interpolator = LinearInterpolator()
        breatheAnimator!!.addUpdateListener(ValueAnimator.AnimatorUpdateListener { a: ValueAnimator? ->
            setBreathePhase(
                a!!.getAnimatedValue() as Float
            )
        })
        breatheAnimator!!.start()
    }

    fun stopAnimating() {
        if (scanAnimator != null) {
            scanAnimator!!.cancel()
            scanAnimator = null
        }
        if (dotAnimator != null) {
            dotAnimator!!.cancel()
            dotAnimator = null
        }
        if (breatheAnimator != null) {
            breatheAnimator!!.cancel()
            breatheAnimator = null
        }
    }

    val isAnimating: Boolean
        get() = scanAnimator != null || dotAnimator != null || breatheAnimator != null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (this.isAutoStart) startAnimating()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimating()
    }

    // ===================== drawing =====================
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // default intrinsic size mirrors the original svg (262x150), used only when the
        // host doesn't constrain both dimensions (e.g. wrap_content)
        val desiredW = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 262f,
            resources.displayMetrics
        ).toInt()
        val desiredH = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 150f,
            resources.displayMetrics
        ).toInt()
        val width = resolveSize(desiredW, widthMeasureSpec)
        val height = resolveSize(desiredH, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentW = (width - getPaddingLeft() - getPaddingRight()).toFloat()
        val contentH = (height - paddingTop - paddingBottom).toFloat()
        if (contentW <= 0f || contentH <= 0f) return

        val scale: Float = min(contentW / REF_W, contentH / REF_H)
        val scaledW: Float = REF_W * scale
        val scaledH: Float = REF_H * scale
        val offsetX = getPaddingLeft() + (contentW - scaledW) / 2f
        val offsetY = paddingTop + (contentH - scaledH) / 2f

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)

        drawDocument(canvas)
        drawTravelingDots(canvas)
        drawDevice(canvas)
        if (showWaveIcon) {
            drawWaveform(canvas)
        }

        canvas.restore()
    }

    private fun drawDocument(canvas: Canvas) {
        // outline: x=32,y=26,w=74,h=98,rx=10
        val docRect = RectF(32f, 26f, (32 + 74).toFloat(), (26 + 98).toFloat())
        canvas.drawRoundRect(docRect, documentCornerRadius, documentCornerRadius, docOutlinePaint)

        // content lines, 22% opacity
        docLinePaint.setColor(ColorUtils.setAlphaComponent(documentColor, (255 * 0.22f).toInt()))
        drawLine(canvas, 44f, 40f, 40f)
        drawLine(canvas, 44f, 52f, 50f)
        drawLine(canvas, 44f, 76f, 50f)
        drawLine(canvas, 44f, 88f, 34f)
        drawLine(canvas, 44f, 100f, 46f)

        // scanline: x=38,y(center)=64 +-26, w=62,h=3,rx=1.5
        // upscan: 0%/100% -> translateY(-26), opacity .25 ; 50% -> translateY(26), opacity 1
        val triangle = 1f - abs(2f * scanPhase - 1f) // 0 -> 0 -> 1 (at .5) -> 0
        val scanY = 64 + (-26 + 52 * triangle)
        val scanAlpha = 0.25f + 0.75f * triangle
        scanlinePaint.setColor(
            ColorUtils.setAlphaComponent(
                documentColor,
                (255 * scanAlpha).toInt()
            )
        )
        val scanRect = RectF(38f, scanY - 1.5f, (38 + 62).toFloat(), scanY + 1.5f)
        canvas.drawRoundRect(scanRect, 1.5f, 1.5f, scanlinePaint)
    }

    private fun drawLine(canvas: Canvas, x: Float, y: Float, width: Float) {
        val r = RectF(x, y, x + width, y + 4)
        canvas.drawRoundRect(r, 2f, 2f, docLinePaint)
    }

    private fun drawTravelingDots(canvas: Canvas) {
        // updot: 0% translateX(0) scale(.6) opacity0 ; 25% opacity1 ; 70% opacity1 ; 100% translateX(58) scale(.9) opacity0
        val laneY = floatArrayOf(50f, 75f, 100f)
        for (i in 0..<dotCount) {
            var t: Float = dotPhase - DOT_DELAY_FRACTION[i]
            if (t < 0f) t += 1f

            val translateX = 58f * t
            val scale = 0.6f + 0.3f * t
            var alpha: Float
            if (t < 0.25f) {
                alpha = t / 0.25f
            } else if (t < 0.70f) {
                alpha = 1f
            } else {
                alpha = 1f - (t - 0.70f) / 0.30f
            }
            alpha = max(0f, min(1f, alpha))

            dotPaint.setColor(ColorUtils.setAlphaComponent(documentColor, (255 * alpha).toInt()))
            val cx = 110f + translateX
            val cy = laneY[i]
            canvas.drawCircle(cx, cy, 3f * scale, dotPaint)
        }
    }

    private fun drawDevice(canvas: Canvas) {
        val cx = REF_W - deviceEndMargin - 32f
        val cy = 75f

        // outer static ring
        canvas.drawCircle(cx, cy, 32f, deviceRingPaint)

        // moving core: oscillates slightly from left-top to right-bottom within the main ring
        val triangle = 1f - abs(2f * breathePhase - 1f)
        val dx = -8f + 16f * triangle 
        val dy = -5f + 10f * triangle
        val coreAlpha = 0.92f - 0.52f * triangle
        deviceCorePaint.setColor(
            ColorUtils.setAlphaComponent(
                deviceColor,
                (255 * coreAlpha).toInt()
            )
        )
        canvas.drawCircle(cx + dx, cy + dy, 20f, deviceCorePaint)
    }

    private fun drawWaveform(canvas: Canvas) {
        // small waveform glyph centered at the end of the view
        val iconSize = 16f
        val s = iconSize / 24f
        val cx = REF_W - waveEndMargin - iconSize / 2f
        val cy = 75f // match the device center Y
        val left = cx - iconSize / 2f
        val top = cy - iconSize / 2f
        val path = Path()
        path.moveTo(left + 3 * s, top + 12 * s)
        path.lineTo(left + 6.4f * s, top + 12 * s)
        path.lineTo(left + 8.4f * s, top + 6 * s)
        path.lineTo(left + 11.4f * s, top + 18 * s)
        path.lineTo(left + 13.9f * s, top + 9 * s)
        path.lineTo(left + 15.3f * s, top + 12 * s)
        path.lineTo(left + 21 * s, top + 12 * s)
        canvas.drawPath(path, wavePaint)
    }

    companion object {
        private const val REF_W = 280f
        private const val REF_H = 150f

        // per-dot delay as a fraction of dotDurationMs, matching the original's 0s/.9s/1.8s over 2.6s
        private val DOT_DELAY_FRACTION = floatArrayOf(0f, 0.9f / 2.6f, 1.8f / 2.6f)
    }
}
