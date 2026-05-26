package com.humotron.app.ui.cart

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.FontAssetDelegate
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentOrderSuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderSuccessFragment : BaseFragment(R.layout.fragment_order_success) {

    private lateinit var binding: FragmentOrderSuccessBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOrderSuccessBinding.bind(view)

        // Fix for Lottie font crash: Font asset not found fonts/Helvetica.ttf
        binding.lottieSuccess.setFontAssetDelegate(object : FontAssetDelegate() {
            override fun fetchFont(fontFamily: String?): Typeface {
                return Typeface.DEFAULT
            }
        })

        val orderId = arguments?.getString("orderId") ?: ""
        binding.tvOrderId.text = orderId

        // Set underline for Learn More text
        binding.tvLearnMore.paintFlags = binding.tvLearnMore.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Handle window insets to avoid overlapping with system navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.tvLearnMore) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as ConstraintLayout.LayoutParams
            params.bottomMargin = systemBars.bottom + (20 * resources.displayMetrics.density).toInt()
            v.layoutParams = params
            insets
        }

        binding.btnCheckOrderDetails.setOnClickListener {
            navigateToProfile()
        }

        binding.tvLearnMore.setOnClickListener {
            // Optional: Handle learn more if needed
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToProfile()
        }
    }

    private fun navigateToProfile() {
        // Navigate to ProfileFragment and clear back stack
        // Assuming action_fragmentOrderSuccess_to_fragmentProfile is defined in nav_graph.xml
        try {
            findNavController().navigate(R.id.action_fragmentOrderSuccess_to_fragmentProfile)
        } catch (e: Exception) {
            // Fallback if action is not found or fails
            findNavController().popBackStack(R.id.fragmentProfile, false)
        }
    }
}
