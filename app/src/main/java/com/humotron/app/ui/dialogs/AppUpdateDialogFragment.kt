package com.humotron.app.ui.dialogs

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.DialogAppUpdateBinding

class AppUpdateDialogFragment : DialogFragment() {

    private var _binding: DialogAppUpdateBinding? = null
    private val binding get() = _binding!!

    private var isRequired: Boolean = false
    private var titleText: String? = null
    private var descText: String? = null

    private var onUpdateClickListener: (() -> Unit)? = null
    private var onDismissClickListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isRequired = it.getBoolean(ARG_IS_REQUIRED, false)
            titleText = it.getString(ARG_TITLE)
            descText = it.getString(ARG_DESC)
        }
        isCancelable = !isRequired
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAppUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWindow()
        setupUI()
        setupClickListeners()
        startAnimations()
    }

    private fun startAnimations() {
        val offset = -9f * resources.displayMetrics.density
        ObjectAnimator.ofFloat(binding.ivRocketIcon, View.TRANSLATION_Y, 0f, offset, 0f).apply {
            duration = 3600
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        binding.flSparkTrail?.let {
            animateSpark(binding.spark1, 0)
            animateSpark(binding.spark2, 350)
            animateSpark(binding.spark3, 700)
            animateSpark(binding.spark4, 1050)
        }
    }

    private fun animateSpark(view: View?, delayMs: Long) {
        if (view == null) return
        val alpha0 = android.animation.Keyframe.ofFloat(0f, 0f)
        val alpha30 = android.animation.Keyframe.ofFloat(0.3f, 1f)
        val alpha100 = android.animation.Keyframe.ofFloat(1f, 0f)
        val pAlpha = android.animation.PropertyValuesHolder.ofKeyframe(View.ALPHA, alpha0, alpha30, alpha100)

        val yEnd = 36f * resources.displayMetrics.density
        val pY = android.animation.PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, yEnd)
        val pScaleX = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.2f)
        val pScaleY = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.2f)

        ObjectAnimator.ofPropertyValuesHolder(view, pAlpha, pY, pScaleX, pScaleY).apply {
            duration = 1500
            startDelay = delayMs
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    private fun setupWindow() {
        dialog?.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setGravity(Gravity.CENTER)
            val marginPx = (16 * resources.displayMetrics.density).toInt()
            val maxWidthPx = (330 * resources.displayMetrics.density).toInt()
            val width = minOf(resources.displayMetrics.widthPixels - (marginPx * 2), maxWidthPx)
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun setupUI() {

        if (isRequired) {
            // Required / Hard Update — matches HTML Screen 04: .disc.tint-attn, border-color:rgba(238,77,61,.22)
            binding.cardDialog.setBackgroundResource(R.drawable.bg_app_update_dialog_red)
            binding.vDisc.setBackgroundResource(R.drawable.bg_hero_disc_red)
            binding.ivRocketIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.attention))

            binding.tvTitle.text = titleText ?: "Update to keep going"
            binding.tvDescription.text = descText
                ?: "This version is no longer supported. A quick update restores your readings and recommendations — your data is safe and waiting."

            binding.btnMaybeLater.isVisible = false
            binding.tvRequiredSubtext.isVisible = true
            binding.flSparkTrail.isVisible = false
        } else {
            // Optional / Soft Update — matches HTML Screen 03: .disc.tint-lime
            binding.cardDialog.setBackgroundResource(R.drawable.bg_app_update_dialog_lime)
            binding.vDisc.setBackgroundResource(R.drawable.bg_hero_disc_lime)
            binding.ivRocketIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.lime))

            binding.tvTitle.text = titleText ?: "A sharper Humotron is ready"
            binding.tvDescription.text = descText
                ?: "Faster readings and a cleaner recovery view. Takes about a minute."

            binding.btnMaybeLater.isVisible = true
            binding.tvRequiredSubtext.isVisible = false
            binding.flSparkTrail.isVisible = true
        }
    }

    private fun setupClickListeners() {
        binding.btnUpdateNow.setOnClickListener {
            onUpdateClickListener?.invoke() ?: openPlayStore()
            if (!isRequired) {
                dismiss()
            }
        }

        binding.btnMaybeLater.setOnClickListener {
            onDismissClickListener?.invoke()
            dismiss()
        }
    }

    private fun openPlayStore() {
        val packageName = requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun getAppVersionName(): String {
        return try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName ?: "2.3.1"
        } catch (_: Exception) {
            "2.3.1"
        }
    }

    fun setOnUpdateClickListener(listener: () -> Unit) {
        this.onUpdateClickListener = listener
    }

    fun setOnDismissClickListener(listener: () -> Unit) {
        this.onDismissClickListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AppUpdateDialogFragment"
        private const val ARG_IS_REQUIRED = "arg_is_required"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESC = "arg_desc"

        fun newInstance(
            isRequired: Boolean = false,
            title: String? = null,
            description: String? = null
        ): AppUpdateDialogFragment {
            val args = Bundle().apply {
                putBoolean(ARG_IS_REQUIRED, isRequired)
                putString(ARG_TITLE, title)
                putString(ARG_DESC, description)
            }
            return AppUpdateDialogFragment().apply {
                arguments = args
            }
        }
    }
}
