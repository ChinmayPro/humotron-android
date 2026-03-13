package com.humotron.app.ui.onboarding.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.Preference.Companion.ONBOARD_PRIVACY
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentOnBoardPrivacyBinding


class OnBoardPrivacyFragment : BaseFragment(R.layout.fragment_on_board_privacy) {

    private lateinit var binding: FragmentOnBoardPrivacyBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnBoardPrivacyBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setPrivacyViews()
        binding.btnSubmit.setOnClickListener {
            prefUtils.setBoolean(ONBOARD_PRIVACY, true)
            findNavController().navigate(R.id.loginMethodFragment)
        }

        binding.cbPrivacy.setOnCheckedChangeListener { compoundButton, b ->
            binding.btnSubmit.isEnabled = b && binding.cbDataProcessing.isChecked
        }

        binding.cbDataProcessing.setOnCheckedChangeListener { compoundButton, b ->
            binding.btnSubmit.isEnabled = b && binding.cbPrivacy.isChecked
        }
    }

    private fun setPrivacyViews() {
        val fullText =
            "Allows us to use your data to offer key features & improvements.[Privacy Policy]"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("[Privacy Policy]")
        val end = start + "[Privacy Policy]".length
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = "https://www.google.com" // replace with your actual URL
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(widget.context, url.toUri())
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color =
                    ContextCompat.getColor(requireContext(), R.color.colorBgBtn) // custom color
                ds.isUnderlineText = false // optional: remove underline
            }
        }

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorBgBtn)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            clickableSpan,
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvPrivacyDesc.text = spannable

        binding.tvPrivacyDesc.movementMethod = LinkMovementMethod.getInstance()
        binding.tvPrivacyDesc.highlightColor = Color.TRANSPARENT


        val fullText1 =
            "Enables secure data storage & protection measures. [Data Security Policy]"
        val spannable1 = SpannableString(fullText1)

        val start1 = fullText1.indexOf("[Data Security Policy]")
        val end1 = start1 + "[Data Security Policy]".length
        val clickableSpan1 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = "https://www.google.com" // replace with your actual URL
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(widget.context, url.toUri())
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color =
                    ContextCompat.getColor(requireContext(), R.color.colorBgBtn) // custom color
                ds.isUnderlineText = false // optional: remove underline
            }
        }

        spannable1.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorBgBtn)),
            start1,
            end1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable1.setSpan(
            clickableSpan1,
            start1,
            end1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvDataProcessingDesc.text = spannable1

        binding.tvDataProcessingDesc.movementMethod = LinkMovementMethod.getInstance()
        binding.tvDataProcessingDesc.highlightColor = Color.TRANSPARENT
    }


}