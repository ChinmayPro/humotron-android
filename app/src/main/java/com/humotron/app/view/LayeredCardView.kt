package com.humotron.app.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.card.MaterialCardView
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.humotron.app.R

data class LayerCard(
    val id: Int,
    val title: String,
    val description: String,
    val backgroundColor: Int,
)

class LayeredCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cardViews = mutableListOf<MaterialCardView>()
    private var expandedCard: MaterialCardView? = null

    /**
     * Visible spacing between cards from TOP + END side
     */
    private val revealMargin = dp(60)

    /**
     * XML customizable properties
     */
    private var titleTextSize = 18f
    private var descriptionTextSize = 15f
    private var plusTextSize = 30f

    private var titleTextColor = 0
    private var descriptionTextColor = 0
    private var plusTextColor = 0

    private var titleTypeface: Typeface? = null
    private var descriptionTypeface: Typeface? = null
    private var plusTypeface: Typeface? = null

    init {
        clipChildren = false
        clipToPadding = false

        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.LayeredCardView
        )

        titleTextSize = typedArray.getDimension(
            R.styleable.LayeredCardView_titleTextSize,
            sp(18).toFloat()
        ) / resources.displayMetrics.scaledDensity

        descriptionTextSize = typedArray.getDimension(
            R.styleable.LayeredCardView_descriptionTextSize,
            sp(15).toFloat()
        ) / resources.displayMetrics.scaledDensity

        plusTextSize = typedArray.getDimension(
            R.styleable.LayeredCardView_plusTextSize,
            sp(30).toFloat()
        ) / resources.displayMetrics.scaledDensity

        titleTextColor = typedArray.getColor(
            R.styleable.LayeredCardView_titleTextColor,
            ContextCompat.getColor(context, android.R.color.white)
        )

        descriptionTextColor = typedArray.getColor(
            R.styleable.LayeredCardView_descriptionTextColor,
            ContextCompat.getColor(context, android.R.color.white)
        )

        plusTextColor = typedArray.getColor(
            R.styleable.LayeredCardView_plusTextColor,
            ContextCompat.getColor(context, android.R.color.white)
        )

        val titleFontResId = typedArray.getResourceId(
            R.styleable.LayeredCardView_titleFontFamily,
            0
        )

        if (titleFontResId != 0) {
            titleTypeface = ResourcesCompat.getFont(context, titleFontResId)
        }

        val descriptionFontResId = typedArray.getResourceId(
            R.styleable.LayeredCardView_descriptionFontFamily,
            0
        )

        if (descriptionFontResId != 0) {
            descriptionTypeface = ResourcesCompat.getFont(context, descriptionFontResId)
        }

        val plusFontResId = typedArray.getResourceId(
            R.styleable.LayeredCardView_plusFontFamily,
            0
        )

        if (plusFontResId != 0) {
            plusTypeface = ResourcesCompat.getFont(context, plusFontResId)
        }

        typedArray.recycle()
    }

    fun setCards(cards: List<LayerCard>) {
        removeAllViews()
        cardViews.clear()
        expandedCard = null

        post {
            val parentWidth = width
            val parentHeight = height

            if (parentWidth == 0 || parentHeight == 0) return@post

            cards.forEachIndexed { index, item ->

                val card = createCard(item)

                /**
                 * Equal reveal from TOP + END
                 * while keeping anchor at BOTTOM + START
                 */
                val offset = revealMargin * index

                val cardWidth = (parentWidth - offset)
                    .coerceAtLeast(dp(160))

                val cardHeight = (parentHeight - offset)
                    .coerceAtLeast(dp(180))

                val params = LayoutParams(
                    cardWidth,
                    cardHeight
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.START
                }

                card.layoutParams = params

                card.tag = CardState(
                    originalWidth = cardWidth,
                    originalHeight = cardHeight,
                    index = index
                )

                addView(card)
                cardViews.add(card)

                card.setOnClickListener {
                    handleCardClick(card)
                }
            }

            updateVisibleTouchAreas()
        }
    }

    private fun createCard(item: LayerCard): MaterialCardView {
        val card = MaterialCardView(context).apply {
            radius = dp(8).toFloat()
            strokeWidth = 0
            cardElevation = dp(0).toFloat()
            setCardBackgroundColor(item.backgroundColor)
            useCompatPadding = false
            preventCornerOverlap = true
        }

        val container = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )

            setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(20)
            )
        }

        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }
        }

        val title = TextView(context).apply {
            text = item.title
            textSize = titleTextSize
            setTextColor(titleTextColor)

            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }
        }

        val description = TextView(context).apply {
            text = item.description
            textSize = descriptionTextSize
            setTextColor(descriptionTextColor)

            alpha = 0f
            visibility = View.GONE

            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START

                /**
                 * Title is at top.
                 * Description appears below title with spacing.
                 */
                topMargin = dp(18)
            }
        }

        val plus = TextView(context).apply {
            text = "+"
            textSize = plusTextSize
            setTextColor(plusTextColor)

            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
            }
        }

        textContainer.addView(title)
        textContainer.addView(description)

        container.addView(textContainer)
        container.addView(plus)

        card.addView(container)

        card.setTag(R.id.description_view_tag, description)
        card.setTag(R.id.plus_view_tag, plus)
        return card
    }

    private fun handleCardClick(card: MaterialCardView) {
        if (expandedCard == card) {
            collapseCard(card)
        } else {
            expandedCard?.let {
                collapseCard(it, animate = false)
            }
            expandCard(card)
        }
    }

    private fun expandCard(card: MaterialCardView) {
        val description =
            card.getTag(R.id.description_view_tag) as TextView

        val plus =
            card.getTag(R.id.plus_view_tag) as TextView

        card.bringToFront()
        expandedCard = card

        TransitionManager.beginDelayedTransition(
            this,
            ChangeBounds().apply {
                duration = 350
            }
        )

        val params = card.layoutParams as LayoutParams
        params.width = width
        params.height = height
        params.gravity = Gravity.BOTTOM or Gravity.START
        card.layoutParams = params

        plus.visibility = View.GONE

        description.visibility = View.VISIBLE

        ObjectAnimator.ofFloat(
            description,
            "alpha",
            0f,
            1f
        ).apply {
            duration = 250
            start()
        }
    }

    private fun collapseCard(
        card: MaterialCardView,
        animate: Boolean = true,
    ) {
        val state = card.tag as CardState

        val description =
            card.getTag(R.id.description_view_tag) as TextView

        val plus =
            card.getTag(R.id.plus_view_tag) as TextView

        if (animate) {
            TransitionManager.beginDelayedTransition(
                this,
                ChangeBounds().apply {
                    duration = 350
                }
            )
        }

        val params = card.layoutParams as LayoutParams
        params.width = state.originalWidth
        params.height = state.originalHeight
        params.gravity = Gravity.BOTTOM or Gravity.START
        card.layoutParams = params

        description.visibility = View.GONE
        description.alpha = 0f

        plus.visibility = View.VISIBLE

        expandedCard = null

        restoreStackOrder()
    }

    private fun restoreStackOrder() {
        val sorted = cardViews.sortedBy {
            (it.tag as CardState).index
        }

        sorted.forEach {
            it.bringToFront()
        }
    }

    private fun updateVisibleTouchAreas() {
        cardViews.forEachIndexed { index, card ->

            card.setOnTouchListener { v, event ->

                if (expandedCard == card) {
                    return@setOnTouchListener false
                }

                if (event.action == MotionEvent.ACTION_DOWN) {

                    val isFrontMost =
                        index == cardViews.lastIndex

                    if (isFrontMost) {
                        return@setOnTouchListener false
                    }

                    val blockedRect = Rect(
                        0,
                        revealMargin,
                        v.width - revealMargin,
                        v.height
                    )

                    val touchedInsideBlocked =
                        blockedRect.contains(
                            event.x.toInt(),
                            event.y.toInt()
                        )

                    touchedInsideBlocked
                } else {
                    false
                }
            }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun sp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    data class CardState(
        val originalWidth: Int,
        val originalHeight: Int,
        val index: Int,
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)

        val squareMeasureSpec = MeasureSpec.makeMeasureSpec(
            width,
            MeasureSpec.EXACTLY
        )

        super.onMeasure(
            squareMeasureSpec,
            squareMeasureSpec
        )

        setMeasuredDimension(width, width)
    }
}