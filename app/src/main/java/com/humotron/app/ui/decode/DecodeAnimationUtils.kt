package com.humotron.app.ui.decode

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.PathInterpolator
import androidx.core.view.children
import com.google.android.material.card.MaterialCardView
import com.humotron.app.R

/**
 * Replicates the HTML prototype's card-in animation:
 *   @keyframes cardIn { from { opacity:0; transform:translateY(12px); } to { opacity:1; transform:translateY(0); } }
 *   .scroll .card { animation: cardIn .42s cubic-bezier(.22,.61,.36,1) backwards; }
 *   Each card gets staggered delay: i * 0.045s
 *
 * Also provides cross-fade + slide helpers for sub-screen transitions.
 */
object DecodeAnimationUtils {

    // Matches CSS cubic-bezier(.22, .61, .36, 1)
    private val CARD_IN_INTERPOLATOR = PathInterpolator(0.22f, 0.61f, 0.36f, 1f)
    private const val CARD_IN_DURATION = 420L
    private const val CARD_STAGGER_DELAY = 45L  // 0.045s per card
    private const val CARD_TRANSLATE_Y = 36f     // dp converted approximately to px (12dp * 3)

    // Screen transition constants
    private const val SCREEN_CROSS_DURATION = 280L
    private const val SCREEN_SLIDE_OFFSET = 80f

    /**
     * Animate cards inside a container with staggered fade-in + slide-up.
     * Finds all direct and nested MaterialCardView children and animates them.
     */
    fun animateCardsIn(container: View) {
        val observer = container.viewTreeObserver
        observer.addOnPreDrawListener(object : android.view.ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (container.viewTreeObserver.isAlive) {
                    container.viewTreeObserver.removeOnPreDrawListener(this)
                }

                val cards = mutableListOf<View>()
                findAnimatableViews(container, cards)

                // Only animate cards that have not been animated yet to avoid duplicate animations on scroll/expand
                val unanimatedCards = cards.filter { it.getTag(R.id.tag_animated) != true }

                if (unanimatedCards.isEmpty()) return true

                // Stagger animate cards
                unanimatedCards.forEachIndexed { index, card ->
                    card.setTag(R.id.tag_animated, true)
                    
                    val targetAlpha = card.alpha
                    card.alpha = 0f
                    card.translationY = CARD_TRANSLATE_Y

                    val fadeIn = ObjectAnimator.ofFloat(card, View.ALPHA, 0f, targetAlpha)
                    val slideUp = ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, CARD_TRANSLATE_Y, 0f)

                    AnimatorSet().apply {
                        playTogether(fadeIn, slideUp)
                        duration = CARD_IN_DURATION
                        interpolator = CARD_IN_INTERPOLATOR
                        startDelay = index * CARD_STAGGER_DELAY
                        start()
                    }
                }
                return true
            }
        })
    }

    /**
     * Animate the entire layout container appearing (for sub-screen transitions).
     * The outgoing view fades + slides left, the incoming view fades + slides from right.
     */
    fun crossFadeScreens(
        outgoing: View?,
        incoming: View,
        onOutDone: (() -> Unit)? = null,
        animateCards: Boolean = true
    ) {
        // Quick exit for outgoing
        if (outgoing != null && outgoing.visibility == View.VISIBLE) {
            val fadeOut = ObjectAnimator.ofFloat(outgoing, View.ALPHA, 1f, 0f)
            val slideOut = ObjectAnimator.ofFloat(outgoing, View.TRANSLATION_X, 0f, -SCREEN_SLIDE_OFFSET)

            AnimatorSet().apply {
                playTogether(fadeOut, slideOut)
                duration = 200
                interpolator = AccelerateInterpolator(1.2f)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        outgoing.visibility = View.GONE
                        outgoing.alpha = 1f
                        outgoing.translationX = 0f
                        onOutDone?.invoke()
                    }
                })
                start()
            }
        } else {
            outgoing?.visibility = View.GONE
            onOutDone?.invoke()
        }

        // Entrance for incoming
        incoming.alpha = 0f
        incoming.translationX = SCREEN_SLIDE_OFFSET
        incoming.visibility = View.VISIBLE

        val fadeIn = ObjectAnimator.ofFloat(incoming, View.ALPHA, 0f, 1f)
        val slideIn = ObjectAnimator.ofFloat(incoming, View.TRANSLATION_X, SCREEN_SLIDE_OFFSET, 0f)

        AnimatorSet().apply {
            playTogether(fadeIn, slideIn)
            duration = SCREEN_CROSS_DURATION
            interpolator = DecelerateInterpolator(1.4f)
            startDelay = 60 // slight delay so outgoing starts first
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (animateCards) {
                        animateCardsIn(incoming)
                    }
                }
            })
            start()
        }
    }

    /**
     * Animate back transition: incoming slides from left, outgoing slides right.
     */
    fun crossFadeBack(
        outgoing: View?,
        incoming: View,
        animateCards: Boolean = true
    ) {
        // Quick exit for outgoing (slide right)
        if (outgoing != null && outgoing.visibility == View.VISIBLE) {
            val fadeOut = ObjectAnimator.ofFloat(outgoing, View.ALPHA, 1f, 0f)
            val slideOut = ObjectAnimator.ofFloat(outgoing, View.TRANSLATION_X, 0f, SCREEN_SLIDE_OFFSET)

            AnimatorSet().apply {
                playTogether(fadeOut, slideOut)
                duration = 200
                interpolator = AccelerateInterpolator(1.2f)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        outgoing.visibility = View.GONE
                        outgoing.alpha = 1f
                        outgoing.translationX = 0f
                    }
                })
                start()
            }
        } else {
            outgoing?.visibility = View.GONE
        }

        // Entrance for incoming (slide from left)
        incoming.alpha = 0f
        incoming.translationX = -SCREEN_SLIDE_OFFSET
        incoming.visibility = View.VISIBLE

        val fadeIn = ObjectAnimator.ofFloat(incoming, View.ALPHA, 0f, 1f)
        val slideIn = ObjectAnimator.ofFloat(incoming, View.TRANSLATION_X, -SCREEN_SLIDE_OFFSET, 0f)

        AnimatorSet().apply {
            playTogether(fadeIn, slideIn)
            duration = SCREEN_CROSS_DURATION
            interpolator = DecelerateInterpolator(1.4f)
            startDelay = 60
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (animateCards) {
                        animateCardsIn(incoming)
                    }
                }
            })
            start()
        }
    }

    /**
     * Simple fade-in for a single view.
     */
    fun fadeIn(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    /**
     * Expand/collapse animation for card detail sections.
     * Matches the HTML prototype's chevron rotation + content reveal.
     */
    fun toggleExpand(
        content: View,
        chevron: View?,
        expand: Boolean
    ) {
        if (expand) {
            content.visibility = View.VISIBLE
            content.alpha = 0f
            content.translationY = -12f

            val fadeIn = ObjectAnimator.ofFloat(content, View.ALPHA, 0f, 1f)
            val slideDown = ObjectAnimator.ofFloat(content, View.TRANSLATION_Y, -12f, 0f)

            AnimatorSet().apply {
                playTogether(fadeIn, slideDown)
                duration = 250
                interpolator = DecelerateInterpolator()
                start()
            }
        } else {
            val fadeOut = ObjectAnimator.ofFloat(content, View.ALPHA, 1f, 0f)
            val slideUp = ObjectAnimator.ofFloat(content, View.TRANSLATION_Y, 0f, -12f)

            AnimatorSet().apply {
                playTogether(fadeOut, slideUp)
                duration = 200
                interpolator = AccelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        content.visibility = View.GONE
                        content.alpha = 1f
                        content.translationY = 0f
                    }
                })
                start()
            }
        }

        // Chevron rotation (matches CSS .chev { transition: transform .25s; } .open .chev { transform: rotate(180deg); })
        chevron?.let {
            ObjectAnimator.ofFloat(it, View.ROTATION, if (expand) 0f else 180f, if (expand) 180f else 0f).apply {
                duration = 250
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    /**
     * Card press animation: scale down slightly on press (matches CSS .card:active { transform: scale(.985); })
     */
    fun addPressEffect(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.985f).scaleY(0.985f).setDuration(120).start()
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
            }
            false // Don't consume the event — let click listeners work
        }
    }

    /**
     * Header transition animation for back button / tab header toggle.
     */
    fun animateHeaderTransition(
        header: View,
        fromTabToBack: Boolean
    ) {
        if (fromTabToBack) {
            // Slide title slightly from right
            header.alpha = 0f
            header.translationX = 20f
            header.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(250)
                .setInterpolator(DecelerateInterpolator())
                .start()
        } else {
            // Slide title slightly from left
            header.alpha = 0f
            header.translationX = -20f
            header.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(250)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    /**
     * Recursively find MaterialCardView and other animatable card-like views.
     */
    private fun findAnimatableViews(view: View, result: MutableList<View>) {
        if (view.id == R.id.cardMetric || 
            view.id == R.id.cardPastInsight || 
            view.tag == "anim_card" ||
            view is MaterialCardView ||
            view.parent is androidx.recyclerview.widget.RecyclerView
        ) {
            result.add(view)
            return
        }
        if (view is ViewGroup) {
            for (child in view.children) {
                findAnimatableViews(child, result)
            }
        }
    }
}

