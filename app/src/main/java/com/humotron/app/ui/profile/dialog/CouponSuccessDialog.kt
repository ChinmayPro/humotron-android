package com.humotron.app.ui.profile.dialog

import android.animation.Animator
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.humotron.app.databinding.DialogCouponSuccessBinding

class CouponSuccessDialog : DialogFragment() {

    private var _binding: DialogCouponSuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCouponSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Handle missing fonts in Lottie to prevent crashes
        binding.lottieSuccess.setFontAssetDelegate(object : com.airbnb.lottie.FontAssetDelegate() {
            override fun fetchFont(fontFamily: String?): Typeface {
                return Typeface.DEFAULT
            }
        })

        // Ensure the dialog is centered and has the correct width
        dialog?.window?.let { window ->
            window.setGravity(android.view.Gravity.CENTER)
            val width = resources.getDimensionPixelSize(com.humotron.app.R.dimen._280dp)
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        var playCount = 0
        binding.lottieSuccess.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            
            override fun onAnimationEnd(animation: Animator) {
                playCount++
                if (playCount >= 2) {
                    dismiss()
                } else {
                    binding.lottieSuccess.playAnimation()
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CouponSuccessDialog"
        fun newInstance() = CouponSuccessDialog()
    }
}
