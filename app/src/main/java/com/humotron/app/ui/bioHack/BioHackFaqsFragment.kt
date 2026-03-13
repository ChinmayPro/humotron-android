package com.humotron.app.ui.bioHack

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.FragmentBioHackFaqsBinding


class BioHackFaqsFragment : BottomSheetDialogFragment(R.layout.fragment_bio_hack_faqs) {

    private lateinit var binding: FragmentBioHackFaqsBinding


    val bullet1 = "You don’t \"feel better\"—you see the improvement in your metrics."
    val bullet2 = "You don’t just \"try things\"—you test, track, and optimize"
    val bullet3 = "You don’t just follow trends—you personalize what works for your body."
    val bullet21 = "<b>For busy professionals</b> → Stay sharp, manage stress, and optimize energy."
    val bullet22 =
        "<b>For parents & caregivers</b> → Improve sleep, longevity, and overall well-being."
    val bullet23 =
        "<b>For health-conscious individuals</b> → Take control of your health beyond doctor visits."
    val bullet24 =
        "<b>For anyone curious about their biology</b> → Learn, experiment, and track progress with real data."


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBioHackFaqsBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 60)
            insets
        }

        val type = arguments?.getInt("type") ?: 0

        if (type == 1) {
            binding.first.visibility = View.VISIBLE
            binding.second.visibility = View.GONE
            binding.third.visibility = View.GONE
        } else if (type == 2) {
            binding.first.visibility = View.GONE
            binding.second.visibility = View.VISIBLE
            binding.third.visibility = View.GONE
        } else {
            binding.first.visibility = View.GONE
            binding.second.visibility = View.GONE
            binding.third.visibility = View.VISIBLE
        }

//        binding.textDetail1.text = string
        binding.bullet1.text = bullet1
        binding.bullet2.text = bullet2
        binding.bullet3.text = bullet3

        binding.bullet21.text = HtmlCompat.fromHtml(bullet21, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.bullet22.text = HtmlCompat.fromHtml(bullet22, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.bullet23.text = HtmlCompat.fromHtml(bullet23, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.bullet24.text = HtmlCompat.fromHtml(bullet24, HtmlCompat.FROM_HTML_MODE_LEGACY)

        binding.ivShrink.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                )!!

            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = true

            bottomSheet.layoutParams.height =
                ViewGroup.LayoutParams.MATCH_PARENT
        }

        return dialog
    }


}