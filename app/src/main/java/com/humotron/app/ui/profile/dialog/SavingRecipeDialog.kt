package com.humotron.app.ui.profile.dialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.humotron.app.R
import com.humotron.app.databinding.DialogSavingRecipeBinding

class SavingRecipeDialog(
    context: Context,
    private val onCompleted: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogSavingRecipeBinding
    private var progressAnimator: ValueAnimator? = null
    private var isApiDone = false
    private var isAnimationDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogSavingRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#E6081315")))
        setCancelable(false)

        startProgressAnimation()
    }

    private fun startProgressAnimation() {
        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = 3200 // 3.2 seconds smooth & gradual progress
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Int
                binding.circularProgress.progress = progress
                binding.tvPercentage.text = progress.toString()

                if (progress >= 50 && binding.tvStep2Text.currentTextColor != Color.WHITE) {
                    binding.tvStep2Number.background = ContextCompat.getDrawable(context, R.drawable.bg_circle_step_lime)
                    binding.tvStep2Number.setTextColor(Color.parseColor("#0D1618"))
                    binding.tvStep2Text.setTextColor(Color.WHITE)
                    binding.tvStep2Text.setTypeface(null, android.graphics.Typeface.BOLD)
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isAnimationDone = true
                    checkAndShowSuccess()
                }
            })
            start()
        }
    }

    fun onApiSuccess() {
        isApiDone = true
        checkAndShowSuccess()
    }

    fun onApiError() {
        dismiss()
        onCompleted()
    }

    private fun checkAndShowSuccess() {
        // Wait until progress animation finishes 100% AND API call returns
        if (isAnimationDone && isApiDone) {
            showSuccessState()
        } else if (isAnimationDone && !isApiDone) {
            // Animation finished but API still loading, keep at 100% until API responds
            binding.circularProgress.progress = 100
            binding.tvPercentage.text = "100"
        }
    }

    private fun showSuccessState() {
        if (binding.layoutSuccessState.visibility == View.VISIBLE) return

        binding.layoutSavingState.animate()
            .alpha(0f)
            .setDuration(220)
            .withEndAction {
                binding.layoutSavingState.visibility = View.GONE
                binding.layoutSuccessState.alpha = 0f
                binding.layoutSuccessState.visibility = View.VISIBLE
                binding.layoutSuccessState.animate()
                    .alpha(1f)
                    .setDuration(250)
                    .start()

                binding.root.postDelayed({
                    dismiss()
                    onCompleted()
                }, 1500)
            }
            .start()
    }

    override fun onDetachedFromWindow() {
        progressAnimator?.cancel()
        super.onDetachedFromWindow()
    }
}
