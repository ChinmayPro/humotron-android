package com.humotron.app.ui.onboarding.personalize

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.humotron.app.R
import com.humotron.app.databinding.FragmentPersonalizeBinding
import com.humotron.app.ui.onboarding.personalize.adapter.PersonalizePagerAdapter


class PersonalizeFragment : Fragment(R.layout.fragment_personalize) {


    private lateinit var binding: FragmentPersonalizeBinding
    private val viewModel: PagerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPersonalizeBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val position = arguments?.getInt("position") ?: 0


        val adapter = PersonalizePagerAdapter(requireActivity())
        binding.personalizeViewPager.adapter = adapter
        binding.personalizeViewPager.isUserInputEnabled = false
        binding.indicator.setPageCount(adapter.itemCount)
        binding.personalizeViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                binding.indicator.setProgress(position + positionOffset)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.indicator.setCurrentPage(position)
            }
        })

        binding.personalizeViewPager.setCurrentItem(position, false)

        viewModel.navigateToPage.observe(viewLifecycleOwner) { index ->
            if (index in 0 until (binding.personalizeViewPager.adapter?.itemCount ?: 0)) {
                binding.personalizeViewPager.currentItem = index
            }
        }
    }


}