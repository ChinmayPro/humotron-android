package com.humotron.app.ui.dialogs

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.DialogMaintenanceBinding

class MaintenanceDialogFragment : DialogFragment() {

    private var _binding: DialogMaintenanceBinding? = null
    private val binding get() = _binding!!

    private var titleText: String? = null
    private var descText: String? = null
    private var endAtText: String? = null

    private var spinAnimator: ObjectAnimator? = null
    private var breatheAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        arguments?.let {
            titleText = it.getString(ARG_TITLE)
            descText = it.getString(ARG_DESC)
            endAtText = it.getString(ARG_END_AT)
        }
        isCancelable = false // User should not be able to bypass maintenance mode
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMaintenanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Manage edge-to-edge system bar insets so content is safe while background covers full screen
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            v.setPadding(
                systemBars.left + (24 * density).toInt(),
                systemBars.top + (20 * density).toInt(),
                systemBars.right + (24 * density).toInt(),
                systemBars.bottom + (20 * density).toInt()
            )
            insets
        }

        // Setup UI logic:
        // 1. Title: If API provides title, show it. Otherwise show default title string resource.
        binding.tvTitle.text = if (!titleText.isNullOrBlank()) {
            titleText
        } else {
            getString(R.string.maintenance_default_title)
        }

        // 2. Description: If API provides message, show it. Otherwise show default desc string resource.
        binding.tvDescription.text = if (!descText.isNullOrBlank()) {
            descText
        } else {
            getString(R.string.maintenance_default_desc)
        }

        // 3. Back by / ETA View (endAt): If API provides endAt data, show view. Otherwise hide view.
        if (!endAtText.isNullOrBlank()) {
            binding.tvEta.text = endAtText
            binding.llEta.visibility = View.VISIBLE
        } else {
            binding.llEta.visibility = View.GONE
        }

        startAnimations()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    private fun startAnimations() {
        // Continuous 360-degree rotation of the calibration arc (spin 2.6s linear infinite)
        spinAnimator = ObjectAnimator.ofFloat(binding.ivRecalArc, View.ROTATION, 0f, 360f).apply {
            duration = 2600L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }

        // Breathe animation for the center dot (breathe 3.2s ease-in-out infinite)
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.15f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.15f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.75f)

        breatheAnimator = ObjectAnimator.ofPropertyValuesHolder(binding.vCenterDot, scaleX, scaleY, alpha).apply {
            duration = 1600L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    override fun onDestroyView() {
        spinAnimator?.cancel()
        breatheAnimator?.cancel()
        spinAnimator = null
        breatheAnimator = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MaintenanceDialogFragment"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESC = "arg_desc"
        private const val ARG_END_AT = "arg_end_at"

        fun newInstance(
            title: String? = null,
            description: String? = null,
            endAt: String? = null
        ): MaintenanceDialogFragment {
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_DESC, description)
                putString(ARG_END_AT, endAt)
            }
            return MaintenanceDialogFragment().apply {
                arguments = args
            }
        }
    }
}
