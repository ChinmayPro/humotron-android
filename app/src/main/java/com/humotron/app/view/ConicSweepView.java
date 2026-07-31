package com.humotron.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import com.humotron.app.R;

/**
 * Mirrors the CSS:
 * .sc-sweep{background:conic-gradient(from 0deg, color-mix(in srgb,var(--ac) 32%,transparent), transparent 65deg);
 * animation:scspin 2.6s linear infinite;}
 * <p>
 * Animatable property: sweepRotationDeg (0-360, wraps). Drive it yourself with
 * ObjectAnimator.ofFloat(view, "sweepRotationDeg", 0f, 360f) or call startAnimating()
 * for the built-in infinite linear spin matching the original 2.6s duration.
 */
public class ConicSweepView extends View {

    private float sweepRotationDeg = 0f;
    private int accentColor = Color.parseColor("#C4F23E");
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;

    public ConicSweepView(Context context) {
        this(context, null);
    }

    public ConicSweepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConicSweepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ConicSweepView)) {
                accentColor = a.getColor(R.styleable.ConicSweepView_accentColor, accentColor);
                a.recycle();
            }
        }
    }

    // ===== animatable property =====
    public float getSweepRotationDeg() {
        return sweepRotationDeg;
    }

    public void setSweepRotationDeg(float degrees) {
        this.sweepRotationDeg = degrees % 360f;
        invalidate();
    }

    public void setAccentColor(@ColorInt int color) {
        this.accentColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f;
        if (radius <= 0f) return;

        int accentFaded = ColorUtils.setAlphaComponent(accentColor, (int) (255 * 0.32f));
        int transparent = ColorUtils.setAlphaComponent(accentColor, 0);

        // conic-gradient(from 0deg, accent 32%, transparent 65deg) -> fades out by 65/360 of the sweep
        float[] positions = {0f, 65f / 360f, 1f};
        int[] colors = {accentFaded, transparent, transparent};

        SweepGradient shader = new SweepGradient(cx, cy, colors, positions);
        Matrix matrix = new Matrix();
        matrix.postRotate(sweepRotationDeg, cx, cy);
        shader.setLocalMatrix(matrix);

        paint.setShader(shader);
        canvas.drawCircle(cx, cy, radius, paint);
    }

    /**
     * Starts the built-in infinite linear spin (2.6s / 360deg, matches @keyframes scspin).
     */
    public void startAnimating() {
        stopAnimating();
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(2600);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> setSweepRotationDeg((float) a.getAnimatedValue()));
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
