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
import android.content.Intent
import com.humotron.app.ui.MainActivity
import com.humotron.app.R
import com.humotron.app.core.Preference.Companion.ONBOARD_PRIVACY
import com.humotron.app.core.base.BaseFragment
import androidx.core.text.HtmlCompat
import com.humotron.app.databinding.FragmentOnBoardPrivacyBinding


class OnBoardPrivacyFragment : BaseFragment(R.layout.fragment_on_board_privacy) {

    private lateinit var binding: FragmentOnBoardPrivacyBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnBoardPrivacyBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        binding.tvSubtitle.text = HtmlCompat.fromHtml(
            getString(R.string.onboard_privacy_desc),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        setPrivacyViews()
        binding.btnSubmit.setOnClickListener {
            prefUtils.setBoolean(ONBOARD_PRIVACY, true)
            val user = prefUtils.getLoginResponse()
            if (user?.isOnBoarding == true) {
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            } else {
                if (user?.name.isNullOrEmpty()) {
                    findNavController().navigate(R.id.personalizeFragment)
                } else if (user?.height.isNullOrEmpty()) {
                    val bundle = Bundle().apply { putInt("position", 1) }
                    findNavController().navigate(R.id.personalizeFragment, bundle)
                } else {
                    val bundle = Bundle().apply { putInt("position", 3) }
                    findNavController().navigate(R.id.personalizeFragment, bundle)
                }
            }
        }

        binding.cbPrivacy.setOnCheckedChangeListener { compoundButton, b ->
            binding.btnSubmit.isEnabled = b && binding.cbDataProcessing.isChecked
            binding.containerPrivacy.setBackgroundResource(
                if (b) R.drawable.bg_consent_container_selected else R.drawable.bg_consent_container
            )
        }

        binding.cbDataProcessing.setOnCheckedChangeListener { compoundButton, b ->
            binding.btnSubmit.isEnabled = b && binding.cbPrivacy.isChecked
            binding.containerDataProcessing.setBackgroundResource(
                if (b) R.drawable.bg_consent_container_selected else R.drawable.bg_consent_container
            )
        }
    }

    private fun setPrivacyViews() {
        val fullText = getString(R.string.onboard_privacy_desc1)
        val spannable = SpannableString(fullText)

        val targetText = "Privacy Policy"
        val start = fullText.indexOf(targetText)
        if (start != -1) {
            val end = start + targetText.length
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val url = "https://www.google.com" // replace with your actual URL
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(widget.context, url.toUri())
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(requireContext(), R.color.insights_green)
                    ds.isUnderlineText = false
                }
            }

            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.insights_green)),
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
        }

        binding.tvPrivacyDesc.text = spannable
        binding.tvPrivacyDesc.movementMethod = LinkMovementMethod.getInstance()
        binding.tvPrivacyDesc.highlightColor = Color.TRANSPARENT


        val fullText1 = getString(R.string.onboard_data_policy_desc)
        val spannable1 = SpannableString(fullText1)

        val targetText1 = "Data Security Policy"
        val start1 = fullText1.indexOf(targetText1)
        if (start1 != -1) {
            val end1 = start1 + targetText1.length
            val clickableSpan1 = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val url = "https://www.google.com" // replace with your actual URL
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(widget.context, url.toUri())
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(requireContext(), R.color.insights_green)
                    ds.isUnderlineText = false
                }
            }

            spannable1.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.insights_green)),
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
        }

        binding.tvDataProcessingDesc.text = spannable1
        binding.tvDataProcessingDesc.movementMethod = LinkMovementMethod.getInstance()
        binding.tvDataProcessingDesc.highlightColor = Color.TRANSPARENT
    }


}