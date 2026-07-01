package com.humotron.app.ui.decode

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeProcessingBinding
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeProcessingFragment : BaseFragment(R.layout.fragment_decode_processing) {

    private lateinit var binding: FragmentDecodeProcessingBinding
    private var metricName: String = ""
    private var metricId: String = ""
    private var selectedRange: String = ""
    private var pastWindows: Array<android.os.Parcelable> = emptyArray()

    private val handler = Handler(Looper.getMainLooper())
    private var processingStep = 0
    private lateinit var processingSteps: Array<String>

    private val animators = mutableListOf<android.animation.ValueAnimator>()

    private val processingRunnable = object : Runnable {
        override fun run() {
            if (processingStep < processingSteps.size) {
                binding.tvProcessingStatus.text = processingSteps[processingStep]
                processingStep++
                handler.postDelayed(this, 650)
            } else {
                navigateToDetail()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeProcessingBinding.bind(view)

        metricName = arguments?.getString("metricName") ?: ""
        metricId = arguments?.getString("metricId") ?: ""
        selectedRange = arguments?.getString("selectedRange") ?: ""
        pastWindows = arguments?.getParcelableArray("pastWindows") ?: emptyArray()

        processingSteps = arrayOf(
            getString(R.string.insights_processing_reading),
            getString(R.string.insights_processing_patterns),
            getString(R.string.insights_processing_dots),
            getString(R.string.insights_processing_insight)
        )

        initViews()
        setupInsets()
        initClicks()
        startProcessing()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.layoutProcessing.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            insets
        }
    }

    private fun initViews() {
        binding.header.title.text = ""
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            cancelProcessingAndGoBack()
        }
    }

    private fun startProcessing() {
        processingStep = 0
        handler.post(processingRunnable)
        startLabelAnimations()
    }

    private fun startLabelAnimations() {
        val labels = listOf(
            binding.tvHrv,
            binding.tvSleep,
            binding.tvTemp,
            binding.tvGlucose,
            binding.tvSteps,
            binding.tvStress
        )
        labels.forEachIndexed { index, textView ->
            textView.alpha = 0.4f
            val animator = android.animation.ValueAnimator.ofFloat(0.4f, 1.0f).apply {
                duration = 1200
                repeatCount = android.animation.ValueAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.REVERSE
                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                startDelay = index * 350L
                addUpdateListener { anim ->
                    textView.alpha = anim.animatedValue as Float
                }
            }
            animators.add(animator)
            animator.start()
        }
    }

    private fun navigateToDetail() {
        val bundle = Bundle().apply {
            putString("metricName", metricName)
            putString("metricId", metricId)
            putString("selectedRange", selectedRange)
            putParcelableArray("pastWindows", pastWindows)
        }
        findNavController().navigate(R.id.action_fragmentDecodeProcessing_to_fragmentDecodePatterns, bundle)
    }

    private fun cancelProcessingAndGoBack() {
        handler.removeCallbacks(processingRunnable)
        animators.forEach { it.cancel() }
        animators.clear()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(processingRunnable)
        animators.forEach { it.cancel() }
        animators.clear()
    }
}
