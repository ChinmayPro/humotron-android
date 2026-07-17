package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentBuyOnHumotronBinding

class BuyOnHumotronFragment : Fragment() {

    private var _binding: FragmentBuyOnHumotronBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyOnHumotronBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getString("productId") ?: ""
        val productTitle = arguments?.getString("productTitle") ?: "Product"
        val productPrice = arguments?.getFloat("productPrice") ?: 0f

        // Format price
        val formattedPrice = if (productPrice > 0) "£" + String.format("%.0f", productPrice) else "Free"

        binding.tvProductName.text = productTitle
        binding.tvProductPrice.text = formattedPrice
        binding.tvProductTotal.text = formattedPrice

        binding.btnBack.setOnClickListener {
            if (binding.llSuccess.visibility == View.VISIBLE) {
                // If on success screen, navigate back to Optimize list
                popToOptimize()
            } else {
                findNavController().navigateUp()
            }
        }

        binding.btnPlaceOrder.setOnClickListener {
            // Set dynamic success message based on product title to match prototype exactly
            val successMessage = when {
                productTitle.lowercase().contains("magnesium") -> {
                    "Your magnesium is on the way — 2-day delivery. We'll start watching your overnight HR and HRV from tonight."
                }
                productTitle.lowercase().contains("pad") || productTitle.lowercase().contains("mattress") -> {
                    "Your mattress pad is on the way. We'll watch deep-sleep minutes on warm nights once it's in place."
                }
                productTitle.lowercase().contains("retinol") -> {
                    "Your retinol is on the way. Judge it by your own skin over 8–12 weeks, not a metric."
                }
                else -> {
                    "Your $productTitle is on the way. We'll start watching your metrics from tonight."
                }
            }
            binding.tvSuccessMessage.text = successMessage

            // Switch UI container to success
            binding.tvHeaderTitle.text = "Done"
            binding.nsvCheckout.visibility = View.GONE
            binding.llSuccess.visibility = View.VISIBLE
        }

        binding.btnBackToOptimize.setOnClickListener {
            popToOptimize()
        }
    }

    private fun popToOptimize() {
        // Pop back to the main DecodeFragment (Optimize tab)
        try {
            findNavController().popBackStack(R.id.fragmentDecode, false)
        } catch (e: Exception) {
            // Fallback
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
