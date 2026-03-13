package com.humotron.app.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.humotron.app.R

class BorderedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // Default values
    private var borderColor: Int = Color.BLACK
    private var cornerRadius: Float = 0f
    private var borderWidth: Int = 1 // Default border width in pixels
    private var leftPadding: Int = 0 // Default padding in pixels
    private var topPadding: Int = 0 // Default padding in pixels
    private var rightPadding: Int = 0 // Default padding in pixels
    private var bottomPadding: Int = 0 // Default padding in pixels

    init {
        // Get custom attributes from XML
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BorderedTextView,
            defStyleAttr,
            0
        ).apply {
            try {
                borderColor = getColor(R.styleable.BorderedTextView_borderColor, Color.BLACK)
                cornerRadius = getDimension(R.styleable.BorderedTextView_cornerRadius, 0f)
                borderWidth = getDimensionPixelSize(R.styleable.BorderedTextView_borderWidth, 1)
                leftPadding = getDimensionPixelSize(R.styleable.BorderedTextView_leftPadding, 0)
                topPadding = getDimensionPixelSize(R.styleable.BorderedTextView_topPadding, 0)
                rightPadding = getDimensionPixelSize(R.styleable.BorderedTextView_rightPadding, 0)
                bottomPadding = getDimensionPixelSize(R.styleable.BorderedTextView_bottomPadding, 0)
            } finally {
                recycle() // Important: Recycle the typed array
            }
        }

        // Apply the initial properties to the view
        applyProperties()
    }

    /**
     * Applies the current border, corner radius, and padding to the TextView's background.
     * This method is called during initialization and can be called programmatically
     * to update the appearance.
     */
    private fun applyProperties() {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setStroke(borderWidth, borderColor)
            cornerRadius = this@BorderedTextView.cornerRadius
        }
        background = drawable
        setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
    }

    /**
     * Sets a new border color and redraws the view.
     *
     * @param color The new border color as an integer.
     */
    fun setBorderColor(color: Int) {
        borderColor = color
        applyProperties()
    }

    /**
     * Sets a new corner radius and redraws the view.
     *
     * @param radius The new corner radius in pixels (float).
     */
    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        applyProperties()
    }

    /**
     * Sets a new border width and redraws the view.
     *
     * @param width The new border width in pixels (integer).
     */
    fun setBorderWidth(width: Int) {
        borderWidth = width
        applyProperties()
    }


}
