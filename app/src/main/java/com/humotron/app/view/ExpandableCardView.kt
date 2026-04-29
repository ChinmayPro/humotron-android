package com.humotron.app.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.humotron.app.R
import com.humotron.app.databinding.ViewExpandableCardBinding

class ExpandableCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: ViewExpandableCardBinding =
        ViewExpandableCardBinding.inflate(LayoutInflater.from(context), this)

    init {
        // Set default card properties
        cardElevation = 0f
        radius =
            resources.getDimension(R.dimen._20dp) // or use a fixed value if R.dimen._20dp is not available everywhere
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.InfoCardView,
            0, 0
        ).apply {
            try {
                val title = getString(R.styleable.InfoCardView_cardTitle)
                val description = getString(R.styleable.InfoCardView_cardDescription)
                val mStrokeColor = getColor(
                    R.styleable.InfoCardView_cardStrokeColor,
                    ContextCompat.getColor(context, R.color.graph_card_bg)
                )
                val mStrokeWidth = getDimension(
                    R.styleable.InfoCardView_cardStrokeWidth,
                    resources.getDimension(R.dimen._1dp)
                )
                strokeWidth = mStrokeWidth.toInt()
                strokeColor = mStrokeColor

                binding.tvTitle.text = title
                binding.tvDescription.text = description
            } finally {
                recycle()
            }
        }

        setOnClickListener {
            toggleDescription()
        }
    }

    private fun toggleDescription() {
        if (binding.tvDescription.isVisible) {
            collapseView()
        } else {
            expandView()
        }
    }

    private fun expandView() {
        binding.tvDescription.isVisible = true
        val parent = binding.tvDescription.parent as View
        val availableWidth = parent.width - parent.paddingLeft - parent.paddingRight

        binding.tvDescription.measure(
            View.MeasureSpec.makeMeasureSpec(availableWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.UNSPECIFIED
        )

        val targetHeight = binding.tvDescription.measuredHeight

        binding.tvDescription.layoutParams.height = 0
        binding.tvDescription.requestLayout()

        ValueAnimator.ofInt(0, targetHeight).apply {
            duration = 200
            addUpdateListener {
                val value = it.animatedValue as Int
                binding.tvDescription.layoutParams.height = value
                binding.tvDescription.requestLayout()
            }
            doOnEnd {
                binding.tvDescription.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            start()
        }

        binding.ivArrow.animate()
            .rotation(180f)
            .setDuration(200)
            .start()
    }

    private fun collapseView() {
        val initialHeight = binding.tvDescription.height

        ValueAnimator.ofInt(initialHeight, 0).apply {
            duration = 200
            addUpdateListener {
                val value = it.animatedValue as Int
                binding.tvDescription.layoutParams.height = value
                binding.tvDescription.requestLayout()
            }

            doOnEnd {
                binding.tvDescription.isVisible = false
                binding.tvDescription.layoutParams.height =
                    ViewGroup.LayoutParams.WRAP_CONTENT
            }

            start()
        }
        binding.ivArrow.animate()
            .rotation(0f)
            .setDuration(200)
            .start()
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setDescription(description: String) {
        binding.tvDescription.text = description
    }
}
