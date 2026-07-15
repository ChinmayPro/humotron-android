package com.humotron.app.ui.decode

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.databinding.FragmentSeeRetailersBinding

class SeeRetailersFragment : Fragment() {

    private var _binding: FragmentSeeRetailersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeeRetailersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val productTitle = arguments?.getString("productTitle") ?: "Product"
        val matchPercentage = arguments?.getInt("matchPercentage") ?: 91
        val productUrl = arguments?.getString("productUrl") ?: ""

        binding.tvProductTitle.text = productTitle
        binding.tvMatchPercent.text = "$matchPercentage%"
        binding.pbMatchRing.progress = matchPercentage

        val privacyHtml = "<b><font color=\"#cfe69a\">Your data stays with Humotron.</font></b> We pass the match to the retailer — never your health signals."
        binding.tvPrivacyText.text = androidx.core.text.HtmlCompat.fromHtml(privacyHtml, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY)

        binding.cardThorne.setOnClickListener {
            openUrl(productUrl)
        }
        binding.cardPure.setOnClickListener {
            openUrl(productUrl)
        }
        binding.cardHolland.setOnClickListener {
            openUrl(productUrl)
        }
    }

    private fun openUrl(url: String) {
        if (url.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
