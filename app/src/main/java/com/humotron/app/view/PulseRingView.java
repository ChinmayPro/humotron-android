package com.humotron.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;

import com.humotron.app.R;

/**
 * Mirrors the CSS:
 * .sc-pulse{width:90px;height:90px;border:1.5px solid var(--ac);border-radius:50%;
 * transform:scale(.4);opacity:0;animation:scpulse 2.8s ease-out infinite;}
 *
 * @keyframes scpulse{0%{transform:scale(.4);opacity:.55}100%{transform:scale(2.6);opacity:0}}
 * <p>
 * One instance == one ring. Use three PulseRingViews stacked in the layout and stagger
 * their startAnimating(startDelayMs) calls at 0ms / 930ms / 1860ms to match
 * ".sc-pulse.b{animation-delay:.93s}.sc-pulse.c{animation-delay:1.86s}".
 * <p>
 * Animatable property: pulseFraction (0-1): 0 = scale .4 / alpha .55, 1 = scale 2.6 / alpha 0.
 */
public class PulseRingView extends View {

    private float pulseFraction = 0f;
    private int ringColor = Color.parseColor("#C4F23E");
    private float baseRadiusPx;
    private float strokeWidthPx;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;

    private static final float SCALE_START = 0.4f;
    private static final float SCALE_END = 2.6f;
    private static final float ALPHA_START = 0.55f;
    private static final float ALPHA_END = 0f;

    public PulseRingView(Context context) {
        this(context, null);
    }

    public PulseRingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PulseRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = getResources().getDisplayMetrics().density;
        baseRadiusPx = 45f * density;
        strokeWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f,
                getResources().getDisplayMetrics());

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PulseRingView);
            ringColor = a.getColor(R.styleable.PulseRingView_ringColor, ringColor);
            baseRadiusPx = a.getDimension(R.styleable.PulseRingView_baseRadius, baseRadiusPx);
            strokeWidthPx = a.getDimension(R.styleable.PulseRingView_pulseStrokeWidth, strokeWidthPx);
            a.recycle();
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidthPx);
    }

    // ===== animatable property =====
    public float getPulseFraction() {
        return pulseFraction;
    }

    public void setPulseFraction(float fraction) {
        this.pulseFraction = Math.max(0f, Math.min(1f, fraction));
        invalidate();
    }

    public void setRingColor(@ColorInt int color) {
        this.ringColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float scale = SCALE_START + (SCALE_END - SCALE_START) * pulseFraction;
        float alpha = ALPHA_START + (ALPHA_END - ALPHA_START) * pulseFraction;

        paint.setColor(ringColor);
        paint.setAlpha((int) (255 * Math.max(0f, alpha)));
        canvas.drawCircle(cx, cy, baseRadiusPx * scale, paint);
    }

    /**
     * Starts the built-in infinite ease-out pulse (2.8s cycle, matches @keyframes scpulse).
     */
    public void startAnimating(long startDelayMs) {
        stopAnimating();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(2800);
        animator.setStartDelay(startDelayMs);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> setPulseFraction((float) a.getAnimatedValue()));
        animator.start();
    }

    public void stopAnimating() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimating();
    }
}
