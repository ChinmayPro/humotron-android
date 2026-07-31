package com.humotron.app.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;

import com.humotron.app.R;

/**
 * Mirrors the CSS / inline SVG:
 * <circle stroke="rgba(255,255,255,.07)" stroke-width="6">                (track)
 * <circle stroke="var(--lime)" stroke-width="6" stroke-linecap="round"
 * transform="rotate(-90 110 110)" stroke-dashoffset="...">        (progress)
 * <p>
 * Animatable property: progressPercent (0-100). The JS-driven value in the export is a
 * static snapshot (43%); use animateTo() to ease the ring toward a new reading, or drive
 * "progressPercent" directly with your own ObjectAnimator.
 */
public class RingProgressView extends View {

    private float progressPercent = 0f;
    private int trackColor = Color.parseColor("#12FFFFFF"); // rgba(255,255,255,.07)
    private int progressColor = Color.parseColor("#C4F23E");
    private float strokeWidthPx;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private ObjectAnimator animator;

    public RingProgressView(Context context) {
        this(context, null);
    }

    public RingProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strokeWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f,
                getResources().getDisplayMetrics());

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RingProgressView);
            trackColor = a.getColor(R.styleable.RingProgressView_trackColorRP, trackColor);
            progressColor = a.getColor(R.styleable.RingProgressView_progressColorRP, progressColor);
            strokeWidthPx = a.getDimension(R.styleable.RingProgressView_ringStrokeWidth, strokeWidthPx);
            progressPercent = a.getFloat(R.styleable.RingProgressView_progressPercent, progressPercent);
            a.recycle();
        }

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidthPx);
        trackPaint.setColor(trackColor);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidthPx);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float inset = strokeWidthPx / 2f;
        oval.set(inset, inset, w - inset, h - inset);
    }

    // ===== animatable property =====
    public float getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(float percent) {
        this.progressPercent = Math.max(0f, Math.min(100f, percent));
        invalidate();
    }

    public void setTrackColor(@ColorInt int color) {
        this.trackColor = color;
        trackPaint.setColor(color);
        invalidate();
    }

    public void setProgressColor(@ColorInt int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(oval, 0f, 360f, false, trackPaint);
        float sweep = 360f * (progressPercent / 100f);
        canvas.drawArc(oval, -90f, sweep, false, progressPaint);
    }

    /**
     * Eases the ring from its current value to {@code targetPercent} over {@code durationMs}.
     */
    public void animateTo(float targetPercent, long durationMs) {
        if (animator != null) animator.cancel();
        animator = ObjectAnimator.ofFloat(this, "progressPercent", progressPercent, targetPercent);
        animator.setDuration(durationMs);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
    }
}
