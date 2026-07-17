package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentLegalCentreBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LegalCentreFragment : BaseFragment(R.layout.fragment_legal_centre) {

    private lateinit var binding: FragmentLegalCentreBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLegalCentreBinding.bind(view)

        initViews()
    }

    private fun initViews() {
        binding.header.title.text = getString(R.string.console_legal_centre)
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.clTermsOfService.setOnClickListener {
            openUrl("https://humotron.com/terms-of-service")
        }

        binding.clPrivacyPolicy.setOnClickListener {
            openUrl("https://humotron.com/privacy-policy")
        }

        binding.clCookiePolicy.setOnClickListener {
            openUrl("https://humotron.com/cookie-policy")
        }

        binding.clDataRights.setOnClickListener {
            openUrl("https://humotron.com/data-rights")
        }

        binding.clAcceptableUse.setOnClickListener {
            openUrl("https://humotron.com/acceptable-use")
        }
    }

    private fun openUrl(url: String) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(requireContext(), url.toUri())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
