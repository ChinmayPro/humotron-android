package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopScansBinding
import androidx.fragment.app.activityViewModels
import com.humotron.app.data.network.Status
import dagger.hilt.android.AndroidEntryPoint

import com.google.android.material.dialog.MaterialAlertDialogBuilder

@AndroidEntryPoint
class ShopScansFragment : BaseFragment(R.layout.fragment_shop_scans) {

    private lateinit var binding: FragmentShopScansBinding
    private val viewModel: ShopViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopScansBinding.bind(view)

        setupInsets()
        initObservers()
        viewModel.fetchBloodTestServices()

        binding.btnBookNow.setOnClickListener {
            binding.btnBookNow.isEnabled = false
            viewModel.fetchBloodTestOrders()
        }

        startAnimations()
    }

    private fun initObservers() {
        viewModel.getBloodTestServicesLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val service = resource.data?.data?.services?.firstOrNull()
                    if (service != null) {
                        val priceFormatted = service.price?.formatted
                            ?: if (service.price?.amount != null) "${service.price?.symbol ?: "$"}${service.price?.amount}"
                            else null
                        if (!priceFormatted.isNullOrEmpty()) {
                            binding.btnBookNow.text = "Book a scan · $priceFormatted"
                        }
                    }
                }
                else -> {}
            }
        }

        viewModel.getBloodTestOrdersLiveData().observe(viewLifecycleOwner) { resource ->
            binding.btnBookNow.isEnabled = true
            when (resource.status) {
                Status.SUCCESS -> {
                    val orders = resource.data?.orderList
                    val hasActiveBooking = orders?.any { order ->
                        val st = order.status?.lowercase() ?: ""
                        st != "cancelled" && st != "completed" && st != "refunded"
                    } == true

                    if (hasActiveBooking) {
                        showAlreadyBookedDialog()
                    } else {
                        parentFragment?.parentFragment?.findNavController()?.navigate(R.id.action_fragmentShop_to_fragmentShopTestDetail)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    parentFragment?.parentFragment?.findNavController()?.navigate(R.id.action_fragmentShop_to_fragmentShopTestDetail)
                }
                Status.LOADING -> {
                    binding.btnBookNow.isEnabled = false
                }
            }
        }
    }

    private fun showAlreadyBookedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("You already have a Deep Scan booked. We'll let you know when your kit ships.")
            .setPositiveButton("Okay") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            binding.layoutFooter.updatePadding(bottom = systemBars.bottom + (20 * density).toInt())
            insets
        }
    }

    private fun startAnimations() {
        val density = resources.displayMetrics.density
        val startY = 16f * density
        val endY = 176f * density

        // Line translation animation
        val lineAnim = android.animation.ObjectAnimator.ofFloat(binding.vScanLine, "translationY", startY, endY).apply {
            duration = 1600L
            repeatMode = android.animation.ValueAnimator.REVERSE
            repeatCount = android.animation.ValueAnimator.INFINITE
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }

        // Line alpha animation
        val alphaAnim = android.animation.ObjectAnimator.ofFloat(binding.vScanLine, "alpha", 0.4f, 1.0f).apply {
            duration = 1600L
            repeatMode = android.animation.ValueAnimator.REVERSE
            repeatCount = android.animation.ValueAnimator.INFINITE
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }

        lineAnim.start()
        alphaAnim.start()

        // Pulse animations for the 3 dots
        startPulse(binding.vScanPulse1, 0L)
        startPulse(binding.vScanPulse2, 600L)
        startPulse(binding.vScanPulse3, 1100L)
    }

    private fun startPulse(pulseView: View, delay: Long) {
        pulseView.scaleX = 0.5f
        pulseView.scaleY = 0.5f
        pulseView.alpha = 0.8f

        val scaleX = android.animation.ObjectAnimator.ofFloat(pulseView, "scaleX", 0.5f, 2.5f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(pulseView, "scaleY", 0.5f, 2.5f)
        val alpha = android.animation.ObjectAnimator.ofFloat(pulseView, "alpha", 0.8f, 0.0f)

        val set = android.animation.AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 2000L
            startDelay = delay
        }

        set.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // Restart with 0 delay on subsequent runs
                set.startDelay = 0
                set.start()
            }
        })

        set.start()
    }
}
