package com.humotron.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import com.humotron.app.R;

/**
 * Mirrors the inline SVG .sc-graph: 6 dashed spokes from the center to a hexagon of nodes,
 * each node breathing in opacity on a staggered delay, plus a soft pulsing glow behind a
 * solid center dot:
 * .sc-node{animation:scnode 2.4s ease-in-out infinite;}         (per node, staggered delay)
 *
 * @keyframes scnode{0%,100%{opacity:.35}50%{opacity:1}}
 * center glow circle: same "scnode" keyframe but run at 2.2s (no stagger)
 * <p>
 * Two independent animatable properties, matching the two different CSS durations:
 * - breathPhase (0-1, repeats every 2.4s): drives the 6 outer node dots
 * - glowPhase   (0-1, repeats every 2.2s): drives the center glow circle
 * <p>
 * All drawing respects the view's own padding (getPaddingLeft/Top/Right/Bottom), so you
 * can inset the scene with android:padding instead of relying on outer layout margins.
 * <p>
 * Configurable via XML attrs:
 * - app:nodeColor           color of the 6 outer node dots
 * - app:graphAccentColor    color of the dashed spokes + center glow
 * - app:centerDotSize       radius (dimension) of the solid center dot, default 13dp
 * - app:centerDotColor      color of the solid center dot, defaults to graphAccentColor
 * <p>
 * Static / non-animated: the 6 dashed spokes and the solid center dot.
 */
public class NodeGraphView extends View {

    // fractions of the 2.4s node cycle, taken from the original animation-delay values
    // (0s, .22s, .44s, .66s, .88s, 1.1s) / 2.4s
    private static final float[] NODE_DELAY_FRACTION = {
            0f, 0.22f / 2.4f, 0.44f / 2.4f, 0.66f / 2.4f, 0.88f / 2.4f, 1.1f / 2.4f
    };

    private float breathPhase = 0f;
    private float glowPhase = 0f;

    private int nodeColor = Color.parseColor("#A9C2BE");
    private int accentColor = Color.parseColor("#C4F23E");
    private int centerDotColor = Color.parseColor("#C4F23E");
    private float centerDotRadiusPx;

    private final Paint spokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ValueAnimator breathAnimator;
    private ValueAnimator glowAnimator;

    private final float density;

    public NodeGraphView(Context context) {
        this(context, null);
    }

    public NodeGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NodeGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = getResources().getDisplayMetrics().density;
        centerDotRadiusPx = 13f * density;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NodeGraphView);
            nodeColor = a.getColor(R.styleable.NodeGraphView_nodeColor, nodeColor);
            accentColor = a.getColor(R.styleable.NodeGraphView_graphAccentColor, accentColor);
            centerDotColor = a.getColor(R.styleable.NodeGraphView_centerDotColor, accentColor);
            centerDotRadiusPx = a.getDimension(R.styleable.NodeGraphView_centerDotSize, centerDotRadiusPx);
            a.recycle();
        }

        spokePaint.setStyle(Paint.Style.STROKE);
        spokePaint.setStrokeWidth(1f * density);
        spokePaint.setPathEffect(new DashPathEffect(new float[]{3f * density, 5f * density}, 0f));
        spokePaint.setColor(ColorUtils.setAlphaComponent(accentColor, (int) (255 * 0.4f)));

        nodePaint.setStyle(Paint.Style.FILL);

        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(accentColor);

        centerDotPaint.setStyle(Paint.Style.FILL);
        centerDotPaint.setColor(centerDotColor);
    }

    // ===== animatable properties =====
    public float getBreathPhase() {
        return breathPhase;
    }

    public void setBreathPhase(float phase) {
        this.breathPhase = phase % 1f;
        invalidate();
    }

    public float getGlowPhase() {
        return glowPhase;
    }

    public void setGlowPhase(float phase) {
        this.glowPhase = phase % 1f;
        invalidate();
    }

    public void setNodeColor(@ColorInt int color) {
        this.nodeColor = color;
        invalidate();
    }

    public void setGraphAccentColor(@ColorInt int color) {
        this.accentColor = color;
        spokePaint.setColor(ColorUtils.setAlphaComponent(color, (int) (255 * 0.4f)));
        glowPaint.setColor(color);
        invalidate();
    }

    public void setCenterDotColor(@ColorInt int color) {
        this.centerDotColor = color;
        centerDotPaint.setColor(color);
        invalidate();
    }

    public void setCenterDotSize(float radiusPx) {
        this.centerDotRadiusPx = radiusPx;
        invalidate();
    }

    /**
     * Raised-cosine ease matching @keyframes scnode{0%,100%{.35} 50%{1}}.
     */
    private static float breathOpacity(float t) {
        return 0.35f + 0.65f * (1f - (float) Math.cos(2 * Math.PI * t)) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        float contentWidth = getWidth() - paddingLeft - getPaddingRight();
        float contentHeight = getHeight() - paddingTop - getPaddingBottom();
        if (contentWidth <= 0f || contentHeight <= 0f) return;

        float cx = paddingLeft + contentWidth / 2f;
        float cy = paddingTop + contentHeight / 2f;
        float viewRadius = Math.min(contentWidth, contentHeight) / 2f;
        // hexagon radius is 72/110 of the half-size in the original 220 viewBox
        float hexRadius = viewRadius * (72f / 110f);
        float nodeRadius = 3.4f * density;

        // spokes + nodes, top vertex first then clockwise every 60deg
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(-90 + 60 * i);
            float nx = cx + hexRadius * (float) Math.cos(angle);
            float ny = cy + hexRadius * (float) Math.sin(angle);

            canvas.drawLine(cx, cy, nx, ny, spokePaint);

            float t = breathPhase - NODE_DELAY_FRACTION[i];
            if (t < 0f) t += 1f;
            float opacity = breathOpacity(t);
            nodePaint.setColor(ColorUtils.setAlphaComponent(nodeColor, (int) (255 * opacity)));
            canvas.drawCircle(nx, ny, nodeRadius, nodePaint);
        }

        // center glow (breathes .35..1 of a .14 base opacity, per the original's 2.2s cycle)
        float glowOpacity = breathOpacity(glowPhase) * 0.14f;
        glowPaint.setAlpha((int) (255 * glowOpacity));
        canvas.drawCircle(cx, cy, 26f * density, glowPaint);

        // solid center dot, static — size/color configurable via centerDotSize / centerDotColor
        canvas.drawCircle(cx, cy, centerDotRadiusPx, centerDotPaint);
    }

    /**
     * Starts both built-in infinite breathing animations (2.4s nodes / 2.2s center glow).
     */
    public void startAnimating() {
        stopAnimating();

        breathAnimator = ValueAnimator.ofFloat(0f, 1f);
        breathAnimator.setDuration(2400);
        breathAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathAnimator.setInterpolator(new LinearInterpolator());
        breathAnimator.addUpdateListener(a -> setBreathPhase((float) a.getAnimatedValue()));
        breathAnimator.start();

        glowAnimator = ValueAnimator.ofFloat(0f, 1f);
        glowAnimator.setDuration(2200);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setInterpolator(new LinearInterpolator());
        glowAnimator.addUpdateListener(a -> setGlowPhase((float) a.getAnimatedValue()));
        glowAnimator.start();
    }

    public void stopAnimating() {
        if (breathAnimator != null) {
            breathAnimator.cancel();
            breathAnimator = null;
        }
        if (glowAnimator != null) {
            glowAnimator.cancel();
            glowAnimator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimating();
    }
}
